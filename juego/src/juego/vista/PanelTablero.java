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

  private final Jugador jugador;
  private final ControladorJuego controlador;

  public PanelTablero(
    Jugador jugador,
    boolean esLocal,
    boolean puedeJugar,
    ControladorJuego controlador
  ) {
    this.jugador = jugador;
    this.controlador = controlador;
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createTitledBorder(
      esLocal ? jugador.obtenerNombre() + " (tu tablero)" : jugador.obtenerNombre()));

    JPanel tablero = new JPanel(new GridLayout(COLUMNAS, COLUMNAS));
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
      tablero.add(casilla);
    }
    add(tablero, BorderLayout.CENTER);
  }

  public void seleccionarCasilla(Carta carta) {
    controlador.seleccionarCasilla(carta, jugador);
  }
}
