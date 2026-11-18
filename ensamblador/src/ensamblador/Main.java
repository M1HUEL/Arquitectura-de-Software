package ensamblador;

import broker.Broker;
import comunicacion.Dispatcher;
import comunicacion.DispatcherFactory;
import comunicacion.Receptor;
import dominio.Dominio;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import juego.controlador.ControladorJuego;
import juego.fachada.FachadaBroker;
import juego.manejador.ManejadorSocket;
import juego.modelo.ModeloJuego;
import juego.vista.VistaJuego;
import serializador.Serializador;

public class Main {

  private static final String PATRON_FILTRO = "comun.*;java.lang.*;java.util.*;!*";
  private static final String HOST = "127.0.0.1";
  private static final int PUERTO_BROKER = 7000;
  private static final int PUERTO_DOMINIO = 7001;
  private static final int PUERTO_CLIENTE = 7002;
  private static final int SEGUNDOS_ESPERA = 30;
  private static final long INTERVALO_CARTAS = 3000L;
  private static final String TEMA_COMANDOS = "dominio";
  private static final String TEMA_EVENTOS = "juego";

  public static void main(String[] args) {
    if (args.length == 0) {
      mostrarUso();
      return;
    }
    switch (args[0].toLowerCase(Locale.ROOT)) {
      case "broker" ->
        iniciarBroker(args);
      case "dominio" ->
        iniciarDominio(args);
      case "cliente" ->
        iniciarCliente(args);
      default ->
        mostrarUso();
    }
  }

  private static void iniciarBroker(String[] args) {
    int puertoBroker = entero(args, 1, PUERTO_BROKER);
    int puertoDominio = entero(args, 2, PUERTO_DOMINIO);
    Dispatcher dispatcher = DispatcherFactory.crear();
    Broker broker = new Broker(new Serializador(PATRON_FILTRO), dispatcher);
    Receptor receptor = new Receptor(broker);
    try {
      broker.suscribir(TEMA_COMANDOS, HOST, puertoDominio);
      if (args.length > 3) {
        for (int i = 3; i < args.length; i++) {
          int puertoCliente = entero(args, i, -1);
          if (puertoCliente > 0) {
            broker.suscribir(TEMA_EVENTOS, HOST, puertoCliente);
          }
        }
      } else {
        broker.suscribir(TEMA_EVENTOS, HOST, PUERTO_CLIENTE);
        broker.suscribir(TEMA_EVENTOS, HOST, PUERTO_CLIENTE + 1);
      }
      dispatcher.iniciar();
      iniciarReceptor(receptor, puertoBroker);
    } catch (RuntimeException e) {
      System.err.println("No se pudo iniciar el broker: " + e.getMessage());
      detener(receptor, dispatcher);
      return;
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> detener(receptor, dispatcher)));
    System.out.println("Broker escuchando en " + HOST + ":" + receptor.obtenerPuerto());
    System.out.println("Dominio suscrito en " + HOST + ":" + puertoDominio);
    bloquear();
  }

  private static void iniciarDominio(String[] args) {
    int puertoDominio = entero(args, 1, PUERTO_DOMINIO);
    int puertoBroker = entero(args, 2, PUERTO_BROKER);
    int segundosEspera = entero(args, 3, SEGUNDOS_ESPERA);
    long intervalo = largo(args, 4, INTERVALO_CARTAS);
    Serializador serializador = new Serializador(PATRON_FILTRO);
    Dispatcher dispatcher = DispatcherFactory.crear();
    Dominio dominio = new Dominio(
      serializador,
      dispatcher,
      HOST,
      puertoBroker,
      TEMA_COMANDOS,
      TEMA_EVENTOS,
      List.of(),
      intervalo,
      new Random().nextLong(),
      Math.max(1, segundosEspera) * 1000L
    );
    Receptor receptor = new Receptor(dominio);
    try {
      dispatcher.iniciar();
      dominio.iniciar();
      iniciarReceptor(receptor, puertoDominio);
    } catch (RuntimeException e) {
      System.err.println("No se pudo iniciar el dominio: " + e.getMessage());
      dominio.detener();
      detener(receptor, dispatcher);
      return;
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      dominio.detener();
      detener(receptor, dispatcher);
    }));
    System.out.println("Dominio escuchando en " + HOST + ":" + receptor.obtenerPuerto());
    System.out.println("Broker en " + HOST + ":" + puertoBroker);
    System.out.println("Espera para conectar clientes: " + Math.max(1, segundosEspera) + " s");
    bloquear();
  }

  private static void iniciarCliente(String[] args) {
    if (args.length < 3) {
      System.out.println("Uso: cliente <nombre> <puertoCliente> [puertoBroker] [hostBroker]");
      return;
    }
    String nombre = args[1];
    if (nombre == null || nombre.isBlank()) {
      System.out.println("El nombre del cliente no puede estar vacío");
      return;
    }
    int puertoCliente = entero(args, 2, PUERTO_CLIENTE);
    int puertoBroker = entero(args, 3, PUERTO_BROKER);
    String hostBroker = args.length > 4 ? args[4] : HOST;
    Serializador serializador = new Serializador(PATRON_FILTRO);
    SwingUtilities.invokeLater(() -> {
      Dispatcher dispatcher = DispatcherFactory.crear();
      FachadaBroker fachada = new FachadaBroker(
        nombre, hostBroker, puertoBroker, TEMA_COMANDOS, serializador, dispatcher);
      ModeloJuego modelo = new ModeloJuego(nombre, fachada);
      ManejadorSocket manejador = new ManejadorSocket(serializador, modelo);
      Receptor receptor = new Receptor(manejador);
      ControladorJuego controlador = new ControladorJuego(modelo);
      VistaJuego vista = new VistaJuego(modelo, controlador);
      vista.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent evento) {
          detener(receptor, dispatcher);
          System.exit(0);
        }
      });
      modelo.agregarObservador(vista);
      try {
        iniciarReceptor(receptor, puertoCliente);
        dispatcher.iniciar();
        vista.setVisible(true);
        modelo.iniciar();
      } catch (RuntimeException e) {
        detener(receptor, dispatcher);
        JOptionPane.showMessageDialog(
          vista,
          "No se pudo conectar el cliente: " + e.getMessage(),
          "Error",
          JOptionPane.ERROR_MESSAGE
        );
        vista.dispose();
      }
    });
  }

  private static void iniciarReceptor(Receptor receptor, int puerto) {
    receptor.iniciar(puerto);
    if (!receptor.estaActivo()) {
      throw new IllegalStateException("No se pudo iniciar la recepción en el puerto " + puerto);
    }
  }

  private static void detener(Receptor receptor, Dispatcher dispatcher) {
    receptor.detener();
    dispatcher.detener();
  }

  private static void bloquear() {
    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static int entero(String[] args, int indice, int porDefecto) {
    if (args.length <= indice) {
      return porDefecto;
    }
    try {
      return Integer.parseInt(args[indice]);
    } catch (NumberFormatException e) {
      System.out.println("Valor no válido: " + args[indice] + ". Se usa " + porDefecto);
      return porDefecto;
    }
  }

  private static long largo(String[] args, int indice, long porDefecto) {
    if (args.length <= indice) {
      return porDefecto;
    }
    try {
      return Long.parseLong(args[indice]);
    } catch (NumberFormatException e) {
      System.out.println("Valor no válido: " + args[indice] + ". Se usa " + porDefecto);
      return porDefecto;
    }
  }

  private static void mostrarUso() {
    System.out.println("Uso:");
    System.out.println("  ensamblador.Main broker [puertoBroker] [puertoDominio] [puertoCliente...]");
    System.out.println("  ensamblador.Main dominio [puertoDominio] [puertoBroker] [segundosEspera] [intervaloMs]");
    System.out.println("  ensamblador.Main cliente <nombre> <puertoCliente> [puertoBroker] [hostBroker]");
    System.out.println();
    System.out.println("Ejemplo:");
    System.out.println("  ensamblador.Main broker 7000 7001 7002 7003 7004");
    System.out.println("  ensamblador.Main dominio 7001 7000 30");
    System.out.println("  ensamblador.Main cliente Miguel 7002");
    System.out.println("  ensamblador.Main cliente Ana 7003");
    System.out.println("  ensamblador.Main cliente Luis 7004");
  }
}
