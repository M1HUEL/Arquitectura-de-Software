package juego.fachada;

import java.util.List;
import juego.modelo.Carta;
import juego.modelo.EstadoPartida;
import juego.modelo.Jugador;
import juego.modelo.Mensaje;
import juego.modelo.Patron;

public interface Fachada {

  void iniciar();

  void cerrar();

  void agregarOyente(Runnable oyente);

  void eliminarOyente(Runnable oyente);

  String obtenerJugadorLocal();

  List<Jugador> obtenerJugadores();

  List<Carta> obtenerCartasCantadas();

  int obtenerNumeroCartasRestantes();

  EstadoPartida obtenerEstadoPartida();

  String obtenerGanador();

  Patron obtenerPatron();

  Mensaje seleccionarCasilla(Jugador jugador, Carta carta);

  Mensaje seleccionarPatron(Jugador jugador, Patron patron);

  Mensaje seleccionarVictoria(Jugador jugador, Patron patron);
}
