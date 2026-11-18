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

  public void publicar(String tema, Object contenido) {
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

  public static void main(String[] args) {
    System.err.println(
      "Use ensamblador.Main broker [puertoBroker] [puertoDominio] [puertoCliente...]");
  }
}
