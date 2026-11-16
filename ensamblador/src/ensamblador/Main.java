package ensamblador;

import broker.Broker;
import comunicacion.Dispatcher;
import comunicacion.DispatcherFactory;
import comunicacion.Receptor;
import dominio.Dominio;
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
  private static final String JUGADOR_LOCAL = "Jugador";
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
      JUGADOR_LOCAL,
      intervalo
    );
    FachadaBroker fachada = new FachadaBroker(
      JUGADOR_LOCAL, HOST, puertoBroker, TEMA_COMANDOS, TEMA_EVENTOS, puertoMvc);

    try {
      broker.suscribir(TEMA_COMANDOS, HOST, puertoDominio);
      broker.suscribir(TEMA_EVENTOS, HOST, puertoMvc);
      broker.iniciar(puertoBroker);
      dominio.iniciar(puertoDominio);
    } catch (RuntimeException e) {
      System.err.println("No se pudo iniciar la aplicación: " + e.getMessage());
      fachada.cerrar();
      dominio.detener();
      broker.detener();
      return;
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      fachada.cerrar();
      dominio.detener();
      broker.detener();
    }));

    System.out.println("Broker: " + HOST + ":" + broker.obtenerPuerto());
    System.out.println("Dominio: " + HOST + ":" + puertoDominio);
    System.out.println("MVC: " + HOST + ":" + puertoMvc);

    SwingUtilities.invokeLater(() -> {
      ModeloJuego modelo = new ModeloJuego(JUGADOR_LOCAL, fachada);
      ControladorJuego controlador = new ControladorJuego(modelo);
      VistaJuego vista = new VistaJuego(modelo, controlador);
      modelo.agregarObservador(vista);
      vista.setVisible(true);
      modelo.iniciar();
    });

    Thread.currentThread().join();
  }
}
