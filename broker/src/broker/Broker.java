package broker;

import comun.EventoBroker;
import comunicacion.api.IDispatcher;
import comunicacion.api.IReceptorExterno;
import java.util.List;
import serializador.ISerializador;

public class Broker implements IReceptorExterno {

  private final ISerializador serializador;
  private final IDispatcher dispatcher;
  private final GestorSuscriptores gestor;

  public Broker(ISerializador serializador, IDispatcher dispatcher) {
    if (serializador == null) {
      throw new IllegalArgumentException("El serializador no puede ser nulo");
    }
    if (dispatcher == null) {
      throw new IllegalArgumentException("El dispatcher no puede ser nulo");
    }
    this.serializador = serializador;
    this.dispatcher = dispatcher;
    this.gestor = new GestorSuscriptores();
  }

  public void suscribir(String tema, String host, int puerto) {
    gestor.agregar(tema, host, puerto);
  }

  public void desuscribir(String tema, String host, int puerto) {
    gestor.eliminar(tema, host, puerto);
  }

  public void publicar(String tema, byte[] evento) {
    List<GestorSuscriptores.Suscriptor> suscriptores = gestor.obtener(tema);
    if (suscriptores.isEmpty()) {
      return;
    }
    for (GestorSuscriptores.Suscriptor suscriptor : suscriptores) {
      dispatcher.enviar(suscriptor.host(), suscriptor.puerto(), evento);
    }
  }

  @Override
  public void alRecibir(byte[] evento) {
    Object sobre = serializador.deserializar(evento, Object.class);
    if (sobre instanceof EventoBroker mensaje) {
      publicar(mensaje.tema(), evento);
    }
  }

  public static void main(String[] args) {
    System.err.println(
      "Use ensamblador.Main broker [puertoBroker] [puertoDominio] [puertoCliente...]");
  }
}
