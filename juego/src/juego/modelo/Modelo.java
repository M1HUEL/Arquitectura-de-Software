package juego.modelo;

import java.util.List;

public interface Modelo {

  void cerrar();

  String obtenerJugadorLocal();

  List<Jugador> obtenerJugadores();

  List<Carta> obtenerCartasCantadas();

  int obtenerNumeroCartasRestantes();

  EstadoPartida obtenerEstadoPartida();

  String obtenerGanador();

  String obtenerUltimoError();

  TipoMensaje consumirMensaje();
}
