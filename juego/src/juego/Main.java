package juego;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import juego.controlador.ControladorJuego;
import juego.fachada.Fachada;
import juego.fachada.FachadaBroker;
import juego.fachada.FachadaJuego;
import juego.modelo.ModeloJuego;
import juego.vista.VistaJuego;

public class Main {

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      Fachada fachada = crearFachada(args);
      String jugadorLocal = fachada.obtenerJugadorLocal();
      ModeloJuego modelo = new ModeloJuego(jugadorLocal, fachada);
      ControladorJuego controlador = new ControladorJuego(modelo);
      VistaJuego vista = new VistaJuego(modelo, controlador);
      vista.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent evento) {
          System.exit(0);
        }
      });
      modelo.agregarObservador(vista);
      vista.setVisible(true);
      modelo.iniciar();
    });
  }

  private static Fachada crearFachada(String[] args) {
    if (args.length > 0 && "--broker".equals(args[0])) {
      String jugadorLocal = args.length > 1 ? args[1] : "Jugador";
      String hostBroker = args.length > 2 ? args[2] : "127.0.0.1";
      int puertoBroker = args.length > 3 ? Integer.parseInt(args[3]) : 7000;
      int puertoLocal = args.length > 4 ? Integer.parseInt(args[4]) : 7002;
      return new FachadaBroker(
        jugadorLocal, hostBroker, puertoBroker, "dominio", "juego", puertoLocal);
    }
    String jugadorLocal = args.length > 0 ? args[0] : "Jugador";
    return new FachadaJuego(jugadorLocal);
  }
}
