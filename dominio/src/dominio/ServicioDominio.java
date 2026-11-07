package dominio;

import broker.cliente.ClienteBroker;
import comun.MensajesJuego;
import comun.MensajesJuego.EstadoPartidaJuego;
import comun.MensajesJuego.EventoPartida;
import comun.MensajesJuego.JugadorEstado;
import comun.MensajesJuego.Movimiento;
import comun.MensajesJuego.MovimientoProcesado;
import comun.Protocolo;
import comun.Tipos;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ServicioDominio implements AutoCloseable {

  private final Partida partida;
  private final ClienteBroker cliente;
  private final ScheduledExecutorService cantador;
  private final long intervaloMs;
  private volatile boolean activo = true;
  private volatile String ganador = "";

  public ServicioDominio(Partida partida, String host, int puerto, long intervaloMs)
      throws IOException {
    if (partida == null) {
      throw new IllegalArgumentException("La partida no puede ser nula");
    }
    if (intervaloMs <= 0) {
      throw new IllegalArgumentException("El intervalo debe ser positivo");
    }
    this.partida = partida;
    this.intervaloMs = intervaloMs;
    this.cliente = new ClienteBroker(host, puerto, new Observador());
    this.cantador = Executors.newSingleThreadScheduledExecutor(tarea -> {
      Thread hilo = new Thread(tarea, "cantador-dominio");
      hilo.setDaemon(true);
      return hilo;
    });
  }

  public void iniciar() throws IOException {
    this.cliente.suscribir(Tipos.TEMA_MOVIMIENTO);
    this.cliente.suscribir(Tipos.TEMA_SINCRONIZAR);
    publicarEvento(MensajesJuego.PARTIDA_INICIADA, "", "Partida iniciada");
    publicarEstado();
    this.cantador.scheduleAtFixedRate(
      this::cantar, this.intervaloMs, this.intervaloMs, TimeUnit.MILLISECONDS);
  }

  @Override
  public void close() {
    if (!this.activo) {
      return;
    }
    this.activo = false;
    this.cantador.shutdownNow();
    this.cliente.close();
  }

  private synchronized void cantar() {
    if (!this.activo) {
      return;
    }
    int antes = this.partida.obtenerNumeroCartasCantadas();
    this.partida.cantarSiguienteCarta();
    int despues = this.partida.obtenerNumeroCartasCantadas();
    if (despues > antes) {
      Carta carta = this.partida.obtenerCartasCantadas().get(despues - 1);
      publicarEvento(MensajesJuego.CARTA_CANTADA, carta.obtenerNombre(), "");
      publicarEstado();
    }

    String nuevoGanador = buscarGanador();
    if (nuevoGanador != null) {
      this.ganador = nuevoGanador;
      this.partida.finalizar();
      publicarEvento(MensajesJuego.PARTIDA_TERMINADA, "", "Ganó " + nuevoGanador);
      publicarEstado();
      this.cantador.shutdown();
      return;
    }
    if (this.partida.obtenerEstadoPartida() == EstadoPartida.TERMINADA) {
      publicarEvento(MensajesJuego.PARTIDA_TERMINADA, "", "Se agotaron las cartas");
      publicarEstado();
      this.cantador.shutdown();
    }
  }

  private synchronized void procesarMovimiento(byte[] contenido) {
    Movimiento movimiento;
    try {
      movimiento = MensajesJuego.leerMovimiento(contenido);
    } catch (RuntimeException e) {
      System.err.println("Dominio: movimiento inválido: " + e.getMessage());
      return;
    }

    Jugador jugador = buscarJugador(movimiento.jugador());
    boolean valido = false;
    String detalle = "";
    if (jugador == null) {
      detalle = "Jugador desconocido";
    } else {
      switch (movimiento.tipo()) {
        case MensajesJuego.CASILLA -> {
          Carta carta = buscarCarta(jugador, movimiento.carta());
          if (carta == null) {
            detalle = "El jugador no tiene esa carta";
          } else if (!this.partida.obtenerCartasCantadas().contains(carta)) {
            detalle = "La carta todavía no ha sido cantada";
          } else if (!jugador.marcarCasilla(carta)) {
            valido = true;
            detalle = "La casilla ya estaba seleccionada";
          } else {
            valido = true;
            detalle = "Casilla válida";
          }
        }
        case MensajesJuego.PATRON -> {
          Patron patron = leerPatron(movimiento.patron());
          if (patron == null) {
            detalle = "Patrón desconocido";
          } else {
            valido = this.partida.cumplePatron(jugador, patron);
            detalle = valido ? "Patrón válido" : "El jugador no cumple el patrón";
          }
        }
        case MensajesJuego.VICTORIA -> {
          Patron patron = leerPatron(movimiento.patron());
          if (patron == null) {
            detalle = "Patrón desconocido";
          } else {
            valido = this.partida.tieneVictoria(jugador, patron);
            detalle = valido ? "Victoria válida" : "Victoria inválida";
          }
        }
        default -> detalle = "Tipo de movimiento desconocido";
      }
    }

    publicarMovimientoProcesado(new MovimientoProcesado(
      movimiento.jugador(), movimiento.tipo(), movimiento.carta(),
      movimiento.patron(), valido, detalle));

    if (valido && MensajesJuego.CASILLA.equals(movimiento.tipo())) {
      publicarEstado();
    }

    if (valido && MensajesJuego.VICTORIA.equals(movimiento.tipo())) {
      this.ganador = movimiento.jugador();
      this.partida.finalizar();
      publicarEvento(MensajesJuego.PARTIDA_TERMINADA, "", "Ganó " + this.ganador);
      publicarEstado();
      this.cantador.shutdown();
    }
  }

  private Jugador buscarJugador(String nombre) {
    for (Jugador jugador : this.partida.obtenerJugadores()) {
      if (jugador.obtenerNombre().equals(nombre)) {
        return jugador;
      }
    }
    return null;
  }

  private Carta buscarCarta(Jugador jugador, String nombre) {
    for (Carta carta : jugador.obtenerCartas()) {
      if (carta.obtenerNombre().equals(nombre)) {
        return carta;
      }
    }
    return null;
  }

  private Patron leerPatron(String valor) {
    try {
      return Patron.valueOf(valor);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private String buscarGanador() {
    Patron patron = this.partida.obtenerPatron();
    if (patron == null) {
      return null;
    }
    for (Jugador jugador : this.partida.obtenerJugadores()) {
      if (this.partida.cumplePatron(jugador, patron)) {
        return jugador.obtenerNombre();
      }
    }
    return null;
  }

  private synchronized void publicarEstado() {
    if (!this.activo) {
      return;
    }
    List<JugadorEstado> jugadores = new ArrayList<>();
    for (Jugador jugador : this.partida.obtenerJugadores()) {
      List<String> cartas = new ArrayList<>();
      for (Carta carta : jugador.obtenerCartas()) {
        cartas.add(carta.obtenerNombre());
      }
      List<String> seleccionadas = new ArrayList<>();
      for (Carta carta : jugador.obtenerCasillasSeleccionadas()) {
        seleccionadas.add(carta.obtenerNombre());
      }
      jugadores.add(new JugadorEstado(jugador.obtenerNombre(), jugador.obtenerImagen(),
        jugador.obtenerPuntaje(), cartas, seleccionadas));
    }

    List<String> cartasCantadas = new ArrayList<>();
    for (Carta carta : this.partida.obtenerCartasCantadas()) {
      cartasCantadas.add(carta.obtenerNombre());
    }

    EstadoPartidaJuego estado = new EstadoPartidaJuego(
      this.partida.obtenerEstadoPartida().name(),
      this.partida.obtenerNumeroCartasRestantes(),
      this.partida.obtenerNumeroCartasCantadas(),
      this.ganador,
      this.partida.obtenerPatron() == null ? "" : this.partida.obtenerPatron().name(),
      jugadores,
      cartasCantadas);
    publicar(Tipos.TEMA_PARTIDA_ESTADO, MensajesJuego.estado(estado));
  }

  private void publicarEvento(String tipo, String carta, String detalle) {
    if (!this.activo) {
      return;
    }
    publicar(Tipos.TEMA_PARTIDA_EVENTO,
      MensajesJuego.evento(new EventoPartida(tipo, carta, detalle)));
  }

  private void publicarMovimientoProcesado(MovimientoProcesado movimiento) {
    if (!this.activo) {
      return;
    }
    publicar(Tipos.TEMA_MOVIMIENTO_PROCESADO, MensajesJuego.movimientoProcesado(movimiento));
  }

  private void publicar(String tema, byte[] contenido) {
    try {
      this.cliente.publicar(tema, contenido);
    } catch (IOException e) {
      System.err.println("Dominio: no se pudo publicar en " + tema + ": " + e.getMessage());
      this.activo = false;
      this.cantador.shutdown();
    }
  }

  private final class Observador implements ClienteBroker.Observador {

    @Override
    public void alRecibirPublicacion(Protocolo.Publicacion publicacion) {
      switch (publicacion.tema()) {
        case Tipos.TEMA_MOVIMIENTO -> procesarMovimiento(publicacion.contenido());
        case Tipos.TEMA_SINCRONIZAR -> publicarEstado();
        default -> {
        }
      }
    }

    @Override
    public void alError(Protocolo.Error error) {
      System.err.println("Dominio: error del broker " + error.codigo() + ": " + error.detalle());
    }

    @Override
    public void alDesconectar(Throwable causa) {
      String detalle = causa == null ? "sin detalle" : causa.getMessage();
      System.err.println("Dominio: desconectado del broker: " + detalle);
      ServicioDominio.this.activo = false;
      ServicioDominio.this.cantador.shutdownNow();
    }
  }
}
