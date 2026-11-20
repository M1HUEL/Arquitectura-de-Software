package broker;

import comun.evento.Evento;
import comun.evento.Tema;
import comunicacion.api.IDispatcher;
import comunicacion.api.IReceptorExterno;
import comun.serializador.ISerializador;
import java.util.List;

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

  public void suscribir(Tema tema, String host, int puerto) {
    gestor.agregar(tema, host, puerto);
  }

  public void desuscribir(Tema tema, String host, int puerto) {
    gestor.eliminar(tema, host, puerto);
  }

  public void publicar(Tema tema, byte[] evento) {
    List<Suscriptor> suscriptores = gestor.obtener(tema);
    if (suscriptores.isEmpty()) {
      return;
    }
    for (Suscriptor suscriptor : suscriptores) {
      dispatcher.enviar(suscriptor.obtenerHost(), suscriptor.obtenerPuerto(), evento);
    }
  }

  @Override
  public void alRecibir(byte[] evento) {
    Object sobre = serializador.deserializar(evento, Object.class);
    if (sobre instanceof Evento mensaje) {
      publicar(mensaje.tema(), evento);
    }
  }

  public static void main(String[] args) {
    System.err.println(
      "Use ensamblador.Main broker [puertoBroker] [puertoDominio] [puertoCliente...]");
  }
}
