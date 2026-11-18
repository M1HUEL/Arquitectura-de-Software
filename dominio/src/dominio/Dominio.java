package dominio;

import comun.CartaMensaje;
import comun.ComandoCerrar;
import comun.ComandoIniciar;
import comun.ComandoMarcar;
import comun.ComandoPatron;
import comun.ComandoVictoria;
import comun.EstadoJuego;
import comun.EventoBroker;
import comun.EventoCartaCantada;
import comun.EventoError;
import comun.EventoFin;
import comun.EventoSincronizacion;
import comun.EventoVictoria;
import comun.JugadorMensaje;
import comunicacion.api.IDispatcher;
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
  private static final String PATRON_POR_DEFECTO = "CHORRO";
  private static final int JUGADORES_MINIMOS = 3;

  private final ISerializador serializador;
  private final IDispatcher dispatcher;
  private final String hostBroker;
  private final int puertoBroker;
  private final String temaComandos;
  private final String temaEventos;
  private final long intervaloMs;
  private final Random aleatorio;
  private final long esperaMs;
  private final List<String> humanos = new ArrayList<>();
  private final Object candado = new Object();

  private Partida partida;
  private String patronActual = PATRON_POR_DEFECTO;
  private Timer temporizador;
  private Timer temporizadorEspera;
  private boolean activo;

  public Dominio(
    ISerializador serializador,
    IDispatcher dispatcher,
    String hostBroker,
    int puertoBroker,
    String temaComandos,
    String temaEventos,
    String jugadorLocal,
    long intervaloMs
  ) {
    this(serializador, dispatcher, hostBroker, puertoBroker, temaComandos,
      temaEventos, List.of(jugadorLocal), intervaloMs);
  }

  public Dominio(
    ISerializador serializador,
    IDispatcher dispatcher,
    String hostBroker,
    int puertoBroker,
    String temaComandos,
    String temaEventos,
    String jugadorLocal,
    long intervaloMs,
    long semilla
  ) {
    this(serializador, dispatcher, hostBroker, puertoBroker, temaComandos,
      temaEventos, List.of(jugadorLocal), intervaloMs, semilla);
  }

  public Dominio(
    ISerializador serializador,
    IDispatcher dispatcher,
    String hostBroker,
    int puertoBroker,
    String temaComandos,
    String temaEventos,
    List<String> jugadoresHumanos,
    long intervaloMs
  ) {
    this(serializador, dispatcher, hostBroker, puertoBroker, temaComandos,
      temaEventos, jugadoresHumanos, intervaloMs, new Random().nextLong());
  }

  public Dominio(
    ISerializador serializador,
    IDispatcher dispatcher,
    String hostBroker,
    int puertoBroker,
    String temaComandos,
    String temaEventos,
    List<String> jugadoresHumanos,
    long intervaloMs,
    long semilla
  ) {
    this(serializador, dispatcher, hostBroker, puertoBroker, temaComandos,
      temaEventos, jugadoresHumanos, intervaloMs, semilla, 0L);
  }

  public Dominio(
    ISerializador serializador,
    IDispatcher dispatcher,
    String hostBroker,
    int puertoBroker,
    String temaComandos,
    String temaEventos,
    List<String> jugadoresHumanos,
    long intervaloMs,
    long semilla,
    long esperaMs
  ) {
    if (serializador == null) {
      throw new IllegalArgumentException("El serializador no puede ser nulo");
    }
    if (dispatcher == null) {
      throw new IllegalArgumentException("El dispatcher no puede ser nulo");
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
    if (jugadoresHumanos == null) {
      throw new IllegalArgumentException("La lista de jugadores no puede ser nula");
    }
    if (esperaMs <= 0 && jugadoresHumanos.isEmpty()) {
      throw new IllegalArgumentException("Debe haber al menos un jugador humano");
    }
    for (String nombre : jugadoresHumanos) {
      if (nombre == null || nombre.isBlank()) {
        throw new IllegalArgumentException("El nombre de un jugador no puede estar vacío");
      }
      String limpio = nombre.trim();
      if (!contieneNombre(this.humanos, limpio)) {
        this.humanos.add(limpio);
      }
    }
    this.serializador = serializador;
    this.dispatcher = dispatcher;
    this.hostBroker = hostBroker.trim();
    this.puertoBroker = puertoBroker;
    this.temaComandos = temaComandos.trim();
    this.temaEventos = temaEventos.trim();
    this.intervaloMs = intervaloMs > 0 ? intervaloMs : INTERVALO_POR_DEFECTO;
    this.aleatorio = new Random(semilla);
    this.esperaMs = Math.max(0L, esperaMs);
  }

  public void iniciar() {
    if (activo) {
      return;
    }
    activo = true;
    if (esperaMs > 0) {
      iniciarEspera();
    }
  }

  public void detener() {
    synchronized (candado) {
      activo = false;
      detenerTemporizador();
      detenerEspera();
    }
  }

  @Override
  public void alRecibir(byte[] datos) {
    Object contenido = serializador.deserializar(datos, Object.class);
    synchronized (candado) {
      if (!activo) {
        return;
      }
      procesar(contenido);
    }
  }

  private void procesar(Object contenido) {
    if (contenido instanceof ComandoIniciar comando) {
      if (esperaMs > 0 && (partida == null || partida.estaFinalizada())) {
        registrarJugador(comando.jugador());
      } else {
        iniciarPartida(comando.jugador());
      }
    } else if (contenido instanceof ComandoMarcar comando) {
      marcar(comando.jugador(), comando.cartaId());
    } else if (contenido instanceof ComandoPatron comando) {
      cambiarPatron(comando.patron());
    } else if (contenido instanceof ComandoVictoria comando) {
      gritarVictoria(comando.jugador(), comando.patron());
    } else if (contenido instanceof ComandoCerrar) {
      finalizarPartida();
    } else {
      publicar(new EventoError("Comando desconocido: " + contenido.getClass().getSimpleName()));
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
      publicar(new EventoSincronizacion(estadoJuego()));
      return;
    }
    if (jugador == null || jugador.isBlank()
      || !contieneNombre(humanos, jugador)) {
      publicar(new EventoError("El jugador no participa en la partida"));
      return;
    }
    crearPartida(new ArrayList<>(humanos), true);
  }

  private void registrarJugador(String jugador) {
    if (jugador == null || jugador.isBlank()) {
      publicar(new EventoError("El nombre del jugador no puede estar vacío"));
      return;
    }
    synchronized (candado) {
      String limpio = jugador.trim();
      if (!contieneNombre(humanos, limpio)) {
        humanos.add(limpio);
        System.out.println("Jugador conectado: " + limpio
          + " (" + humanos.size() + " en espera)");
      }
    }
    publicar(new EventoSincronizacion(estadoJuego()));
  }

  private void iniciarEspera() {
    detenerEspera();
    temporizadorEspera = new Timer("dominio-espera", true);
    temporizadorEspera.schedule(new TimerTask() {
      @Override
      public void run() {
        synchronized (candado) {
          if (!activo) {
            return;
          }
          if (humanos.isEmpty()) {
            System.out.println("No hay jugadores conectados; se reinicia la espera");
            iniciarEspera();
            return;
          }
          System.out.println("Tiempo de espera terminado. Iniciando partida con: "
            + humanos);
          crearPartida(new ArrayList<>(humanos), false);
        }
      }
    }, esperaMs);
    System.out.println("Esperando " + (esperaMs / 1000)
      + " s para que se conecten los clientes...");
  }

  private void crearPartida(List<String> jugadores, boolean completarConOponentes) {
    detenerEspera();
    List<String> nombres = new ArrayList<>(jugadores);
    if (completarConOponentes) {
      for (int i = 1; nombres.size() < JUGADORES_MINIMOS; i++) {
        String nombre = "Oponente " + i;
        if (!contieneNombre(nombres, nombre)) {
          nombres.add(nombre);
        }
      }
    }
    partida = Partida.crear(nombres, TipoVictoria.CHORRO, aleatorio);
    partida.iniciar();
    patronActual = PATRON_POR_DEFECTO;
    detenerTemporizador();
    publicar(new EventoSincronizacion(estadoJuego()));
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

  private void detenerEspera() {
    if (temporizadorEspera != null) {
      temporizadorEspera.cancel();
      temporizadorEspera = null;
    }
  }

  private void cantarSiguiente() {
    if (partida == null || !partida.estaEnCurso()) {
      detenerTemporizador();
      return;
    }
    if (!partida.puedeCantar()) {
      finalizarPartida();
      return;
    }
    Carta carta = partida.cantarSiguiente();
    marcarOponentes(carta);
    publicar(new EventoCartaCantada(estadoJuego()));
    if (!partida.puedeCantar()) {
      finalizarPartida();
    }
  }

  private void marcarOponentes(Carta carta) {
    for (Jugador jugador : partida.obtenerJugadores()) {
      if (contieneNombre(humanos, jugador.obtenerNombre())) {
        continue;
      }
      if (!jugador.obtenerTabla().contiene(carta)) {
        continue;
      }
      try {
        partida.marcar(jugador, carta);
      } catch (RuntimeException e) {
        System.err.println("No se pudo marcar al oponente " + jugador.obtenerNombre()
          + ": " + e.getMessage());
      }
    }
  }

  private void marcar(String nombreJugador, int cartaId) {
    if (partida == null || !partida.estaEnCurso()) {
      publicar(new EventoError("La partida no está en curso"));
      return;
    }
    try {
      Carta carta = CatalogoCartas.obtenerPorId(cartaId);
      Jugador jugador = buscarJugador(nombreJugador);
      if (jugador == null) {
        publicar(new EventoError("El jugador no participa en la partida"));
        return;
      }
      if (!partida.marcar(jugador, carta)) {
        publicar(new EventoError("La casilla ya estaba marcada"));
        return;
      }
      publicar(new EventoSincronizacion(estadoJuego()));
    } catch (RuntimeException e) {
      publicarError(e);
    }
  }

  private void cambiarPatron(String patron) {
    patronActual = convertirTipoVictoria(patron).name();
    publicar(new EventoSincronizacion(estadoJuego()));
  }

  private void gritarVictoria(String nombreJugador, String patron) {
    if (partida == null || !partida.estaEnCurso()) {
      publicar(new EventoError("La partida no está en curso"));
      return;
    }
    Jugador jugador = buscarJugador(nombreJugador);
    if (jugador == null) {
      publicar(new EventoError("El jugador no participa en la partida"));
      return;
    }
    try {
      TipoVictoria tipo = convertirTipoVictoria(patron);
      if (!partida.gritarLoteria(jugador, tipo)) {
        publicar(new EventoError("La victoria no es válida"));
        return;
      }
      patronActual = tipo.name();
      detenerTemporizador();
      publicar(new EventoVictoria(estadoJuego()));
    } catch (RuntimeException e) {
      publicarError(e);
    }
  }

  private void finalizarPartida() {
    detenerTemporizador();
    if (partida != null && partida.estaEnCurso()) {
      partida.finalizar();
    }
    publicar(new EventoFin(estadoJuego()));
  }

  private TipoVictoria convertirTipoVictoria(String patron) {
    if (patron == null || patron.isBlank()) {
      return TipoVictoria.CHORRO;
    }
    try {
      return TipoVictoria.valueOf(patron.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      return TipoVictoria.CHORRO;
    }
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

  private boolean contieneNombre(List<String> nombres, String nombre) {
    if (nombres == null || nombre == null) {
      return false;
    }
    String buscado = nombre.trim();
    for (String existente : nombres) {
      if (existente.equalsIgnoreCase(buscado)) {
        return true;
      }
    }
    return false;
  }

  private void publicarError(RuntimeException e) {
    String mensaje = e.getMessage();
    publicar(new EventoError(mensaje == null || mensaje.isBlank()
      ? "Error inesperado del dominio" : mensaje));
  }

  private void publicar(Object evento) {
    byte[] datos = serializador.serializar(new EventoBroker(temaEventos, evento));
    dispatcher.enviar(hostBroker, puertoBroker, datos);
  }

  private EstadoJuego estadoJuego() {
    if (partida == null) {
      return new EstadoJuego("", "", patronActual, List.of(),
        List.of(), 0, "");
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
    return new EstadoJuego(
      partida.obtenerId(),
      partida.obtenerEstado().name(),
      patronActual,
      jugadores,
      cantadas,
      partida.obtenerCartasRestantes().size(),
      ganador
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
