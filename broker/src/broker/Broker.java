package broker;

import comunicacion.Dispatcher;
import comunicacion.DispatcherFactory;
import comunicacion.Receptor;
import comunicacion.api.IReceptorExterno;
import comun.EventoBroker;
import java.util.List;
import serializador.Serializador;
import serializador.ISerializador;

public class Broker implements IReceptorExterno {

  private static final String PATRON_FILTRO = "comun.*;java.lang.*;java.util.*;!*";

  private final ISerializador serializador;
  private final Dispatcher dispatcher;
  private final GestorSuscriptores gestor;
  private final Receptor receptor;

  public Broker(ISerializador serializador, Dispatcher dispatcher) {
    if (serializador == null) {
      throw new IllegalArgumentException("El serializador no puede ser nulo");
    }
    if (dispatcher == null) {
      throw new IllegalArgumentException("El dispatcher no puede ser nulo");
    }
    this.serializador = serializador;
    this.dispatcher = dispatcher;
    this.gestor = new GestorSuscriptores();
    this.receptor = new Receptor(this);
  }

  public void iniciar(int puerto) {
    if (puerto < 0 || puerto > 65535) {
      throw new IllegalArgumentException("El puerto no es válido: " + puerto);
    }
    dispatcher.iniciar();
    try {
      receptor.iniciar(puerto);
    } catch (RuntimeException e) {
      dispatcher.detener();
      throw e;
    }
    if (!receptor.estaActivo()) {
      dispatcher.detener();
      throw new IllegalStateException(
        "No se pudo iniciar el broker en el puerto " + puerto);
    }
  }

  public void detener() {
    receptor.detener();
    dispatcher.detener();
  }

  public void suscribir(String tema, String host, int puerto) {
    gestor.agregar(tema, host, puerto);
  }

  public void desuscribir(String tema, String host, int puerto) {
    gestor.eliminar(tema, host, puerto);
  }

  public void publicar(String tema, Object contenido) {
    if (!dispatcher.estaActivo()) {
      return;
    }
    List<GestorSuscriptores.Suscriptor> suscriptores = gestor.obtener(tema);
    if (suscriptores.isEmpty()) {
      return;
    }
    byte[] datos = serializador.serializar(contenido);
    for (GestorSuscriptores.Suscriptor suscriptor : suscriptores) {
      dispatcher.enviar(suscriptor.host(), suscriptor.puerto(), datos);
    }
  }

  @Override
  public void alRecibir(byte[] datos) {
    Object objeto = serializador.deserializar(datos, Object.class);
    if (objeto instanceof EventoBroker evento) {
      publicar(evento.tema(), evento.contenido());
    }
  }

  public boolean estaActivo() {
    return receptor.estaActivo();
  }

  public int obtenerPuerto() {
    return receptor.obtenerPuerto();
  }

  public List<String> obtenerTemas() {
    return gestor.obtenerTemas();
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Uso: Broker <puertoLocal> [tema host puertoDestino]...");
      return;
    }
    int puerto;
    try {
      puerto = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      System.err.println("El puerto no es válido: " + args[0]);
      return;
    }
    if ((args.length - 1) % 3 != 0) {
      System.err.println("Cada suscripción debe tener tema, host y puerto");
      return;
    }

    ISerializador serializador = new Serializador(PATRON_FILTRO);
    Dispatcher dispatcher = DispatcherFactory.crear();
    Broker broker = new Broker(serializador, dispatcher);

    try {
      for (int i = 1; i < args.length; i += 3) {
        broker.suscribir(args[i], args[i + 1], Integer.parseInt(args[i + 2]));
      }
      broker.iniciar(puerto);
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.err.println(e.getMessage());
      broker.detener();
      return;
    }

    System.out.println("Broker escuchando en el puerto " + broker.obtenerPuerto());
    Runtime.getRuntime().addShutdownHook(new Thread(broker::detener));

    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
