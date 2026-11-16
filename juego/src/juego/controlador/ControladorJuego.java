package juego.controlador;

import juego.modelo.Carta;
import juego.modelo.Jugador;
import juego.modelo.ModeloJuego;
import juego.modelo.Patron;

public class ControladorJuego {

  private final ModeloJuego modelo;

  public ControladorJuego(ModeloJuego modelo) {
    this.modelo = modelo;
  }

  public void seleccionarCasilla(Carta carta, Jugador jugador) {
    modelo.seleccionarCasilla(carta, jugador);
  }

  public void seleccionarPatron(Jugador jugador, Patron patron) {
    modelo.seleccionarPatron(jugador, patron);
  }

  public void seleccionarVictoria(Jugador jugador, Patron patron) {
    modelo.seleccionarVictoria(jugador, patron);
  }
}
