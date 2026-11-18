package juego.modelo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import juego.fachada.Fachada;
import juego.observador.Observador;

public class ModeloJuego implements Modelo {

  private final String jugadorLocal;
  private final Fachada fachada;
  private final List<Observador> observadores = new CopyOnWriteArrayList<>();

  private volatile List<Jugador> jugadores = List.of();
  private volatile List<Carta> cartasCantadas = List.of();
  private volatile int restantes;
  private volatile EstadoPartida estadoPartida = EstadoPartida.NO_INICIADA;
  private volatile String ganador = "";
  private volatile String ultimoError = "";
  private volatile Patron patron = Patron.CHORRO;
  private volatile TipoMensaje mensaje;

  public ModeloJuego(String jugadorLocal, Fachada fachada) {
    if (jugadorLocal == null || jugadorLocal.isBlank()) {
      throw new IllegalArgumentException("El jugador local no puede estar vacío");
    }
    if (fachada == null) {
      throw new IllegalArgumentException("La fachada no puede ser nula");
    }
    this.jugadorLocal = jugadorLocal.trim();
    this.fachada = fachada;
  }

  public void iniciar() {
    fachada.iniciar();
  }

  @Override
  public void cerrar() {
    fachada.cerrar();
  }

  @Override
  public void desconectar() {
    fachada.desconectar();
  }

  public void actualizar(
    EstadoPartida estado,
    Patron patron,
    List<Jugador> jugadores,
    List<Carta> cartasCantadas,
    int restantes,
    String ganador
  ) {
    this.estadoPartida = estado == null ? EstadoPartida.NO_INICIADA : estado;
    this.patron = patron == null ? Patron.CHORRO : patron;
    this.jugadores = jugadores == null ? List.of() : List.copyOf(jugadores);
    this.cartasCantadas = cartasCantadas == null ? List.of() : List.copyOf(cartasCantadas);
    this.restantes = Math.max(0, restantes);
    this.ganador = ganador == null ? "" : ganador;
    this.ultimoError = "";
    notificarObservadores();
  }

  public void registrarError(String mensaje) {
    this.ultimoError = mensaje == null ? "" : mensaje;
    notificarObservadores();
  }

  public void seleccionarCasilla(Carta carta, Jugador jugador) {
    TipoMensaje resultado = validarCasilla(carta, jugador);
    if (resultado == TipoMensaje.CASILLA_VALIDA) {
      fachada.seleccionarCasilla(carta);
    }
    notificarObservadores(resultado);
  }

  public void seleccionarPatron(Jugador jugador, Patron patron) {
    TipoMensaje resultado = validarPatron(jugador, patron);
    if (resultado == TipoMensaje.PATRON_VALIDO) {
      fachada.seleccionarPatron(patron);
    }
    notificarObservadores(resultado);
  }

  public void seleccionarVictoria(Jugador jugador, Patron patron) {
    TipoMensaje resultado = validarVictoria(jugador, patron);
    if (resultado == TipoMensaje.VICTORIA_VALIDA) {
      fachada.seleccionarVictoria(patron);
    }
    notificarObservadores(resultado);
  }

  private TipoMensaje validarCasilla(Carta carta, Jugador jugador) {
    if (!puedeJugar(jugador) || carta == null) {
      return TipoMensaje.CASILLA_INVALIDA;
    }
    if (!jugador.obtenerCartas().contains(carta)) {
      return TipoMensaje.CASILLA_INVALIDA;
    }
    if (!cartasCantadas.contains(carta)) {
      return TipoMensaje.CASILLA_INVALIDA;
    }
    if (jugador.estaSeleccionada(carta)) {
      return TipoMensaje.CASILLA_INVALIDA;
    }
    return TipoMensaje.CASILLA_VALIDA;
  }

  private TipoMensaje validarPatron(Jugador jugador, Patron patron) {
    if (patron == null || !puedeJugar(jugador) || !jugador.cumple(patron)) {
      return TipoMensaje.PATRON_INVALIDO;
    }
    return TipoMensaje.PATRON_VALIDO;
  }

  private TipoMensaje validarVictoria(Jugador jugador, Patron patron) {
    if (patron == null || !puedeJugar(jugador) || !jugador.cumple(patron)) {
      return TipoMensaje.VICTORIA_INVALIDA;
    }
    return TipoMensaje.VICTORIA_VALIDA;
  }

  private boolean puedeJugar(Jugador jugador) {
    return estadoPartida == EstadoPartida.EN_CURSO
      && jugador != null
      && jugador.obtenerNombre().equals(jugadorLocal);
  }

  public void agregarObservador(Observador observador) {
    if (observador != null) {
      observadores.add(observador);
    }
  }

  public void eliminarObservador(Observador observador) {
    observadores.remove(observador);
  }

  public void notificarObservadores() {
    for (Observador observador : observadores) {
      observador.actualizar(this);
    }
  }

  private void notificarObservadores(TipoMensaje resultado) {
    this.mensaje = resultado;
    notificarObservadores();
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

  public Patron obtenerPatron() {
    return patron;
  }

  @Override
  public String obtenerUltimoError() {
    return ultimoError;
  }

  @Override
  public TipoMensaje consumirMensaje() {
    TipoMensaje actual = mensaje;
    mensaje = null;
    return actual;
  }
}
