package juego.modelo;

import java.util.ArrayList;
import java.util.List;
import juego.fachada.Fachada;
import juego.observador.Observador;

public class ModeloJuego implements Modelo {

  private final String jugadorLocal;
  private final Fachada fachada;
  private final List<Observador> observadores = new ArrayList<>();

  private List<Jugador> jugadores = List.of();
  private List<Carta> cartasCantadas = List.of();
  private int restantes;
  private EstadoPartida estadoPartida = EstadoPartida.NO_INICIADA;
  private String ganador = "";
  private String ultimoError = "";
  private TipoMensaje mensaje;

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
    try {
      fachada.cerrar();
    } catch (RuntimeException e) {
      registrarError("No se pudo cerrar la partida: " + e.getMessage());
    }
  }

  public void actualizar(
    EstadoPartida estado,
    List<Jugador> jugadores,
    List<Carta> cartasCantadas,
    int restantes,
    String ganador
  ) {
    this.estadoPartida = estado == null ? EstadoPartida.NO_INICIADA : estado;
    this.jugadores = jugadores == null ? List.of() : List.copyOf(jugadores);
    this.cartasCantadas = cartasCantadas == null ? List.of() : List.copyOf(cartasCantadas);
    this.restantes = Math.max(0, restantes);
    this.ganador = ganador == null ? "" : ganador;
    this.ultimoError = "";
    this.mensaje = null;
    notificarObservadores();
  }

  public void registrarError(String mensaje) {
    this.ultimoError = mensaje == null ? "" : mensaje;
    notificarObservadores(TipoMensaje.ERROR);
  }

  public void seleccionarCasilla(Carta carta, Jugador jugador) {
    if (esCasillaValida(carta, jugador)) {
      try {
        fachada.seleccionarCasilla(carta.obtenerId());
      } catch (RuntimeException e) {
        registrarError("No se pudo enviar la jugada: " + e.getMessage());
      }
      return;
    }
    notificarObservadores(TipoMensaje.CASILLA_INVALIDA);
  }

  public void seleccionarPatron(Jugador jugador, Patron patron) {
    boolean valido = puedeEnviarPatron(jugador, patron);
    if (valido) {
      try {
        fachada.seleccionarPatron(patron);
      } catch (RuntimeException e) {
        registrarError("No se pudo enviar el patrón: " + e.getMessage());
        return;
      }
    }
    notificarObservadores(valido
      ? TipoMensaje.PATRON_VALIDO : TipoMensaje.PATRON_INVALIDO);
  }

  public void seleccionarVictoria(Jugador jugador, Patron patron) {
    boolean valido = puedeEnviarPatron(jugador, patron);
    if (valido) {
      try {
        fachada.seleccionarVictoria(patron);
      } catch (RuntimeException e) {
        registrarError("No se pudo enviar la victoria: " + e.getMessage());
        return;
      }
    }
    notificarObservadores(valido
      ? TipoMensaje.VICTORIA_VALIDA : TipoMensaje.VICTORIA_INVALIDA);
  }

  private boolean esCasillaValida(Carta carta, Jugador jugador) {
    if (!puedeJugar(jugador) || carta == null) {
      return false;
    }
    if (!jugador.obtenerCartas().contains(carta)) {
      return false;
    }
    if (!cartasCantadas.contains(carta)) {
      return false;
    }
    if (jugador.estaSeleccionada(carta)) {
      return false;
    }
    return true;
  }

  private boolean puedeEnviarPatron(Jugador jugador, Patron patron) {
    return patron != null && puedeJugar(jugador);
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

  private void notificarObservadores() {
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
