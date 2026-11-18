package juego.modelo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import juego.fachada.Fachada;
import juego.observador.Observador;

public class ModeloJuego implements Modelo {

  private final String jugadorLocal;
  private final Fachada fachada;
  private final List<Observador> observadores = new CopyOnWriteArrayList<>();
  private volatile Mensaje mensaje;
  private long versionMensaje;
  private final Runnable oyente = this::notificarObservadores;
  private boolean conectado;

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
    if (!conectado) {
      fachada.agregarOyente(oyente);
      conectado = true;
    }
    fachada.iniciar();
  }

  public void cerrar() {
    if (conectado) {
      fachada.eliminarOyente(oyente);
      conectado = false;
    }
    fachada.cerrar();
  }

  public void desconectar() {
    if (conectado) {
      fachada.eliminarOyente(oyente);
      conectado = false;
    }
    fachada.desconectar();
  }

  public void seleccionarCasilla(Carta carta, Jugador jugador) {
    notificarObservadores(fachada.seleccionarCasilla(jugador, carta));
  }

  public void seleccionarPatron(Jugador jugador, Patron patron) {
    notificarObservadores(fachada.seleccionarPatron(jugador, patron));
  }

  public void seleccionarVictoria(Jugador jugador, Patron patron) {
    notificarObservadores(fachada.seleccionarVictoria(jugador, patron));
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

  private void notificarObservadores(Mensaje resultado) {
    this.mensaje = resultado;
    this.versionMensaje++;
    notificarObservadores();
  }

  @Override
  public String obtenerJugadorLocal() {
    return jugadorLocal;
  }

  @Override
  public List<Jugador> obtenerJugadores() {
    return fachada.obtenerJugadores();
  }

  @Override
  public List<Carta> obtenerCartasCantadas() {
    return fachada.obtenerCartasCantadas();
  }

  @Override
  public int obtenerNumeroCartasRestantes() {
    return fachada.obtenerNumeroCartasRestantes();
  }

  @Override
  public EstadoPartida obtenerEstadoPartida() {
    return fachada.obtenerEstadoPartida();
  }

  @Override
  public String obtenerGanador() {
    return fachada.obtenerGanador();
  }

  public Patron obtenerPatron() {
    return fachada.obtenerPatron();
  }

  @Override
  public String obtenerUltimoError() {
    return fachada.obtenerUltimoError();
  }

  @Override
  public Mensaje obtenerMensaje() {
    return mensaje;
  }

  @Override
  public long obtenerVersionMensaje() {
    return versionMensaje;
  }
}
