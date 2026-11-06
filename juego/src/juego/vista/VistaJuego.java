package juego.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import juego.controlador.ControladorJuego;
import juego.modelo.Carta;
import juego.modelo.Jugador;
import juego.modelo.Mensaje;
import juego.modelo.Modelo;
import juego.modelo.Patron;
import juego.observador.Observador;

public class VistaJuego extends JFrame implements Observador {

  private static final int COLUMNAS_TABLERO = 4;

  private final Modelo modelo;
  private final ControladorJuego controlador;
  private final JLabel etiquetaCartaCantada;
  private final JLabel etiquetaCartasRestantes;
  private final JLabel etiquetaEstado;
  private final JPanel panelTableros;
  private final DefaultListModel<String> modeloListaJugadores;
  private final JList<String> listaJugadores;
  private Patron patronSeleccionado = Patron.LINEA;

  public VistaJuego(Modelo modelo, ControladorJuego controlador) {
    this.modelo = modelo;
    this.controlador = controlador;

    setTitle("Loteria");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 700);

    etiquetaCartaCantada = new JLabel();
    etiquetaCartasRestantes = new JLabel();
    etiquetaEstado = new JLabel();

    modeloListaJugadores = new DefaultListModel<>();
    listaJugadores = new JList<>(modeloListaJugadores);

    panelTableros = new JPanel(new GridLayout(0, 1, 0, 8));

    JPanel panelPatrones = new JPanel(new GridLayout(0, 1, 0, 4));
    for (Patron patron : Patron.values()) {
      JButton botonPatron = new JButton(patron.name());
      botonPatron.addActionListener(evento -> {
        patronSeleccionado = patron;
        int indice = listaJugadores.getSelectedIndex();
        if (indice < 0 || indice >= this.modelo.obtenerJugadores().size()) {
          return;
        }
        Jugador jugador = this.modelo.obtenerJugadores().get(indice);
        seleccionarPatron(jugador, patron);
      });
      panelPatrones.add(botonPatron);
    }

    JButton botonCantarVictoria = new JButton("Cantar Victoria");
    botonCantarVictoria.addActionListener(evento -> {
      int indice = listaJugadores.getSelectedIndex();
      if (indice < 0 || indice >= this.modelo.obtenerJugadores().size()) {
        return;
      }
      Jugador jugador = this.modelo.obtenerJugadores().get(indice);
      seleccionarVictoria(jugador, patronSeleccionado);
    });

    JButton botonSalir = new JButton("Salir de la partida");
    botonSalir.addActionListener(evento -> {
      dispose();
      System.exit(0);
    });

    JPanel seccionInformacion = new JPanel(new GridLayout(3, 1));
    seccionInformacion.setBorder(BorderFactory.createTitledBorder("Informacion"));
    seccionInformacion.add(etiquetaCartaCantada);
    seccionInformacion.add(etiquetaCartasRestantes);
    seccionInformacion.add(etiquetaEstado);

    JPanel seccionPatron = new JPanel(new BorderLayout(0, 4));
    seccionPatron.setBorder(BorderFactory.createTitledBorder("Patron"));
    seccionPatron.add(panelPatrones, BorderLayout.CENTER);
    seccionPatron.add(botonCantarVictoria, BorderLayout.SOUTH);

    JPanel seccionJugadores = new JPanel(new BorderLayout());
    seccionJugadores.setBorder(BorderFactory.createTitledBorder("Jugadores"));
    seccionJugadores.add(new JScrollPane(listaJugadores), BorderLayout.CENTER);

    JPanel panelCentroLateral = new JPanel(new BorderLayout(0, 8));
    panelCentroLateral.add(seccionPatron, BorderLayout.NORTH);
    panelCentroLateral.add(seccionJugadores, BorderLayout.CENTER);

    JPanel panelLateral = new JPanel(new BorderLayout(0, 8));
    panelLateral.setPreferredSize(new Dimension(320, 0));
    panelLateral.add(seccionInformacion, BorderLayout.NORTH);
    panelLateral.add(panelCentroLateral, BorderLayout.CENTER);
    panelLateral.add(botonSalir, BorderLayout.SOUTH);

    add(new JScrollPane(panelTableros), BorderLayout.CENTER);
    add(panelLateral, BorderLayout.EAST);
  }

  public void seleccionarCasilla(Carta carta, Jugador jugador) {
    controlador.seleccionarCasilla(carta, jugador);
  }

  public void seleccionarPatron(Jugador jugador, Patron patron) {
    controlador.seleccionarPatron(jugador, patron);
  }

  public void seleccionarVictoria(Jugador jugador, Patron patron) {
    controlador.seleccionarVictoria(jugador, patron);
  }

  @Override
  public void actualizar(Modelo modelo) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> actualizar(modelo));
      return;
    }

    List<Carta> cantadas = this.modelo.obtenerCartasCantadas();
    if (cantadas.isEmpty()) {
      etiquetaCartaCantada.setText("Carta cantada: (ninguna)");
    } else {
      etiquetaCartaCantada.setText(
        "Carta cantada: " + cantadas.get(cantadas.size() - 1).obtenerNombre());
    }

    etiquetaCartasRestantes.setText(
      "Cartas restantes: " + this.modelo.obtenerNumeroCartasRestantes());
    etiquetaEstado.setText("Estado: " + this.modelo.obtenerEstadoPartida());

    int seleccion = listaJugadores.getSelectedIndex();
    modeloListaJugadores.clear();
    for (Jugador jugador : this.modelo.obtenerJugadores()) {
      modeloListaJugadores.addElement(
        jugador.obtenerNombre() + " - Puntaje: " + jugador.obtenerPuntaje());
    }
    if (!modeloListaJugadores.isEmpty()) {
      if (seleccion < 0 || seleccion >= modeloListaJugadores.size()) {
        seleccion = 0;
      }
      listaJugadores.setSelectedIndex(seleccion);
    }

    panelTableros.removeAll();
    for (Jugador jugador : this.modelo.obtenerJugadores()) {
      JPanel panelJugador = new JPanel(new BorderLayout());
      panelJugador.setBorder(BorderFactory.createTitledBorder(jugador.obtenerNombre()));

      JPanel tablero = new JPanel(new GridLayout(COLUMNAS_TABLERO, COLUMNAS_TABLERO));
      for (Carta carta : jugador.obtenerCartas()) {
        boolean marcada = false;
        for (Carta cantada : cantadas) {
          if (cantada.obtenerNombre().equals(carta.obtenerNombre())) {
            marcada = true;
            break;
          }
        }
        JButton casilla = new JButton(
          marcada ? "[" + carta.obtenerNombre() + "]" : carta.obtenerNombre());
        casilla.addActionListener(evento -> seleccionarCasilla(carta, jugador));
        tablero.add(casilla);
      }

      panelJugador.add(tablero, BorderLayout.CENTER);
      panelTableros.add(panelJugador);
    }
    panelTableros.revalidate();
    panelTableros.repaint();

    Mensaje mensaje = this.modelo.obtenerMensaje();
    if (mensaje != null) {
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
  }

  public void mostrarMensajeCasillaValida() {
    JOptionPane.showMessageDialog(this, "La casilla es valida.");
  }

  public void mostrarMensajeCasillaInvalida() {
    JOptionPane.showMessageDialog(this, "La casilla no es valida.");
  }

  public void mostrarMensajePatronValido() {
    JOptionPane.showMessageDialog(this, "El patron es valido.");
  }

  public void mostrarMensajePatronInvalido() {
    JOptionPane.showMessageDialog(this, "El jugador no tiene ese patron en su tablero.");
  }

  public void mostrarMensajeVictoriaValida() {
    JOptionPane.showMessageDialog(this, "Victoria valida.");
  }

  public void mostrarMensajeVictoriaInvalida() {
    JOptionPane.showMessageDialog(this, "Victoria invalida.");
  }
}
