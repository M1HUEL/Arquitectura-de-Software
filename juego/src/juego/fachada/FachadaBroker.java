package juego.fachada;

import comun.CartaMensaje;
import comun.ComandoJuego;
import comun.EventoPartida;
import comun.JugadorMensaje;
import comun.MensajeBroker;
import comunicacion.Dispatcher;
import comunicacion.DispatcherFactory;
import comunicacion.Receptor;
import comunicacion.api.IReceptorExterno;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import juego.modelo.Carta;
import juego.modelo.EstadoPartida;
import juego.modelo.Jugador;
import juego.modelo.Mazo;
import juego.modelo.Mensaje;
import juego.modelo.Patron;
import serializador.ISerializador;
import serializador.Serializador;

public class FachadaBroker implements Fachada, IReceptorExterno {

  private static final String PATRON_FILTRO = "comun.*;java.lang.*;java.util.*;!*";

  private final String jugadorLocal;
  private final String hostBroker;
  private final int puertoBroker;
  private final String temaComandos;
  private final String temaEventos;
  private final int puertoLocal;
  private final ISerializador serializador;
  private final Dispatcher dispatcher;
  private final Receptor receptor;
  private final List<Runnable> oyentes = new CopyOnWriteArrayList<>();
  private final Map<String, Integer> idsCartas = new ConcurrentHashMap<>();

  private volatile List<Jugador> jugadores = List.of();
  private volatile List<Carta> cartasCantadas = List.of();
  private volatile int restantes;
  private volatile EstadoPartida estadoPartida = EstadoPartida.NO_INICIADA;
  private volatile String ganador = "";
  private volatile Patron patron = Patron.LINEA;
  private volatile boolean conectado;

  public FachadaBroker(
    String jugadorLocal,
    String hostBroker,
    int puertoBroker,
    String temaComandos,
    String temaEventos,
    int puertoLocal
  ) {
    if (jugadorLocal == null || jugadorLocal.isBlank()) {
      throw new IllegalArgumentException("El jugador local no puede estar vacío");
    }
    if (hostBroker == null || hostBroker.isBlank()) {
      throw new IllegalArgumentException("El host del broker no puede estar vacío");
    }
    if (puertoBroker < 0 || puertoBroker > 65535) {
      throw new IllegalArgumentException("El puerto del broker no es válido: " + puertoBroker);
    }
    if (temaComandos == null || temaComandos.isBlank()) {
      throw new IllegalArgumentException("El tema de comandos no puede estar vacío");
    }
    if (temaEventos == null || temaEventos.isBlank()) {
      throw new IllegalArgumentException("El tema de eventos no puede estar vacío");
    }
    if (puertoLocal < 0 || puertoLocal > 65535) {
      throw new IllegalArgumentException("El puerto local no es válido: " + puertoLocal);
    }
    this.jugadorLocal = jugadorLocal.trim();
    this.hostBroker = hostBroker.trim();
    this.puertoBroker = puertoBroker;
    this.temaComandos = temaComandos.trim();
    this.temaEventos = temaEventos.trim();
    this.puertoLocal = puertoLocal;
    this.serializador = new Serializador(PATRON_FILTRO);
    this.dispatcher = DispatcherFactory.crear();
    this.receptor = new Receptor();
    this.receptor.agregarReceptor(this);
  }

  @Override
  public void iniciar() {
    if (conectado) {
      return;
    }
    dispatcher.iniciar();
    receptor.iniciar(puertoLocal);
    if (!receptor.estaActivo()) {
      dispatcher.detener();
      throw new IllegalStateException(
        "No se pudo iniciar el MVC en el puerto " + puertoLocal);
    }
    conectado = true;
    enviar(new ComandoJuego(ComandoJuego.INICIAR, jugadorLocal, 0, patron.name()));
  }

  @Override
  public void cerrar() {
    if (conectado) {
      enviar(new ComandoJuego(ComandoJuego.CERRAR, jugadorLocal, 0, patron.name()));
      conectado = false;
    }
    receptor.detener();
    dispatcher.detener();
  }

  @Override
  public void agregarOyente(Runnable oyente) {
    if (oyente != null) {
      oyentes.add(oyente);
    }
  }

  @Override
  public void eliminarOyente(Runnable oyente) {
    oyentes.remove(oyente);
  }

  @Override
  public String obtenerJugadorLocal() {
    return jugadorLocal;
  }

  @Override
  public List<Jugador> obtenerJugadores() {
    return jugadores;
  }

  @Override
  public List<Carta> obtenerCartasCantadas() {
    return cartasCantadas;
  }

  @Override
  public int obtenerNumeroCartasRestantes() {
    return restantes;
  }

  @Override
  public EstadoPartida obtenerEstadoPartida() {
    return estadoPartida;
  }

  @Override
  public String obtenerGanador() {
    return ganador;
  }

  @Override
  public Patron obtenerPatron() {
    return patron;
  }

  @Override
  public Mensaje seleccionarCasilla(Jugador jugador, Carta carta) {
    if (!puedeJugar(jugador) || carta == null) {
      return Mensaje.CASILLA_INVALIDA;
    }
    if (!jugador.obtenerCartas().contains(carta)) {
      return Mensaje.CASILLA_INVALIDA;
    }
    if (!fueCantada(carta)) {
      return Mensaje.CASILLA_INVALIDA;
    }
    if (!jugador.marcarCasilla(carta)) {
      return Mensaje.CASILLA_INVALIDA;
    }
    enviar(new ComandoJuego(ComandoJuego.MARCAR, jugadorLocal, idDe(carta), patron.name()));
    return Mensaje.CASILLA_VALIDA;
  }

  @Override
  public Mensaje seleccionarPatron(Jugador jugador, Patron patron) {
    if (patron == null) {
      return Mensaje.PATRON_INVALIDO;
    }
    this.patron = patron;
    enviar(new ComandoJuego(ComandoJuego.PATRON, jugadorLocal, 0, patron.name()));
    if (!puedeJugar(jugador) || !jugador.cumple(patron)) {
      return Mensaje.PATRON_INVALIDO;
    }
    return Mensaje.PATRON_VALIDO;
  }

  @Override
  public Mensaje seleccionarVictoria(Jugador jugador, Patron patron) {
    if (patron == null || !puedeJugar(jugador) || !jugador.cumple(patron)) {
      return Mensaje.VICTORIA_INVALIDA;
    }
    this.patron = patron;
    enviar(new ComandoJuego(ComandoJuego.VICTORIA, jugadorLocal, 0, patron.name()));
    return Mensaje.VICTORIA_VALIDA;
  }

  @Override
  public void alRecibir(byte[] datos) {
    Object objeto = serializador.deserializar(datos, Object.class);
    if (!(objeto instanceof EventoPartida evento)) {
      return;
    }
    actualizar(evento);
    notificarOyentes();
  }

  private void actualizar(EventoPartida evento) {
    estadoPartida = convertirEstado(evento.estado());
    if (evento.patron() != null && !evento.patron().isBlank()) {
      try {
        patron = Patron.valueOf(evento.patron().trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
      }
    }
    restantes = evento.restantes();
    ganador = evento.ganador() == null ? "" : evento.ganador();
    cartasCantadas = convertirCartas(evento.cartasCantadas());
    jugadores = convertirJugadores(evento.jugadores());
  }

  private EstadoPartida convertirEstado(String estado) {
    if (estado == null || estado.isBlank()) {
      return estadoPartida;
    }
    return switch (estado.trim().toUpperCase(Locale.ROOT)) {
      case "EN_CURSO" ->
        EstadoPartida.EN_CURSO;
      case "FINALIZADA" ->
        EstadoPartida.TERMINADA;
      case "CREADA", "PAUSADA" ->
        EstadoPartida.NO_INICIADA;
      default ->
        estadoPartida;
    };
  }

  private List<Jugador> convertirJugadores(List<JugadorMensaje> mensajes) {
    List<Jugador> resultado = new ArrayList<>();
    for (JugadorMensaje mensaje : mensajes) {
      List<Carta> cartas = new ArrayList<>();
      Map<Integer, Carta> cartasPorId = new HashMap<>();
      for (CartaMensaje cartaMensaje : mensaje.cartas()) {
        Carta carta = Mazo.crearCarta(cartaMensaje.nombre());
        cartas.add(carta);
        cartasPorId.put(cartaMensaje.id(), carta);
        idsCartas.put(cartaMensaje.nombre(), cartaMensaje.id());
      }
      Jugador jugador = new Jugador(mensaje.nombre(), "", cartas);
      for (Integer id : mensaje.marcadas()) {
        Carta carta = cartasPorId.get(id);
        if (carta != null) {
          jugador.marcarCasilla(carta);
        }
      }
      jugador.agregarPuntos(mensaje.aciertos());
      resultado.add(jugador);
    }
    return List.copyOf(resultado);
  }

  private List<Carta> convertirCartas(List<CartaMensaje> mensajes) {
    List<Carta> resultado = new ArrayList<>();
    for (CartaMensaje mensaje : mensajes) {
      resultado.add(Mazo.crearCarta(mensaje.nombre()));
      idsCartas.put(mensaje.nombre(), mensaje.id());
    }
    return List.copyOf(resultado);
  }

  private int idDe(Carta carta) {
    if (carta == null) {
      return 0;
    }
    Integer id = idsCartas.get(carta.obtenerNombre());
    return id == null ? 0 : id;
  }

  private boolean fueCantada(Carta carta) {
    return cartasCantadas.contains(carta);
  }

  private boolean puedeJugar(Jugador jugador) {
    return estadoPartida == EstadoPartida.EN_CURSO
      && jugador != null
      && jugador.obtenerNombre().equals(jugadorLocal);
  }

  private void enviar(ComandoJuego comando) {
    if (!dispatcher.estaActivo()) {
      return;
    }
    byte[] datos = serializador.serializar(new MensajeBroker(temaComandos, comando));
    dispatcher.enviar(hostBroker, puertoBroker, datos);
  }

  private void notificarOyentes() {
    for (Runnable oyente : oyentes) {
      oyente.run();
    }
  }
}
