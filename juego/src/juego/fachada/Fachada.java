package juego.fachada;

import juego.modelo.Carta;
import juego.modelo.Patron;

public interface Fachada {

  void iniciar();

  void cerrar();

  void seleccionarCasilla(Carta carta);

  void seleccionarPatron(Patron patron);

  void seleccionarVictoria(Patron patron);
}
