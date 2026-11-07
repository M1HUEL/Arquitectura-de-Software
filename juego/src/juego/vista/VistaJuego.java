package juego.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
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
import juego.modelo.EstadoPartida;
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
  private final JLabel etiquetaJugadorLocal;
  private final JPanel panelTableros;
  private final DefaultListModel<String> modeloListaJugadores;
  private final JList<String> listaJugadores;
  private Patron patronSeleccionado = Patron.LINEA;
  private boolean avisoDesconexionMostrado;
  private long versionMensajeMostrado = -1;

  public VistaJuego(Modelo modelo, ControladorJuego controlador) {
    this.modelo = modelo;
    this.controlador = controlador;

    setTitle("Loteria - " + modelo.obtenerJugadorLocal());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 700);

    etiquetaCartaCantada = new JLabel();
    etiquetaCartasRestantes = new JLabel();
    etiquetaEstado = new JLabel();
    etiquetaJugadorLocal = new JLabel("Jugador: " + modelo.obtenerJugadorLocal());

    modeloListaJugadores = new DefaultListModel<>();
    listaJugadores = new JList<>(modeloListaJugadores);
    listaJugadores.setEnabled(false);

    panelTableros = new JPanel(new GridLayout(0, 1, 0, 8));

    JPanel panelPatrones = new JPanel(new GridLayout(0, 1, 0, 4));
    for (Patron patron : Patron.values()) {
      JButton botonPatron = new JButton(patron.name());
      botonPatron.addActionListener(evento -> {
        patronSeleccionado = patron;
        Jugador local = buscarJugadorLocal();
        if (local != null) {
          seleccionarPatron(local, patron);
        }
      });
      panelPatrones.add(botonPatron);
    }

    JButton botonCantarVictoria = new JButton("Cantar Victoria");
    botonCantarVictoria.addActionListener(evento -> {
      Jugador local = buscarJugadorLocal();
      if (local != null) {
        seleccionarVictoria(local, patronSeleccionado);
      }
    });

    JButton botonSalir = new JButton("Salir de la partida");
    botonSalir.addActionListener(evento -> {
      dispose();
      System.exit(0);
    });

    JPanel seccionInformacion = new JPanel(new GridLayout(4, 1));
    seccionInformacion.setBorder(BorderFactory.createTitledBorder("Informacion"));
    seccionInformacion.add(etiquetaJugadorLocal);
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

    List<Jugador> jugadores = ordenarJugadores();
    Jugador local = buscarJugadorLocal(jugadores);
    if (local == null) {
      etiquetaJugadorLocal.setText(
        "Jugador: " + this.modelo.obtenerJugadorLocal() + " (no encontrado)");
    } else {
      etiquetaJugadorLocal.setText("Jugador: " + local.obtenerNombre());
    }

    if (this.modelo.estaDesconectado() && !avisoDesconexionMostrado) {
      avisoDesconexionMostrado = true;
      JOptionPane.showMessageDialog(this,
        "Se perdio la conexion con el broker. La partida ya no se actualizara.",
        "Conexion perdida", JOptionPane.WARNING_MESSAGE);
    }

    modeloListaJugadores.clear();
    for (Jugador jugador : jugadores) {
      boolean esLocal = local != null
        && jugador.obtenerNombre().equals(local.obtenerNombre());
      String marca = esLocal ? " (tu)" : "";
      modeloListaJugadores.addElement(
        jugador.obtenerNombre() + marca + " - Puntaje: " + jugador.obtenerPuntaje());
    }

    boolean puedeJugar = !this.modelo.estaDesconectado()
      && this.modelo.obtenerEstadoPartida() == EstadoPartida.EN_CURSO;
    panelTableros.removeAll();
    for (Jugador jugador : jugadores) {
      boolean esLocal = local != null
        && jugador.obtenerNombre().equals(local.obtenerNombre());
      JPanel panelJugador = new JPanel(new BorderLayout());
      panelJugador.setBorder(BorderFactory.createTitledBorder(
        esLocal ? jugador.obtenerNombre() + " (tu tablero)" : jugador.obtenerNombre()));

      JPanel tablero = new JPanel(new GridLayout(COLUMNAS_TABLERO, COLUMNAS_TABLERO));
      for (Carta carta : jugador.obtenerCartas()) {
        boolean marcada = jugador.estaSeleccionada(carta);
        JButton casilla = new JButton(
          marcada ? "[" + carta.obtenerNombre() + "]" : carta.obtenerNombre());
        boolean interactivo = esLocal && puedeJugar;
        casilla.setEnabled(interactivo);
        if (interactivo) {
          casilla.addActionListener(evento -> seleccionarCasilla(carta, jugador));
        } else if (!esLocal) {
          casilla.setToolTipText("Tablero de otro jugador: solo lectura");
        }
        tablero.add(casilla);
      }

      panelJugador.add(tablero, BorderLayout.CENTER);
      panelTableros.add(panelJugador);
    }
    panelTableros.revalidate();
    panelTableros.repaint();

    long version = this.modelo.obtenerVersionMensaje();
    if (version != versionMensajeMostrado) {
      versionMensajeMostrado = version;
      Mensaje mensaje = this.modelo.obtenerMensaje();
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
