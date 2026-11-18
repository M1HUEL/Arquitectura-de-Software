package juego.vista;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import juego.controlador.ControladorJuego;
import juego.modelo.Carta;
import juego.modelo.Jugador;

public class PanelTablero extends JPanel {

  private static final int COLUMNAS = 4;

  private final String nombre;
  private final ControladorJuego controlador;
  private Jugador jugador;
  private JPanel tablero;

  public PanelTablero(
    Jugador jugador,
    boolean esLocal,
    boolean puedeJugar,
    ControladorJuego controlador
  ) {
    this.nombre = jugador.obtenerNombre();
    this.controlador = controlador;
    setLayout(new BorderLayout());
    actualizar(jugador, esLocal, puedeJugar);
  }

  public String obtenerNombre() {
    return nombre;
  }

  public void actualizar(Jugador jugador, boolean esLocal, boolean puedeJugar) {
    this.jugador = jugador;
    setBorder(BorderFactory.createTitledBorder(
      esLocal ? jugador.obtenerNombre() + " (tu tablero)" : jugador.obtenerNombre()));

    JPanel nuevoTablero = new JPanel(new GridLayout(COLUMNAS, COLUMNAS));
    for (Carta carta : jugador.obtenerCartas()) {
      boolean marcada = jugador.estaSeleccionada(carta);
      JButton casilla = new JButton(
        marcada ? "[" + carta.obtenerNombre() + "]" : carta.obtenerNombre());
      casilla.setEnabled(puedeJugar);
      if (puedeJugar) {
        casilla.addActionListener(evento -> seleccionarCasilla(carta));
      } else if (!esLocal) {
        casilla.setToolTipText("Tablero de otro jugador: solo lectura");
      }
      nuevoTablero.add(casilla);
    }

    if (tablero != null) {
      remove(tablero);
    }
    tablero = nuevoTablero;
    add(tablero, BorderLayout.CENTER);
    revalidate();
    repaint();
  }

  public void seleccionarCasilla(Carta carta) {
    controlador.seleccionarCasilla(carta, jugador);
  }
}
