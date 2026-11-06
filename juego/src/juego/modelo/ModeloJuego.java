package juego.modelo;

import java.util.ArrayList;
import java.util.List;
import juego.fachada.Fachada;
import juego.observador.Observador;

public class ModeloJuego implements Modelo {

  private final Fachada fachada;
  private final List<Observador> observadores = new ArrayList<>();
  private final List<Jugador> jugadores = new ArrayList<>();
  private final List<Carta> cartas;
  private final List<Carta> cartasCantadas = new ArrayList<>();
  private int numeroCartasRestantes;
  private Patron patron;
  private EstadoPartida estadoPartida = EstadoPartida.NO_INICIADA;
  private Mensaje mensaje;

  public ModeloJuego(Fachada fachada, List<Carta> cartas) {
    this.fachada = fachada;
    this.cartas = new ArrayList<>(cartas);
    this.numeroCartasRestantes = this.cartas.size();
  }

  public void seleccionarCasilla(Carta carta, Jugador jugador) {
    boolean valida = fachada.validarCasilla(carta, jugador);
    notificarObservadores(valida ? Mensaje.CASILLA_VALIDA : Mensaje.CASILLA_INVALIDA);
  }

  public void seleccionarPatron(Jugador jugador, Patron patron) {
    boolean valido = fachada.validarPatron(jugador, cartasCantadas, patron);
    notificarObservadores(valido ? Mensaje.PATRON_VALIDO : Mensaje.PATRON_INVALIDO);
  }

  public void seleccionarVictoria(Jugador jugador, Patron patron) {
    boolean valida = fachada.validarVictoria(jugador, cartasCantadas, patron);
    notificarObservadores(valida ? Mensaje.VICTORIA_VALIDA : Mensaje.VICTORIA_INVALIDA);
  }

  public void agregarObservador(Observador observador) {
    observadores.add(observador);
  }

  public void eliminarObservador(Observador observador) {
    observadores.remove(observador);
  }

  public void notificarObservadores() {
    notificarObservadores(null);
  }

  private void notificarObservadores(Mensaje mensaje) {
    this.mensaje = mensaje;
    for (Observador observador : observadores) {
      observador.actualizar(this);
    }
  }

  @Override
  public Mensaje obtenerMensaje() {
    return mensaje;
  }

  @Override
  public List<Jugador> obtenerJugadores() {
    return jugadores;
  }

  public List<Carta> obtenerCartas() {
    return cartas;
  }

  public int obtenerNumeroCartas() {
    return cartas.size();
  }

  @Override
  public int obtenerNumeroCartasRestantes() {
    return numeroCartasRestantes;
  }

  @Override
  public List<Carta> obtenerCartasCantadas() {
    return cartasCantadas;
  }

  public int obtenerNumeroCartasCantadas() {
    return cartasCantadas.size();
  }

  public Patron obtenerPatron() {
    return patron;
  }

  @Override
  public EstadoPartida obtenerEstadoPartida() {
    return estadoPartida;
  }

  public void cantarSiguienteCarta() {
    Carta carta = buscarSiguienteCarta();
    if (carta == null) {
      return;
    }
    if (!fachada.validarCantar(carta, cartas, cartasCantadas)) {
      return;
    }
    cartasCantadas.add(carta);
    numeroCartasRestantes--;
    if (numeroCartasRestantes == 0) {
      estadoPartida = EstadoPartida.TERMINADA;
    }
    notificarObservadores();
  }

  private Carta buscarSiguienteCarta() {
    for (Carta carta : cartas) {
      boolean yaCantada = false;
      for (Carta cartaCantada : cartasCantadas) {
        if (cartaCantada.obtenerNombre().equals(carta.obtenerNombre())) {
          yaCantada = true;
          break;
        }
      }
      if (!yaCantada) {
        return carta;
      }
    }
    return null;
  }

  public void agregarJugador(Jugador jugador) {
    jugadores.add(jugador);
    notificarObservadores();
  }

  public void establecerPatron(Patron patron) {
    this.patron = patron;
    notificarObservadores();
  }

  public void establecerEstadoPartida(EstadoPartida estadoPartida) {
    this.estadoPartida = estadoPartida;
    notificarObservadores();
  }
}
