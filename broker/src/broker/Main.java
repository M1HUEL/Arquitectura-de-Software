package broker;

import java.util.concurrent.CountDownLatch;

public class Main {

  private static final int PUERTO_POR_DEFECTO = 7000;

  public static void main(String[] args) throws Exception {
    int puerto = args.length > 0 ? Integer.parseInt(args[0]) : PUERTO_POR_DEFECTO;
    Broker broker = new Broker();
    broker.iniciar(puerto);
    System.out.println("Broker: escuchando en el puerto " + broker.obtenerPuerto());

    Runtime.getRuntime().addShutdownHook(new Thread(broker::detener, "cierre-broker"));

    new CountDownLatch(1).await();
  }
}
