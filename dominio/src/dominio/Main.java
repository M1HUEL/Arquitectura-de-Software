package dominio;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Main {

  private static final int CARTAS_POR_JUGADOR = 16;
  private static final String HOST_POR_DEFECTO = "127.0.0.1";
  private static final int PUERTO_POR_DEFECTO = 7000;
  private static final long INTERVALO_POR_DEFECTO_MS = 1000;

  public static void main(String[] args) throws Exception {
    String host = args.length > 0 ? args[0] : HOST_POR_DEFECTO;
    int puerto = args.length > 1 ? Integer.parseInt(args[1]) : PUERTO_POR_DEFECTO;
    long intervalo = args.length > 2
      ? Long.parseLong(args[2]) : INTERVALO_POR_DEFECTO_MS;

    Partida partida = crearPartida();
    ServicioDominio servicio = new ServicioDominio(partida, host, puerto, intervalo);
    servicio.iniciar();
    System.out.println("Dominio: conectado al broker " + host + ":" + puerto);
    System.out.println("Dominio: intervalo de cantado " + intervalo + " ms");

    Runtime.getRuntime().addShutdownHook(new Thread(servicio::close, "cierre-dominio"));
    new CountDownLatch(1).await();
  }

  private static Partida crearPartida() {
    Mazo mazo = new Mazo();
    mazo.barajar();

    List<Carta> cartas = mazo.obtenerCartas();
    Partida partida = new Partida(mazo);
    partida.agregarJugador(new Jugador(
      "Jugador 1", "jugador1.png", cartas.subList(0, CARTAS_POR_JUGADOR)));
    partida.agregarJugador(new Jugador(
      "Jugador 2", "jugador2.png",
      cartas.subList(CARTAS_POR_JUGADOR, CARTAS_POR_JUGADOR * 2)));
    partida.establecerPatron(Patron.LINEA);
    partida.iniciar();
    return partida;
  }
}
