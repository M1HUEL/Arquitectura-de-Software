package juego.vista;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import juego.controlador.ControladorJuego;
import juego.modelo.EstadoPartida;
import juego.modelo.Jugador;
import juego.modelo.Mensaje;
import juego.modelo.Modelo;
import juego.observador.Observador;

public class VistaJuego extends JFrame implements Observador {

  private final Modelo modelo;
  private final ControladorJuego controlador;
  private final PanelLateral panelLateral;
  private final JPanel panelTableros;

  public VistaJuego(Modelo modelo, ControladorJuego controlador) {
    this.modelo = modelo;
    this.controlador = controlador;

    setTitle("Lotería - " + modelo.obtenerJugadorLocal());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(1200, 700);
    setLocationRelativeTo(null);

    panelTableros = new JPanel(new GridLayout(0, 1, 0, 8));
    panelLateral = new PanelLateral(modelo, controlador, this::buscarJugadorLocal, this::dispose);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent evento) {
        modelo.desconectar();
      }
    });

    add(new JScrollPane(panelTableros), BorderLayout.CENTER);
    add(panelLateral, BorderLayout.EAST);
  }

  @Override
  public void actualizar(Modelo modelo) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> actualizar(modelo));
      return;
    }

    List<Jugador> jugadores = ordenarJugadores();
    Jugador local = buscarJugadorLocal(jugadores);
    panelLateral.actualizar(jugadores, local);

    boolean puedeJugar = this.modelo.obtenerEstadoPartida() == EstadoPartida.EN_CURSO;
    panelTableros.removeAll();
    for (Jugador jugador : jugadores) {
      boolean esLocal = local != null
        && jugador.obtenerNombre().equals(local.obtenerNombre());
      panelTableros.add(new PanelTablero(
        jugador, esLocal, esLocal && puedeJugar, controlador));
    }
    panelTableros.revalidate();
    panelTableros.repaint();

    Mensaje mensaje = this.modelo.consumirMensaje();
    if (mensaje == null) {
      return;
    }
    switch (mensaje) {
      case CASILLA_VALIDA:
        mostrarMensajeCasillaValida();
        break;
      case CASILLA_INVALIDA:
        mostrarMensajeCasillaInvalida();
        break;
      case PATRON_VALIDO:
        mostrarMensajePatronValido();
        break;
      case PATRON_INVALIDO:
        mostrarMensajePatronInvalido();
        break;
      case VICTORIA_VALIDA:
        mostrarMensajeVictoriaValida();
        break;
      case VICTORIA_INVALIDA:
        mostrarMensajeVictoriaInvalida();
        break;
    }
  }

  private List<Jugador> ordenarJugadores() {
    List<Jugador> jugadores = new ArrayList<>(this.modelo.obtenerJugadores());
    String nombreLocal = this.modelo.obtenerJugadorLocal();
    jugadores.sort((uno, otro) -> Boolean.compare(
      !uno.obtenerNombre().equals(nombreLocal),
      !otro.obtenerNombre().equals(nombreLocal)));
    return jugadores;
  }

  private Jugador buscarJugadorLocal() {
    return buscarJugadorLocal(this.modelo.obtenerJugadores());
  }

  private Jugador buscarJugadorLocal(List<Jugador> jugadores) {
    String nombreLocal = this.modelo.obtenerJugadorLocal();
    for (Jugador jugador : jugadores) {
      if (jugador.obtenerNombre().equals(nombreLocal)) {
        return jugador;
      }
    }
    return null;
  }

  public void mostrarMensajeCasillaValida() {
    JOptionPane.showMessageDialog(this, "La casilla es válida.");
  }

  public void mostrarMensajeCasillaInvalida() {
    JOptionPane.showMessageDialog(this, "La casilla no es válida.");
  }

  public void mostrarMensajePatronValido() {
    JOptionPane.showMessageDialog(this, "El patrón es válido.");
  }

  public void mostrarMensajePatronInvalido() {
    JOptionPane.showMessageDialog(this, "El patrón no está completo en el tablero.");
  }

  public void mostrarMensajeVictoriaValida() {
    JOptionPane.showMessageDialog(this, "Victoria válida.");
  }

  public void mostrarMensajeVictoriaInvalida() {
    JOptionPane.showMessageDialog(this, "Victoria inválida.");
  }
}
