package juego;

import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import juego.controlador.ControladorJuego;
import juego.modelo.Carta;
import juego.modelo.Mazo;
import juego.modelo.ModeloJuego;
import juego.red.ConexionBroker;
import juego.vista.VistaJuego;

public class Main {

  private static final String HOST_POR_DEFECTO = "127.0.0.1";
  private static final int PUERTO_POR_DEFECTO = 7000;
  private static final String JUGADOR_POR_DEFECTO = "Jugador 1";

  public static void main(String[] args) {
    String host = args.length > 0 ? args[0] : HOST_POR_DEFECTO;
    int puerto = args.length > 1 ? Integer.parseInt(args[1]) : PUERTO_POR_DEFECTO;
    String jugadorLocal = args.length > 2 ? args[2] : JUGADOR_POR_DEFECTO;
    SwingUtilities.invokeLater(() -> iniciarJuego(host, puerto, jugadorLocal));
  }

  private static void iniciarJuego(String host, int puerto, String jugadorLocal) {
    List<Carta> cartas = Mazo.crearCartas();
    ModeloJuego modelo = new ModeloJuego(jugadorLocal, cartas);
    ControladorJuego controlador = new ControladorJuego(modelo);
    VistaJuego vista = new VistaJuego(modelo, controlador);
    modelo.agregarObservador(vista);

    ConexionBroker conexion;
    try {
      conexion = new ConexionBroker(host, puerto, modelo);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null,
        "No se pudo conectar con el broker " + host + ":" + puerto
          + "\n" + e.getMessage(),
        "Conexion fallida", JOptionPane.ERROR_MESSAGE);
      return;
    }

    modelo.establecerPublicador(conexion);
    Runtime.getRuntime().addShutdownHook(new Thread(conexion::close, "cierre-juego"));
    vista.setVisible(true);
  }
}
