package juego.fachada;

import juego.modelo.Patron;

public interface Fachada {

  void iniciar();

  void cerrar();

  void seleccionarCasilla(int cartaId);

  void seleccionarPatron(Patron patron);

  void seleccionarVictoria();
}
