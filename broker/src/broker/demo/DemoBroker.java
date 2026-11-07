package broker.demo;

import broker.Broker;
import broker.cliente.ClienteBroker;
import comun.Protocolo;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class DemoBroker {

  private static final String HOST = "127.0.0.1";
  private static final String TEMA = "partida.evento";
  private static final long ESPERA_SEGUNDOS = 5;

  public static void main(String[] args) throws Exception {
    Broker broker = new Broker();
    broker.iniciar(0);
    int puerto = broker.obtenerPuerto();
    System.out.println("Demo: broker escuchando en el puerto " + puerto);

    Suscriptor uno = new Suscriptor("suscriptor-1", TEMA);
    Suscriptor dos = new Suscriptor("suscriptor-2", TEMA);

    try {
      uno.conectar(HOST, puerto);
      dos.conectar(HOST, puerto);

      ClienteBroker.Observador observador = publicacion -> {
        // El publicador no está suscrito a ningún tema.
      };

      try (ClienteBroker publicador = new ClienteBroker(HOST, puerto, observador)) {
        publicar(publicador, "evento-1");
        publicar(publicador, "evento-2");
        publicar(publicador, "evento-3");

        uno.imprimir(3);
        dos.imprimir(3);

        dos.desuscribir();
        esperarHasta(() -> broker.cantidadSuscriptores(TEMA) == 1,
          "El broker no aplicó la desuscripción");
        System.out.println("suscriptor-2: desuscrito de " + TEMA);

        publicar(publicador, "evento-4");
        uno.imprimir(1);
        dos.verificarSinMensajes();
      }
    } finally {
      uno.cerrar();
      dos.cerrar();
      broker.detener();
    }

    System.out.println("Demo completada");
  }

  private static void publicar(ClienteBroker publicador, String texto) throws IOException {
    System.out.println("publicador: publica " + TEMA + " -> " + texto);
    publicador.publicar(TEMA, texto.getBytes(StandardCharsets.UTF_8));
  }

  private static void esperarHasta(BooleanSupplier condicion, String mensaje)
      throws InterruptedException {
    long limite = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ESPERA_SEGUNDOS);
    while (!condicion.getAsBoolean() && System.currentTimeMillis() < limite) {
      Thread.sleep(10);
    }
    if (!condicion.getAsBoolean()) {
      throw new IllegalStateException(mensaje);
    }
  }

  private static final class Suscriptor implements ClienteBroker.Observador {

    private final String nombre;
    private final String tema;
    private final BlockingQueue<Protocolo.Publicacion> recibidas = new LinkedBlockingQueue<>();
    private final CountDownLatch suscripcion = new CountDownLatch(1);
    private ClienteBroker cliente;

    private Suscriptor(String nombre, String tema) {
      this.nombre = nombre;
      this.tema = tema;
    }

    private void conectar(String host, int puerto) throws IOException, InterruptedException {
      this.cliente = new ClienteBroker(host, puerto, this);
      this.cliente.suscribir(this.tema);
      if (!this.suscripcion.await(ESPERA_SEGUNDOS, TimeUnit.SECONDS)) {
        throw new IllegalStateException(this.nombre + ": sin confirmación de suscripción");
      }
      System.out.println(this.nombre + ": suscrito a " + this.tema);
    }

    @Override
    public void alRecibirPublicacion(Protocolo.Publicacion publicacion) {
      this.recibidas.add(publicacion);
    }

    @Override
    public void alSuscribir(String tema) {
      if (this.tema.equals(tema)) {
        this.suscripcion.countDown();
      }
    }

    @Override
    public void alError(Protocolo.Error error) {
      System.err.println(this.nombre + ": error " + error.codigo() + ": " + error.detalle());
    }

    @Override
    public void alDesconectar(Throwable causa) {
      System.err.println(this.nombre + ": desconectado"
        + (causa == null ? "" : ": " + causa.getMessage()));
    }

    private void imprimir(int cantidad) throws InterruptedException {
      List<String> textos = new ArrayList<>();
      for (int i = 0; i < cantidad; i++) {
        Protocolo.Publicacion publicacion = this.recibidas.poll(
          ESPERA_SEGUNDOS, TimeUnit.SECONDS);
        if (publicacion == null) {
          throw new IllegalStateException(this.nombre + ": faltan mensajes ("
            + textos.size() + "/" + cantidad + ")");
        }
        textos.add(new String(publicacion.contenido(), StandardCharsets.UTF_8));
      }
      System.out.println(this.nombre + " recibió: " + textos);
    }

    private void verificarSinMensajes() throws InterruptedException {
      Protocolo.Publicacion sobrante = this.recibidas.poll(1, TimeUnit.SECONDS);
      if (sobrante != null) {
        throw new IllegalStateException(
          this.nombre + ": recibió un tema al que ya no está suscrito");
      }
      System.out.println(this.nombre + ": no recibió mensajes tras desuscribirse");
    }

    private void desuscribir() throws IOException {
      this.cliente.desuscribir(this.tema);
    }

    private void cerrar() {
      if (this.cliente != null) {
        this.cliente.close();
      }
    }
  }
}
