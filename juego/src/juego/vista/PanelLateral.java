package juego.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import juego.controlador.ControladorJuego;
import juego.modelo.Carta;
import juego.modelo.EstadoPartida;
import juego.modelo.Jugador;
import juego.modelo.Modelo;
import juego.modelo.Patron;

public class PanelLateral extends JPanel {

  private final Modelo modelo;
  private final ControladorJuego controlador;
  private final Supplier<Jugador> jugadorLocal;
  private final JLabel etiquetaCartaCantada;
  private final JLabel etiquetaCartasRestantes;
  private final JLabel etiquetaEstado;
  private final JLabel etiquetaJugadorLocal;
  private final DefaultListModel<String> modeloListaJugadores;
  private Patron patronSeleccionado = Patron.CHORRO;

  public PanelLateral(
    Modelo modelo,
    ControladorJuego controlador,
    Supplier<Jugador> jugadorLocal,
    Runnable alCerrarVentana
  ) {
    this.modelo = modelo;
    this.controlador = controlador;
    this.jugadorLocal = jugadorLocal;

    setLayout(new BorderLayout(0, 8));
    setPreferredSize(new Dimension(320, 0));

    etiquetaCartaCantada = new JLabel();
    etiquetaCartasRestantes = new JLabel();
    etiquetaEstado = new JLabel();
    etiquetaJugadorLocal = new JLabel("Jugador: " + modelo.obtenerJugadorLocal());

    modeloListaJugadores = new DefaultListModel<>();
    JList<String> listaJugadores = new JList<>(modeloListaJugadores);
    listaJugadores.setEnabled(false);

    JPanel panelPatrones = new JPanel(new GridLayout(0, 1, 0, 4));
    for (Patron patron : Patron.values()) {
      JButton botonPatron = new JButton(patron.name());
      botonPatron.addActionListener(evento -> {
        patronSeleccionado = patron;
        Jugador local = this.jugadorLocal.get();
        if (local != null) {
          seleccionarPatron(local, patron);
        }
      });
      panelPatrones.add(botonPatron);
    }

    JButton botonCantarVictoria = new JButton("Cantar victoria");
    botonCantarVictoria.addActionListener(evento -> {
      Jugador local = this.jugadorLocal.get();
      if (local != null) {
        seleccionarVictoria(local, patronSeleccionado);
      }
    });

    JButton botonCerrarPartida = new JButton("Cerrar partida");
    botonCerrarPartida.addActionListener(evento -> {
      modelo.cerrar();
      alCerrarVentana.run();
    });

    JButton botonSalir = new JButton("Salir de la partida");
    botonSalir.addActionListener(evento -> alCerrarVentana.run());

    JPanel seccionInformacion = new JPanel(new GridLayout(4, 1));
    seccionInformacion.setBorder(BorderFactory.createTitledBorder("Información"));
    seccionInformacion.add(etiquetaJugadorLocal);
    seccionInformacion.add(etiquetaCartaCantada);
    seccionInformacion.add(etiquetaCartasRestantes);
    seccionInformacion.add(etiquetaEstado);

    JPanel seccionPatron = new JPanel(new BorderLayout(0, 4));
    seccionPatron.setBorder(BorderFactory.createTitledBorder("Patrón"));
    seccionPatron.add(panelPatrones, BorderLayout.CENTER);
    seccionPatron.add(botonCantarVictoria, BorderLayout.SOUTH);

    JPanel seccionJugadores = new JPanel(new BorderLayout());
    seccionJugadores.setBorder(BorderFactory.createTitledBorder("Jugadores"));
    seccionJugadores.add(new JScrollPane(listaJugadores), BorderLayout.CENTER);

    JPanel panelCentro = new JPanel(new BorderLayout(0, 8));
    panelCentro.add(seccionPatron, BorderLayout.NORTH);
    panelCentro.add(seccionJugadores, BorderLayout.CENTER);

    JPanel panelBotones = new JPanel(new GridLayout(2, 1, 0, 4));
    panelBotones.add(botonCerrarPartida);
    panelBotones.add(botonSalir);

    add(seccionInformacion, BorderLayout.NORTH);
    add(panelCentro, BorderLayout.CENTER);
    add(panelBotones, BorderLayout.SOUTH);
  }

  public void actualizar(List<Jugador> jugadores, Jugador local) {
    List<Carta> cantadas = modelo.obtenerCartasCantadas();
    if (cantadas.isEmpty()) {
      etiquetaCartaCantada.setText("Carta cantada: (ninguna)");
    } else {
      etiquetaCartaCantada.setText(
        "Carta cantada: " + cantadas.get(cantadas.size() - 1).obtenerNombre());
    }

    etiquetaCartasRestantes.setText(
      "Cartas restantes: " + modelo.obtenerNumeroCartasRestantes());
    String textoEstado = "Estado: " + modelo.obtenerEstadoPartida();
    if (modelo.obtenerEstadoPartida() == EstadoPartida.TERMINADA) {
      String ganador = modelo.obtenerGanador();
      textoEstado += ganador == null || ganador.isBlank()
        ? " - Empate" : " - Ganador: " + ganador;
    }
    etiquetaEstado.setText(textoEstado);

    if (local == null && modelo.obtenerEstadoPartida() == EstadoPartida.NO_INICIADA) {
      etiquetaJugadorLocal.setText("Esperando a que inicie la partida...");
    } else if (local == null) {
      etiquetaJugadorLocal.setText(
        "Jugador: " + modelo.obtenerJugadorLocal() + " (no encontrado)");
    } else {
      etiquetaJugadorLocal.setText("Jugador: " + local.obtenerNombre());
    }

    modeloListaJugadores.clear();
    for (Jugador jugador : jugadores) {
      boolean esLocal = local != null
        && jugador.obtenerNombre().equals(local.obtenerNombre());
      String marca = esLocal ? " (tú)" : "";
      modeloListaJugadores.addElement(
        jugador.obtenerNombre() + marca + " - Puntaje: " + jugador.obtenerPuntaje());
    }
  }

  public void seleccionarPatron(Jugador jugador, Patron patron) {
    controlador.seleccionarPatron(jugador, patron);
  }

  public void seleccionarVictoria(Jugador jugador, Patron patron) {
    controlador.seleccionarVictoria(jugador, patron);
  }
}
