package juego.modelo;

import java.util.List;

public interface Modelo {

  String obtenerJugadorLocal();

  List<Jugador> obtenerJugadores();

  List<Carta> obtenerCartasCantadas();

  int obtenerNumeroCartasRestantes();

  EstadoPartida obtenerEstadoPartida();

  Mensaje obtenerMensaje();

  long obtenerVersionMensaje();

  boolean estaDesconectado();
}
