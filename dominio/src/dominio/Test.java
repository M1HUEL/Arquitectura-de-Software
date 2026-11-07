package dominio;

import broker.Broker;
import broker.cliente.ClienteBroker;
import comun.MensajesJuego;
import comun.MensajesJuego.EstadoPartidaJuego;
import comun.MensajesJuego.EventoPartida;
import comun.MensajesJuego.Movimiento;
import comun.MensajesJuego.MovimientoProcesado;
import comun.Protocolo;
import comun.Tipos;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Test {

  private static final String HOST = "127.0.0.1";
  private static final long ESPERA_SEGUNDOS = 3;
  private static final long INTERVALO_LARGO_MS = 60_000;

  public static void main(String[] args) throws Exception {
    Broker broker = new Broker();
    broker.iniciar(0);
    int puerto = broker.obtenerPuerto();

    Partida partida = crearPartida();
    ServicioDominio servicio = new ServicioDominio(partida, HOST, puerto, INTERVALO_LARGO_MS);
    servicio.iniciar();

    try {
      Observador observador = new Observador();
      try (ClienteBroker cliente = new ClienteBroker(HOST, puerto, observador)) {
        cliente.suscribir(Tipos.TEMA_PARTIDA_ESTADO);
        cliente.suscribir(Tipos.TEMA_PARTIDA_EVENTO);
        cliente.suscribir(Tipos.TEMA_MOVIMIENTO_PROCESADO);
        cliente.publicar(Tipos.TEMA_SINCRONIZAR, new byte[0]);

        verificarEstadoInicial(esperarEstado(observador));
        verificarMovimientos(cliente, observador);
        verificarVictoria(cliente, observador);
      }
    } finally {
      servicio.close();
      broker.detener();
    }

    System.out.println("Integración dominio-broker OK");
  }

  private static Partida crearPartida() {
    Mazo mazo = new Mazo();
    List<Carta> cartas = mazo.obtenerCartas();
    Partida partida = new Partida(mazo);
    partida.agregarJugador(new Jugador(
      "Jugador 1", "jugador1.png", cartas.subList(0, 16)));
    partida.agregarJugador(new Jugador(
      "Jugador 2", "jugador2.png", cartas.subList(16, 32)));
    partida.establecerPatron(Patron.LINEA);
    partida.iniciar();
    for (int i = 0; i < 4; i++) {
      partida.cantarSiguienteCarta();
    }
    return partida;
  }

  private static void verificarEstadoInicial(EstadoPartidaJuego estado) {
    verificar("EN_CURSO".equals(estado.estado()), "Estado inicial incorrecto");
    verificar(estado.jugadores().size() == 2, "Número de jugadores incorrecto");
    verificar(estado.cartasCantadas().size() == 4, "Cartas cantadas iniciales incorrectas");
    verificar(estado.cartasCantadas().contains("El gallo"), "Falta El gallo");
    verificar(estado.cartasRestantes() == 50, "Cartas restantes iniciales incorrectas");
  }

  private static void verificarMovimientos(ClienteBroker cliente, Observador observador)
      throws Exception {
    enviar(cliente, new Movimiento(
      "Jugador 1", MensajesJuego.CASILLA, "El gallo", ""));
    MovimientoProcesado valido = esperarMovimiento(observador);
    verificar(valido.valido(), "La casilla válida fue rechazada");
    verificar("Casilla válida".equals(valido.detalle()), "Detalle de casilla válida incorrecto");

    enviar(cliente, new Movimiento(
      "Jugador 1", MensajesJuego.CASILLA, "La sirena", ""));
    MovimientoProcesado noCantada = esperarMovimiento(observador);
    verificar(!noCantada.valido(), "Se aceptó una carta no cantada");

    enviar(cliente, new Movimiento(
      "Jugador 1", MensajesJuego.PATRON, "", "LINEA"));
    MovimientoProcesado patron = esperarMovimiento(observador);
    verificar(patron.valido(), "El patrón LINEA debía ser válido");

    enviar(cliente, new Movimiento(
      "Nadie", MensajesJuego.CASILLA, "El gallo", ""));
    MovimientoProcesado desconocido = esperarMovimiento(observador);
    verificar(!desconocido.valido(), "Se aceptó un jugador desconocido");
    verificar("Jugador desconocido".equals(desconocido.detalle()),
      "Detalle de jugador desconocido incorrecto");
  }

  private static void verificarVictoria(ClienteBroker cliente, Observador observador)
      throws Exception {
    enviar(cliente, new Movimiento(
      "Jugador 1", MensajesJuego.VICTORIA, "", "LINEA"));

    MovimientoProcesado victoria = esperarMovimiento(observador);
    verificar(victoria.valido(), "La victoria debía ser válida");

    EventoPartida evento = observador.eventos.poll(ESPERA_SEGUNDOS, TimeUnit.SECONDS);
    verificar(evento != null, "No llegó el evento de fin de partida");
    verificar(MensajesJuego.PARTIDA_TERMINADA.equals(evento.tipo()),
      "Tipo de evento de fin incorrecto");

    EstadoPartidaJuego estado = esperarEstado(observador);
    verificar("TERMINADA".equals(estado.estado()), "La partida no terminó");
    verificar("Jugador 1".equals(estado.ganador()), "Ganador incorrecto");
  }

  private static void enviar(ClienteBroker cliente, Movimiento movimiento) throws Exception {
    cliente.publicar(Tipos.TEMA_MOVIMIENTO, MensajesJuego.movimiento(movimiento));
  }

  private static EstadoPartidaJuego esperarEstado(Observador observador)
      throws InterruptedException {
    EstadoPartidaJuego estado = observador.estados.poll(ESPERA_SEGUNDOS, TimeUnit.SECONDS);
    verificar(estado != null, "No llegó el estado de la partida");
    return estado;
  }

  private static MovimientoProcesado esperarMovimiento(Observador observador)
      throws InterruptedException {
    MovimientoProcesado movimiento = observador.movimientos.poll(
      ESPERA_SEGUNDOS, TimeUnit.SECONDS);
    verificar(movimiento != null, "No llegó la respuesta del movimiento");
    return movimiento;
  }

  private static void verificar(boolean condicion, String mensaje) {
    if (!condicion) {
      throw new AssertionError(mensaje);
    }
  }

  private static final class Observador implements ClienteBroker.Observador {

    private final BlockingQueue<EstadoPartidaJuego> estados = new LinkedBlockingQueue<>();
    private final BlockingQueue<MovimientoProcesado> movimientos = new LinkedBlockingQueue<>();
    private final BlockingQueue<EventoPartida> eventos = new LinkedBlockingQueue<>();

    @Override
    public void alRecibirPublicacion(Protocolo.Publicacion publicacion) {
      switch (publicacion.tema()) {
        case Tipos.TEMA_PARTIDA_ESTADO -> this.estados.add(
          MensajesJuego.leerEstado(publicacion.contenido()));
        case Tipos.TEMA_MOVIMIENTO_PROCESADO -> this.movimientos.add(
          MensajesJuego.leerMovimientoProcesado(publicacion.contenido()));
        case Tipos.TEMA_PARTIDA_EVENTO -> this.eventos.add(
          MensajesJuego.leerEvento(publicacion.contenido()));
        default -> {
        }
      }
    }
  }
}
