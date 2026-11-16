package dominio;

import comun.CartaMensaje;
import comun.ComandoJuego;
import comun.EventoPartida;
import comun.JugadorMensaje;
import comun.MensajeBroker;
import comunicacion.Dispatcher;
import comunicacion.Receptor;
import comunicacion.api.IReceptorExterno;
import dominio.modelo.Carta;
import dominio.modelo.Casilla;
import dominio.modelo.CatalogoCartas;
import dominio.modelo.Jugador;
import dominio.modelo.Partida;
import dominio.modelo.TipoVictoria;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import serializador.ISerializador;

public class Dominio implements IReceptorExterno {

  private static final long INTERVALO_POR_DEFECTO = 3000L;
  private static final String PATRON_POR_DEFECTO = "LINEA";
  private static final int JUGADORES = 3;

  private final ISerializador serializador;
  private final Dispatcher dispatcher;
  private final Receptor receptor;
  private final String hostBroker;
  private final int puertoBroker;
  private final String temaComandos;
  private final String temaEventos;
  private final String jugadorLocal;
  private final long intervaloMs;
  private final Random aleatorio;
  private final Object candado = new Object();

  private Partida partida;
  private String jugadorPrincipal;
  private String patronActual = PATRON_POR_DEFECTO;
  private Timer temporizador;
  private boolean activo;

  public Dominio(
    ISerializador serializador,
    Dispatcher dispatcher,
    Receptor receptor,
    String hostBroker,
    int puertoBroker,
    String temaComandos,
    String temaEventos,
    String jugadorLocal,
    long intervaloMs
  ) {
    this(serializador, dispatcher, receptor, hostBroker, puertoBroker, temaComandos,
      temaEventos, jugadorLocal, intervaloMs, new Random().nextLong());
  }

  public Dominio(
    ISerializador serializador,
    Dispatcher dispatcher,
    Receptor receptor,
    String hostBroker,
    int puertoBroker,
    String temaComandos,
    String temaEventos,
    String jugadorLocal,
    long intervaloMs,
    long semilla
  ) {
    if (serializador == null) {
      throw new IllegalArgumentException("El serializador no puede ser nulo");
    }
    if (dispatcher == null) {
      throw new IllegalArgumentException("El dispatcher no puede ser nulo");
    }
    if (receptor == null) {
      throw new IllegalArgumentException("El receptor no puede ser nulo");
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
    if (jugadorLocal == null || jugadorLocal.isBlank()) {
      throw new IllegalArgumentException("El jugador local no puede estar vacío");
    }
    this.serializador = serializador;
    this.dispatcher = dispatcher;
    this.receptor = receptor;
    this.hostBroker = hostBroker.trim();
    this.puertoBroker = puertoBroker;
    this.temaComandos = temaComandos.trim();
    this.temaEventos = temaEventos.trim();
    this.jugadorLocal = jugadorLocal.trim();
    this.jugadorPrincipal = this.jugadorLocal;
    this.intervaloMs = intervaloMs > 0 ? intervaloMs : INTERVALO_POR_DEFECTO;
    this.aleatorio = new Random(semilla);
    this.receptor.agregarReceptor(this);
  }

  public void iniciar(int puertoLocal) {
    if (puertoLocal < 0 || puertoLocal > 65535) {
      throw new IllegalArgumentException("El puerto local no es válido: " + puertoLocal);
    }
    if (activo) {
      return;
    }
    dispatcher.iniciar();
    receptor.iniciar(puertoLocal);
    if (!receptor.estaActivo()) {
      dispatcher.detener();
      throw new IllegalStateException(
        "No se pudo iniciar el dominio en el puerto " + puertoLocal);
    }
    activo = true;
  }

  public void detener() {
    synchronized (candado) {
      activo = false;
      detenerTemporizador();
    }
    receptor.detener();
    dispatcher.detener();
  }

  @Override
  public void alRecibir(byte[] datos) {
    Object objeto = serializador.deserializar(datos, Object.class);
    if (!(objeto instanceof ComandoJuego comando)) {
      return;
    }
    synchronized (candado) {
      if (!activo) {
        return;
      }
      switch (comando.accion()) {
        case ComandoJuego.INICIAR ->
          iniciarPartida(comando.jugador());
        case ComandoJuego.MARCAR ->
          marcar(comando.jugador(), comando.cartaId());
        case ComandoJuego.PATRON ->
          cambiarPatron(comando.patron());
        case ComandoJuego.VICTORIA ->
          gritarVictoria(comando.jugador(), comando.patron());
        case ComandoJuego.CERRAR ->
          cerrarPartida();
        default ->
          publicar(EventoPartida.ERROR, "Comando desconocido: " + comando.accion());
      }
    }
  }

  public boolean estaActivo() {
    return activo;
  }

  public String obtenerTemaComandos() {
    return temaComandos;
  }

  public String obtenerTemaEventos() {
    return temaEventos;
  }

  private void iniciarPartida(String jugador) {
    if (partida != null && !partida.estaFinalizada()) {
      publicar(EventoPartida.SINCRONIZACION, "");
      return;
    }
    if (jugador != null && !jugador.isBlank()) {
      jugadorPrincipal = jugador.trim();
    }
    List<String> nombres = new ArrayList<>();
    nombres.add(jugadorPrincipal);
    for (int i = 1; nombres.size() < JUGADORES; i++) {
      String nombre = "Oponente " + i;
      boolean repetido = false;
      for (String existente : nombres) {
        if (existente.equalsIgnoreCase(nombre)) {
          repetido = true;
          break;
        }
      }
      if (!repetido) {
        nombres.add(nombre);
      }
    }
    partida = Partida.crear(nombres, TipoVictoria.LINEA, aleatorio);
    partida.iniciar();
    patronActual = PATRON_POR_DEFECTO;
    detenerTemporizador();
    publicar(EventoPartida.SINCRONIZACION, "");
    cantarSiguiente();
    if (partida.estaEnCurso() && partida.puedeCantar()) {
      temporizador = new Timer("dominio-cartas", true);
      temporizador.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          synchronized (candado) {
            if (activo) {
              cantarSiguiente();
            }
          }
        }
      }, intervaloMs, intervaloMs);
    }
  }

  private void cantarSiguiente() {
    if (partida == null || !partida.estaEnCurso()) {
      detenerTemporizador();
      return;
    }
    if (!partida.puedeCantar()) {
      finalizarPorAgotamiento();
      return;
    }
    Carta carta = partida.cantarSiguiente();
    marcarOponentes(carta);
    publicar(EventoPartida.CARTA_CANTADA, "");
    if (!partida.puedeCantar()) {
      finalizarPorAgotamiento();
    }
  }

  private void marcarOponentes(Carta carta) {
    for (Jugador jugador : partida.obtenerJugadores()) {
      if (jugador.obtenerNombre().equals(jugadorPrincipal)) {
        continue;
      }
      if (!jugador.obtenerTabla().contiene(carta)) {
        continue;
      }
      try {
        partida.marcar(jugador, carta);
      } catch (RuntimeException ignored) {
      }
    }
  }

  private void marcar(String nombreJugador, int cartaId) {
    if (partida == null || !partida.estaEnCurso()) {
      publicar(EventoPartida.ERROR, "La partida no está en curso");
      return;
    }
    try {
      Carta carta = CatalogoCartas.obtenerPorId(cartaId);
      Jugador jugador = buscarJugador(nombreJugador);
      if (jugador == null) {
        publicar(EventoPartida.ERROR, "El jugador no participa en la partida");
        return;
      }
      if (!partida.marcar(jugador, carta)) {
        publicar(EventoPartida.ERROR, "La casilla ya estaba marcada");
        return;
      }
      publicar(EventoPartida.SINCRONIZACION, "");
    } catch (RuntimeException e) {
      publicar(EventoPartida.ERROR, e.getMessage());
    }
  }

  private void cambiarPatron(String patron) {
    if (patron != null && !patron.isBlank()) {
      patronActual = patron.trim().toUpperCase(Locale.ROOT);
    }
    publicar(EventoPartida.SINCRONIZACION, "");
  }

  private void gritarVictoria(String nombreJugador, String patron) {
    if (partida == null || !partida.estaEnCurso()) {
      publicar(EventoPartida.ERROR, "La partida no está en curso");
      return;
    }
    Jugador jugador = buscarJugador(nombreJugador);
    if (jugador == null) {
      publicar(EventoPartida.ERROR, "El jugador no participa en la partida");
      return;
    }
    try {
      if (!partida.gritarLoteria(jugador, convertirTipoVictoria(patron))) {
        publicar(EventoPartida.ERROR, "La victoria no es válida");
        return;
      }
      if (patron != null && !patron.isBlank()) {
        patronActual = patron.trim().toUpperCase(Locale.ROOT);
      }
      detenerTemporizador();
      publicar(EventoPartida.VICTORIA, "");
    } catch (RuntimeException e) {
      publicar(EventoPartida.ERROR, e.getMessage());
    }
  }

  private void cerrarPartida() {
    detenerTemporizador();
    if (partida != null && partida.estaEnCurso()) {
      partida.finalizar();
    }
    publicar(EventoPartida.FIN, "");
  }

  private void finalizarPorAgotamiento() {
    detenerTemporizador();
    if (partida != null && partida.estaEnCurso()) {
      partida.finalizar();
    }
    publicar(EventoPartida.FIN, "");
  }

  private TipoVictoria convertirTipoVictoria(String patron) {
    if (patron == null) {
      return TipoVictoria.LINEA;
    }
    return switch (patron.trim().toUpperCase(Locale.ROOT)) {
      case "CUATRO_ESQUINAS" ->
        TipoVictoria.CUATRO_ESQUINAS;
      case "TABLA_LLENA" ->
        TipoVictoria.TABLA_LLENA;
      default ->
        TipoVictoria.LINEA;
    };
  }

  private Jugador buscarJugador(String nombre) {
    if (nombre == null || nombre.isBlank()) {
      return null;
    }
    String buscado = nombre.trim();
    for (Jugador jugador : partida.obtenerJugadores()) {
      if (jugador.obtenerNombre().equalsIgnoreCase(buscado)) {
        return jugador;
      }
    }
    return null;
  }

  private void publicar(String tipo, String mensaje) {
    EventoPartida evento = construirEvento(tipo, mensaje == null ? "" : mensaje);
    byte[] datos = serializador.serializar(new MensajeBroker(temaEventos, evento));
    dispatcher.enviar(hostBroker, puertoBroker, datos);
  }

  private EventoPartida construirEvento(String tipo, String mensaje) {
    if (partida == null) {
      return new EventoPartida(tipo, "", "", patronActual, new ArrayList<>(),
        new ArrayList<>(), 0, "", mensaje);
    }
    List<JugadorMensaje> jugadores = new ArrayList<>();
    for (Jugador jugador : partida.obtenerJugadores()) {
      jugadores.add(convertirJugador(jugador));
    }
    List<CartaMensaje> cantadas = new ArrayList<>();
    for (Carta carta : partida.obtenerCartasCantadas()) {
      cantadas.add(convertirCarta(carta));
    }
    String ganador = partida.obtenerGanadores().isEmpty()
      ? "" : partida.obtenerGanadores().get(0).obtenerNombre();
    return new EventoPartida(
      tipo,
      partida.obtenerId(),
      partida.obtenerEstado().name(),
      patronActual,
      jugadores,
      cantadas,
      partida.obtenerCartasRestantes().size(),
      ganador,
      mensaje
    );
  }

  private JugadorMensaje convertirJugador(Jugador jugador) {
    List<CartaMensaje> cartas = new ArrayList<>();
    for (Casilla casilla : jugador.obtenerTabla().obtenerCasillas()) {
      cartas.add(convertirCarta(casilla.obtenerCarta()));
    }
    List<Integer> marcadas = new ArrayList<>();
    for (Carta carta : jugador.obtenerTabla().obtenerCartasMarcadas()) {
      marcadas.add(carta.obtenerId());
    }
    return new JugadorMensaje(
      jugador.obtenerId(),
      jugador.obtenerNombre(),
      cartas,
      marcadas,
      jugador.obtenerAciertos()
    );
  }

  private CartaMensaje convertirCarta(Carta carta) {
    return new CartaMensaje(carta.obtenerId(), carta.obtenerNombre());
  }

  private void detenerTemporizador() {
    if (temporizador != null) {
      temporizador.cancel();
      temporizador = null;
    }
  }
}
