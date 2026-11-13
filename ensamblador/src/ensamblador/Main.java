package ensamblador;

import comunicacion.Dispatcher;
import comunicacion.DispatcherFactory;
import comunicacion.Receptor;
import dominio.Dominio;
import juego.Juego;
import serializador.Serializador;
import serializador.api.ISerializador;

public class Main {

  private static final String MARCA_RESPUESTA = "ECO: ";

  public static void main(String[] args) throws InterruptedException {
    if (args.length < 1) {
      System.err.println("Uso: Main <puertoLocal> [hostDestino puertoDestino [hostLocal]]");
      return;
    }
    int puertoLocal = Integer.parseInt(args[0]);
    String hostLocal = args.length >= 4 ? args[3] : "127.0.0.1";

    ISerializador serializador = new Serializador("java.lang.String;!*");

    Dispatcher dispatcher = DispatcherFactory.crear();
    Receptor receptor = new Receptor();

    Dominio dominio = new Dominio(dispatcher, serializador);
    Juego juego = new Juego(dispatcher, serializador);

    receptor.agregarReceptor(dominio);
    receptor.agregarReceptor(juego);
    receptor.agregarReceptor(datos -> responder(datos, dominio, serializador, hostLocal, puertoLocal));

    dispatcher.iniciar();
    receptor.iniciar(puertoLocal);

    if (!receptor.estaActivo()) {
      System.err.println("No se pudo iniciar el receptor en el puerto " + puertoLocal);
      dispatcher.detener();
      return;
    }
    System.out.println("Escuchando en " + hostLocal + ":" + receptor.obtenerPuerto());

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      receptor.detener();
      dispatcher.detener();
    }));

    if (args.length >= 3) {
      String host = args[1];
      int puerto = Integer.parseInt(args[2]);
      String texto = "Hola desde el puerto " + puertoLocal;
      dominio.enviarMensaje(host, puerto, hostLocal + ":" + puertoLocal + "|" + texto);
    }

    Thread.currentThread().join();
  }

  private static void responder(byte[] bytes, Dominio dominio, ISerializador serializador, String hostLocal, int puertoLocal) {
    try {
      String crudo = serializador.deserializar(bytes, String.class);
      int barra = crudo.indexOf('|');
      if (barra < 0) {
        return;
      }

      String origen = crudo.substring(0, barra);
      String texto = crudo.substring(barra + 1);
      if (texto.startsWith(MARCA_RESPUESTA)) {
        return;
      }

      int dosPuntos = origen.lastIndexOf(':');
      if (dosPuntos <= 0 || dosPuntos == origen.length() - 1) {
        return;
      }

      String host = origen.substring(0, dosPuntos);
      int puerto = Integer.parseInt(origen.substring(dosPuntos + 1));

      String respuesta = MARCA_RESPUESTA + texto;
      dominio.enviarMensaje(host, puerto, hostLocal + ":" + puertoLocal + "|" + respuesta);
      System.out.println("[Respondedor] Respuesta enviada a " + host + ":" + puerto);
    } catch (Exception e) {
      System.err.println("Mensaje inválido en Respondedor: " + e.getMessage());
    }
  }
}
