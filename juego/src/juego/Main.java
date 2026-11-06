package juego;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import juego.controlador.ControladorJuego;
import juego.fachada.Fachada;
import juego.fachada.FachadaJuego;
import juego.modelo.Carta;
import juego.modelo.EstadoPartida;
import juego.modelo.Jugador;
import juego.modelo.Mazo;
import juego.modelo.ModeloJuego;
import juego.motor.CantadorAutomatico;
import juego.vista.VistaJuego;

public class Main {

  private static final int CARTAS_POR_JUGADOR = 16;
  private static final long INTERVALO_CANTADO_MS = 1000;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(Main::iniciarJuego);
  }

  private static void iniciarJuego() {
    List<Carta> cartas = Mazo.crearCartas();
    Collections.shuffle(cartas);

    Fachada fachada = new FachadaJuego();
    ModeloJuego modelo = new ModeloJuego(fachada, cartas);
    ControladorJuego controlador = new ControladorJuego(modelo);
    VistaJuego vista = new VistaJuego(modelo, controlador);
    modelo.agregarObservador(vista);

    modelo.agregarJugador(crearJugador("Jugador 1", "jugador1.png", cartas));
    modelo.agregarJugador(crearJugador("Jugador 2", "jugador2.png", cartas));
    modelo.establecerEstadoPartida(EstadoPartida.EN_CURSO);

    vista.setVisible(true);
    new CantadorAutomatico(modelo, INTERVALO_CANTADO_MS).iniciar();
  }

  private static Jugador crearJugador(String nombre, String imagen, List<Carta> cartas) {
    List<Carta> cartasJugador = new ArrayList<>(cartas);
    Collections.shuffle(cartasJugador);
    return new Jugador(nombre, imagen, cartasJugador.subList(0, CARTAS_POR_JUGADOR));
  }
}
