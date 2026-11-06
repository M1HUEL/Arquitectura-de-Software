package juego.fachada;

import java.util.List;
import juego.modelo.Carta;
import juego.modelo.Jugador;
import juego.modelo.Patron;

public interface Fachada {

  boolean validarCasilla(Carta carta, Jugador jugador);

  boolean validarCantar(Carta carta, List<Carta> cartas, List<Carta> cartasCantadas);

  boolean validarPatron(Jugador jugador, List<Carta> cartasCantadas, Patron patron);

  boolean validarVictoria(Jugador jugador, List<Carta> cartasCantadas, Patron patron);
}
