package dominio.fachada;

import comun.evento.EventoCerrarPartida;
import comun.evento.EventoIniciarPartida;
import comun.evento.EventoMarcarCasilla;
import comun.evento.EventoSeleccionarPatron;
import comun.evento.EventoGritarLoteria;
import comun.evento.Evento;
import comun.evento.Instruccion;
import comun.evento.EventoCartaCantada;
import comun.evento.EventoCasillaMarcada;
import comun.evento.EventoError;
import comun.evento.EventoPartidaFinalizada;
import comun.evento.EventoPartidaGanada;
import comun.evento.EventoPartidaIniciada;
import comun.evento.EventoPatronDeclarado;
import comun.evento.EventoPuntajeActualizado;
import comunicacion.api.IDispatcher;
import comunicacion.api.IReceptorExterno;
import dominio.griton.Griton;
import dominio.modelo.Carta;
import dominio.modelo.Casilla;
import dominio.modelo.CatalogoCartas;
import dominio.modelo.Jugador;
import dominio.modelo.Partida;
import dominio.modelo.TipoVictoria;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import comun.serializador.ISerializador;

public class FachadaDominio implements Fachada, IReceptorExterno {

  private static final long INTERVALO_POR_DEFECTO = 3000L;
  private static final TipoVictoria PATRON_VICTORIA = TipoVictoria.TABLA_LLENA;

  private final ISerializador serializador;
  private final IDispatcher dispatcher;
  private final String hostBroker;
  private final int puertoBroker;
  private final long intervaloMs;
  /* Compartido por tableros y baraja: el orden de creación fija la secuencia.
     Todo su uso ocurre bajo candado. */
  private final Random aleatorio;
  private final long esperaMs;
  private final List<String> humanos = new ArrayList<>();
  private final Object candado = new Object();

  private Partida partida;
  private final Griton griton;
  private ScheduledExecutorService temporizadorEspera;
  private ScheduledFuture<?> tareaEspera;
  private volatile boolean activo;

  public FachadaDominio(
    ISerializador serializador,
    IDispatcher dispatcher,
    String hostBroker,
    int puertoBroker,
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
    this.intervaloMs = intervaloMs > 0 ? intervaloMs : INTERVALO_POR_DEFECTO;
    this.aleatorio = new Random(semilla);
    this.esperaMs = Math.max(0L, esperaMs);
    this.griton = new Griton(this, serializador, dispatcher,
      this.hostBroker, this.puertoBroker, this.intervaloMs);
  }

  @Override
  public void iniciar() {
    synchronized (candado) {
      if (activo) {
        return;
      }
      activo = true;
      if (esperaMs > 0) {
        iniciarEspera();
      }
    }
  }

  @Override
  public void detener() {
    synchronized (candado) {
      activo = false;
      griton.detener();
      detenerEspera();
    }
  }

  @Override
  public void alRecibir(byte[] datos) {
    Object sobre;
    try {
      sobre = serializador.deserializar(datos, Object.class);
    } catch (RuntimeException e) {
      publicarErrorSeguro(e);
      return;
    }
    if (!(sobre instanceof Evento evento)) {
      publicarErrorSeguro(new IllegalArgumentException("Mensaje inesperado del broker"));
      return;
    }
    Object contenido = evento.payload();
    synchronized (candado) {
      if (!activo) {
        return;
      }
      if (contenido instanceof EventoIniciarPartida inicio) {
        if (partida != null) {
          publicar(eventoPartidaIniciada());
          List<Carta> cantadas = partida.obtenerCartasCantadas();
          int total = partida.obtenerRestantes() + cantadas.size();
          for (int i = 0; i < cantadas.size(); i++) {
            publicar(new EventoCartaCantada(cantadas.get(i).obtenerId(), total - i - 1));
          }
          for (Jugador jugador : partida.obtenerJugadores()) {
            for (Carta marcada : jugador.obtenerTablero().obtenerCartasMarcadas()) {
              publicar(new EventoCasillaMarcada(jugador.obtenerNombre(), marcada.obtenerId()));
            }
            publicar(new EventoPuntajeActualizado(jugador.obtenerNombre(), jugador.obtenerPuntos()));
          }
          partida.obtenerGanador()
            .ifPresent(ganador -> publicar(new EventoPartidaGanada(ganador.obtenerNombre())));
          if (partida.estaFinalizada()) {
            publicar(new EventoPartidaFinalizada());
          }
        } else if (esperaMs > 0) {
          String nombre = inicio.jugador();
          if (nombre == null || nombre.isBlank()) {
            publicar(new EventoError("El nombre del jugador no puede estar vacío"));
            return;
          }
          String limpio = nombre.trim();
          if (!contieneNombre(humanos, limpio)) {
            humanos.add(limpio);
            System.out.println("Jugador conectado: " + limpio
              + " (" + humanos.size() + " en espera)");
          }
        } else {
          String jugador = inicio.jugador();
          if (jugador == null || jugador.isBlank()
            || !contieneNombre(humanos, jugador)) {
            publicar(new EventoError("El jugador no participa en la partida"));
            return;
          }
          crearPartida(new ArrayList<>(humanos));
        }
      } else if (contenido instanceof EventoMarcarCasilla marca) {
        if (partida == null || !partida.estaEnCurso()) {
          publicar(new EventoError("La partida no está en curso"));
          return;
        }
        try {
          Carta carta = CatalogoCartas.obtenerPorId(marca.cartaId());
          Jugador jugador = buscarJugador(marca.jugador());
          if (jugador == null) {
            publicar(new EventoError("El jugador no participa en la partida"));
            return;
          }
          if (!partida.marcar(jugador.obtenerId(), carta)) {
            publicar(new EventoError("La casilla ya estaba marcada"));
            return;
          }
          publicar(new EventoCasillaMarcada(jugador.obtenerNombre(), carta.obtenerId()));
          publicar(new EventoPuntajeActualizado(
            jugador.obtenerNombre(), jugador.obtenerPuntos()));
        } catch (RuntimeException e) {
          publicarErrorSeguro(e);
        }
      } else if (contenido instanceof EventoSeleccionarPatron seleccion) {
        if (partida == null || !partida.estaEnCurso()) {
          publicar(new EventoError("La partida no está en curso"));
          return;
        }
        Jugador jugador = buscarJugador(seleccion.jugador());
        if (jugador == null) {
          publicar(new EventoError("El jugador no participa en la partida"));
          return;
        }
        try {
          TipoVictoria tipo = convertirTipoVictoria(seleccion.patron());
          if (!partida.declararPatron(jugador.obtenerId(), tipo)) {
            publicar(new EventoError("El patrón " + tipo.obtenerDescripcion()
              + " no está completo o ya fue declarado"));
            return;
          }
          publicar(new EventoPatronDeclarado(jugador.obtenerNombre(), tipo.name()));
          publicar(new EventoPuntajeActualizado(
            jugador.obtenerNombre(), jugador.obtenerPuntos()));
        } catch (RuntimeException e) {
          publicarErrorSeguro(e);
        }
      } else if (contenido instanceof EventoGritarLoteria grito) {
        if (partida == null || !partida.estaEnCurso()) {
          publicar(new EventoError("La partida no está en curso"));
          return;
        }
        Jugador jugador = buscarJugador(grito.jugador());
        if (jugador == null) {
          publicar(new EventoError("El jugador no participa en la partida"));
          return;
        }
        try {
          if (!partida.gritarLoteria(jugador.obtenerId())) {
            publicar(new EventoError("Todavía no completas el patrón "
              + partida.obtenerTipoVictoria().obtenerDescripcion()));
            publicar(new EventoPuntajeActualizado(
              jugador.obtenerNombre(), jugador.obtenerPuntos()));
            return;
          }
          griton.detener();
          publicar(new EventoPuntajeActualizado(
            jugador.obtenerNombre(), jugador.obtenerPuntos()));
          publicar(new EventoPartidaGanada(jugador.obtenerNombre()));
        } catch (RuntimeException e) {
          publicarErrorSeguro(e);
        }
      } else if (contenido instanceof EventoCerrarPartida) {
        finalizarPartida();
      } else {
        publicar(new EventoError("Evento desconocido: " + contenido.getClass().getSimpleName()));
      }
    }
  }

  @Override
  public boolean estaActivo() {
    return activo;
  }

  private void iniciarEspera() {
    detenerEspera();
    temporizadorEspera = crearPlanificador("dominio-espera");
    tareaEspera = temporizadorEspera.schedule(() -> {
      synchronized (candado) {
        if (!activo || partida != null) {
          return;
        }
        try {
          if (humanos.isEmpty()) {
            System.out.println("No hay jugadores conectados; se reinicia la espera");
            iniciarEspera();
            return;
          }
          System.out.println("Tiempo de espera terminado. Iniciando partida con: "
            + humanos);
          crearPartida(new ArrayList<>(humanos));
        } catch (RuntimeException e) {
          publicarErrorSeguro(e);
          iniciarEspera();
        }
      }
    }, esperaMs, TimeUnit.MILLISECONDS);
    System.out.println("Esperando " + (esperaMs / 1000)
      + " s para que se conecten los clientes...");
  }

  private void crearPartida(List<String> jugadores) {
    detenerEspera();
    partida = Partida.crear(jugadores, PATRON_VICTORIA, aleatorio);
    partida.iniciar();
    griton.iniciar();
    publicar(eventoPartidaIniciada());
  }

  private ScheduledExecutorService crearPlanificador(String nombre) {
    return Executors.newSingleThreadScheduledExecutor(tarea -> {
      Thread hilo = new Thread(tarea, nombre);
      hilo.setDaemon(true);
      return hilo;
    });
  }

  private void detenerEspera() {
    if (tareaEspera != null) {
      tareaEspera.cancel(false);
      tareaEspera = null;
    }
    if (temporizadorEspera != null) {
      temporizadorEspera.shutdown();
      temporizadorEspera = null;
    }
  }

  @Override
  public Optional<CartaCantada> cantarSiguienteCarta() {
    synchronized (candado) {
      if (!activo || partida == null || !partida.estaEnCurso()) {
        return Optional.empty();
      }
      if (!partida.puedeCantar()) {
        finalizarPartida();
        return Optional.empty();
      }
      Carta carta = partida.cantarSiguiente();
      return Optional.of(new CartaCantada(carta.obtenerId(), partida.obtenerRestantes()));
    }
  }

  private void finalizarPartida() {
    if (partida == null || partida.estaFinalizada()) {
      return;
    }
    griton.detener();
    if (partida.obtenerRestantes() == 0 && partida.estaEnCurso()) {
      partida.cerrarPorBarajaAgotada();
    } else {
      partida.finalizar();
    }
    publicar(new EventoPartidaFinalizada());
  }

  private TipoVictoria convertirTipoVictoria(String patron) {
    if (patron == null || patron.isBlank()) {
      throw new IllegalArgumentException("El patrón no puede estar vacío");
    }
    try {
      return TipoVictoria.valueOf(patron.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("El patrón no es válido: " + patron, e);
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

  private void publicarErrorSeguro(RuntimeException e) {
    try {
      String mensaje = e.getMessage();
      publicar(new EventoError(mensaje == null || mensaje.isBlank()
        ? "Error inesperado del dominio" : mensaje));
    } catch (RuntimeException ignorada) {
      System.err.println("No se pudo publicar el error: " + ignorada.getMessage());
    }
  }

  private void publicar(Instruccion contenido) {
    byte[] datos = serializador.serializar(new Evento(contenido));
    dispatcher.enviar(hostBroker, puertoBroker, datos);
  }

  private EventoPartidaIniciada eventoPartidaIniciada() {
    List<String> nombres = new ArrayList<>();
    List<List<Integer>> cartasPorJugador = new ArrayList<>();
    if (partida != null) {
      for (Jugador jugador : partida.obtenerJugadores()) {
        nombres.add(jugador.obtenerNombre());
        List<Integer> cartas = new ArrayList<>();
        for (Casilla casilla : jugador.obtenerTablero().obtenerCasillas()) {
          cartas.add(casilla.obtenerCarta().obtenerId());
        }
        cartasPorJugador.add(cartas);
      }
    }
    return new EventoPartidaIniciada(
      nombres, cartasPorJugador, partida == null ? 0 : partida.obtenerRestantes());
  }
}
