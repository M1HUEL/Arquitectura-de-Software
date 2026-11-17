package ensamblador;

import broker.Broker;
import comunicacion.Dispatcher;
import comunicacion.DispatcherFactory;
import comunicacion.Receptor;
import dominio.Dominio;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import javax.swing.SwingUtilities;
import juego.controlador.ControladorJuego;
import juego.fachada.FachadaBroker;
import juego.modelo.ModeloJuego;
import juego.vista.VistaJuego;
import serializador.Serializador;

public class Main {

  private static final String PATRON_FILTRO = "comun.*;java.lang.*;java.util.*;!*";
  private static final String HOST = "127.0.0.1";
  private static final int PUERTO_BROKER = 7000;
  private static final int PUERTO_DOMINIO = 7001;
  private static final int PUERTO_MVC = 7002;
  private static final String TEMA_COMANDOS = "dominio";
  private static final String TEMA_EVENTOS = "juego";
  private static final String JUGADOR_UNO = "Jugador 1";
  private static final String JUGADOR_DOS = "Jugador 2";
  private static final long INTERVALO_CARTAS = 3000L;

  public static void main(String[] args) throws InterruptedException {
    int puertoBroker = args.length >= 1 ? Integer.parseInt(args[0]) : PUERTO_BROKER;
    int puertoDominio = args.length >= 2 ? Integer.parseInt(args[1]) : PUERTO_DOMINIO;
    int puertoMvc = args.length >= 3 ? Integer.parseInt(args[2]) : PUERTO_MVC;
    long intervalo = args.length >= 4 ? Long.parseLong(args[3]) : INTERVALO_CARTAS;

    Broker broker = new Broker(new Serializador(PATRON_FILTRO), DispatcherFactory.crear());
    Dispatcher dispatcherDominio = DispatcherFactory.crear();
    Receptor receptorDominio = new Receptor();
    Dominio dominio = new Dominio(
      new Serializador(PATRON_FILTRO),
      dispatcherDominio,
      receptorDominio,
      HOST,
      puertoBroker,
      TEMA_COMANDOS,
      TEMA_EVENTOS,
      List.of(JUGADOR_UNO, JUGADOR_DOS),
      intervalo
    );
    FachadaBroker fachadaUno = new FachadaBroker(
      JUGADOR_UNO, HOST, puertoBroker, TEMA_COMANDOS, TEMA_EVENTOS, puertoMvc);
    FachadaBroker fachadaDos = new FachadaBroker(
      JUGADOR_DOS, HOST, puertoBroker, TEMA_COMANDOS, TEMA_EVENTOS, puertoMvc + 1);

    try {
      broker.suscribir(TEMA_COMANDOS, HOST, puertoDominio);
      broker.suscribir(TEMA_EVENTOS, HOST, puertoMvc);
      broker.suscribir(TEMA_EVENTOS, HOST, puertoMvc + 1);
      broker.iniciar(puertoBroker);
      dominio.iniciar(puertoDominio);
    } catch (RuntimeException e) {
      System.err.println("No se pudo iniciar la aplicación: " + e.getMessage());
      fachadaUno.cerrar();
      fachadaDos.cerrar();
      dominio.detener();
      broker.detener();
      return;
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      fachadaUno.cerrar();
      fachadaDos.cerrar();
      dominio.detener();
      broker.detener();
    }));

    System.out.println("Broker: " + HOST + ":" + broker.obtenerPuerto());
    System.out.println("Dominio: " + HOST + ":" + puertoDominio);
    System.out.println("MVC " + JUGADOR_UNO + ": " + HOST + ":" + puertoMvc);
    System.out.println("MVC " + JUGADOR_DOS + ": " + HOST + ":" + (puertoMvc + 1));

    SwingUtilities.invokeLater(() -> {
      VistaJuego vistaUno = crearCliente(JUGADOR_UNO, fachadaUno);
      VistaJuego vistaDos = crearCliente(JUGADOR_DOS, fachadaDos);
      ubicar(vistaUno, 0, 2);
      ubicar(vistaDos, 1, 2);
      vistaUno.setVisible(true);
      vistaDos.setVisible(true);
    });

    Thread.currentThread().join();
  }

  private static VistaJuego crearCliente(String jugador, FachadaBroker fachada) {
    ModeloJuego modelo = new ModeloJuego(jugador, fachada);
    ControladorJuego controlador = new ControladorJuego(modelo);
    VistaJuego vista = new VistaJuego(modelo, controlador);
    modelo.agregarObservador(vista);
    modelo.iniciar();
    return vista;
  }

  private static void ubicar(VistaJuego vista, int indice, int total) {
    Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
    int ancho = Math.max(480, pantalla.width / total);
    int alto = Math.min(700, pantalla.height);
    vista.setSize(ancho, alto);
    vista.setLocation(indice * ancho, 0);
  }
}
