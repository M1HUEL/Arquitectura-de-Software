package dominio;

import comun.Mensaje;
import comun.Serializador;
import comun.Tipos;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import red.ClienteTCP;
import red.Dispatcher;
import red.Receptor;
import red.api.IDispatcher;
import red.api.IReceptorExterno;

public class Test implements IReceptorExterno {

  private final IDispatcher dispatcher;
  private final String hostJuego;
  private final int puertoJuego;

  public Test(IDispatcher dispatcher, String hostJuego, int puertoJuego) {
    this.dispatcher = dispatcher;
    this.hostJuego = hostJuego;
    this.puertoJuego = puertoJuego;
  }

  public static void main(String[] args) throws Exception {
    int puertoEscucha = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
    String hostJuego = args.length > 1 ? args[1] : "127.0.0.1";
    int puertoJuego = args.length > 2 ? Integer.parseInt(args[2]) : 5001;

    IDispatcher dispatcher = new Dispatcher(new ClienteTCP());
    Test dominio = new Test(dispatcher, hostJuego, puertoJuego);
    Receptor receptor = new Receptor(dominio);
    receptor.iniciar(puertoEscucha);
    System.out.println("Dominio: escuchando en el puerto " + puertoEscucha);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      receptor.detener();
      dispatcher.detener();
    }, "cierre-dominio"));

    new CountDownLatch(1).await();
  }

  public void enviarMensaje(String jugador, int x, int y) {
    System.out.println("Dominio: enviando confirmación de movimiento de "
      + jugador + " a (" + x + ", " + y + ")");
    byte[] contenido = Serializador.serializar(salida -> {
      salida.writeUTF(jugador);
      salida.writeInt(x);
      salida.writeInt(y);
    });
    Mensaje mensaje = new Mensaje(Tipos.MOVIMIENTO_PROCESADO, contenido);
    dispatcher.enviar(hostJuego, puertoJuego, mensaje.aBytes());
  }

  @Override
  public void alRecibir(byte[] datos) {
    Mensaje mensaje = Mensaje.desdeBytes(datos);
    if (!Tipos.MOVIMIENTO.equals(mensaje.tipo())) {
      return;
    }
    try (DataInputStream entrada = new DataInputStream(
      new ByteArrayInputStream(mensaje.contenido()))) {
      String jugador = entrada.readUTF();
      int x = entrada.readInt();
      int y = entrada.readInt();
      System.out.println("Dominio: movimiento recibido de "
        + jugador + " a (" + x + ", " + y + ")");
      enviarMensaje(jugador, x, y);
    } catch (IOException e) {
      throw new IllegalArgumentException("Contenido inválido", e);
    }
  }
}
