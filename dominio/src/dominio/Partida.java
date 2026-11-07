package dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Partida {

  private final Mazo mazo;
  private final List<Jugador> jugadores;
  private final List<Carta> cartasCantadas;
  private Patron patron;
  private EstadoPartida estadoPartida;

  public Partida(Mazo mazo) {
    this.mazo = Objects.requireNonNull(mazo, "El mazo no puede ser nulo.");
    this.jugadores = new ArrayList<>();
    this.cartasCantadas = new ArrayList<>();
    this.estadoPartida = EstadoPartida.NO_INICIADA;
  }

  public void agregarJugador(Jugador jugador) {
    if (estadoPartida != EstadoPartida.NO_INICIADA) {
      throw new IllegalStateException("No se pueden agregar jugadores con la partida iniciada.");
    }
    jugadores.add(Objects.requireNonNull(jugador, "El jugador no puede ser nulo."));
  }

  public void iniciar() {
    if (estadoPartida == EstadoPartida.NO_INICIADA) {
      if (jugadores.isEmpty()) {
        throw new IllegalStateException("La partida necesita al menos un jugador.");
      }
      estadoPartida = EstadoPartida.EN_CURSO;
    }
  }

  public void finalizar() {
    if (estadoPartida == EstadoPartida.EN_CURSO) {
      estadoPartida = EstadoPartida.TERMINADA;
    }
  }

  public void cantarSiguienteCarta() {
    if (estadoPartida != EstadoPartida.EN_CURSO) {
      return;
    }

    Carta carta = mazo.tomarSiguiente();
    if (carta == null) {
      estadoPartida = EstadoPartida.TERMINADA;
      return;
    }

    cartasCantadas.add(carta);
    if (mazo.obtenerNumeroCartas() == 0) {
      estadoPartida = EstadoPartida.TERMINADA;
    }
  }

  public boolean contieneCasilla(Jugador jugador, Carta carta) {
    return jugador.obtenerTablero().contiene(carta);
  }

  public boolean cumplePatron(Jugador jugador, Patron patron) {
    return jugador.obtenerTablero().cumplePatron(patron, cartasCantadas);
  }

  public boolean tieneVictoria(Jugador jugador, Patron patron) {
    return cumplePatron(jugador, patron);
  }

  public boolean cartaDisponibleParaCantar(Carta carta) {
    return mazo.contiene(carta) && !cartasCantadas.contains(carta);
  }

  public void establecerPatron(Patron patron) {
    this.patron = patron;
  }

  public Mazo obtenerMazo() {
    return mazo;
  }

  public List<Jugador> obtenerJugadores() {
    return new ArrayList<>(jugadores);
  }

  public List<Carta> obtenerCartasCantadas() {
    return new ArrayList<>(cartasCantadas);
  }

  public int obtenerNumeroCartasCantadas() {
    return cartasCantadas.size();
  }

  public int obtenerNumeroCartasRestantes() {
    return mazo.obtenerNumeroCartas();
  }

  public Patron obtenerPatron() {
    return patron;
  }

  public EstadoPartida obtenerEstadoPartida() {
    return estadoPartida;
  }
}
