package juego;

import comunicacion.DispatcherFactory;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import juego.controlador.ControladorJuego;
import juego.fachada.FachadaBroker;
import juego.fachada.ManejadorSocket;
import juego.modelo.ModeloJuego;
import juego.vista.VistaJuego;
import serializador.Serializador;

public class Main {

  private static final String PATRON_FILTRO = "comun.*;java.lang.*;java.util.*;!*";
  private static final String TEMA_COMANDOS = "dominio";
  private static final String HOST = "127.0.0.1";
  private static final int PUERTO_BROKER = 7000;
  private static final int PUERTO_CLIENTE = 7002;

  public static void main(String[] args) {
    if (args.length == 0 || !"--broker".equals(args[0])) {
      System.err.println(
        "Uso: juego.Main --broker <jugador> [hostBroker] [puertoBroker] [puertoCliente]");
      return;
    }
    SwingUtilities.invokeLater(() -> iniciar(args));
  }

  private static void iniciar(String[] args) {
    String jugadorLocal = args.length > 1 ? args[1] : "Jugador";
    String hostBroker = args.length > 2 ? args[2] : HOST;
    int puertoBroker = entero(args, 3, PUERTO_BROKER);
    int puertoCliente = entero(args, 4, PUERTO_CLIENTE);

    Serializador serializador = new Serializador(PATRON_FILTRO);
    FachadaBroker fachada = new FachadaBroker(
      jugadorLocal,
      hostBroker,
      puertoBroker,
      TEMA_COMANDOS,
      serializador,
      DispatcherFactory.crear()
    );
    ModeloJuego modelo = new ModeloJuego(jugadorLocal, fachada);
    ManejadorSocket manejador = new ManejadorSocket(puertoCliente, serializador, modelo);
    ControladorJuego controlador = new ControladorJuego(modelo);
    VistaJuego vista = new VistaJuego(modelo, controlador);
    vista.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent evento) {
        manejador.detener();
        System.exit(0);
      }
    });
    modelo.agregarObservador(vista);

    try {
      manejador.iniciar();
      vista.setVisible(true);
      modelo.iniciar();
    } catch (RuntimeException e) {
      manejador.detener();
      JOptionPane.showMessageDialog(
        vista,
        "No se pudo conectar el cliente: " + e.getMessage(),
        "Error",
        JOptionPane.ERROR_MESSAGE
      );
      vista.dispose();
    }
  }

  private static int entero(String[] args, int indice, int porDefecto) {
    if (args.length <= indice) {
      return porDefecto;
    }
    try {
      return Integer.parseInt(args[indice]);
    } catch (NumberFormatException e) {
      System.err.println("Valor no válido: " + args[indice] + ". Se usa " + porDefecto);
      return porDefecto;
    }
  }
}
