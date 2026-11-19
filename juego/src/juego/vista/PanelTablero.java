package juego.vista;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
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
  private final Map<Integer, JButton> botonesPorCarta = new LinkedHashMap<>();
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

    if (tablero == null) {
      tablero = new JPanel(new GridLayout(COLUMNAS, COLUMNAS));
      add(tablero, BorderLayout.CENTER);
    }
    for (Carta carta : jugador.obtenerCartas()) {
      int id = carta.obtenerId();
      JButton casilla = botonesPorCarta.get(id);
      if (casilla == null) {
        casilla = new JButton();
        casilla.addActionListener(evento -> seleccionarCasilla(id));
        botonesPorCarta.put(id, casilla);
        tablero.add(casilla);
      }
      boolean marcada = jugador.estaSeleccionada(carta);
      casilla.setText(marcada ? "[" + carta.obtenerNombre() + "]" : carta.obtenerNombre());
      casilla.setEnabled(puedeJugar);
      casilla.setToolTipText(esLocal ? null : "Tablero de otro jugador: solo lectura");
    }
    revalidate();
    repaint();
  }

  private void seleccionarCasilla(int id) {
    for (Carta carta : jugador.obtenerCartas()) {
      if (carta.obtenerId() == id) {
        controlador.seleccionarCasilla(carta, jugador);
        return;
      }
    }
  }
}
