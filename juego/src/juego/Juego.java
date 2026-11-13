package juego;

import comunicacion.api.IDispatcher;
import comunicacion.api.IReceptorExterno;
import serializador.api.ISerializador;
import serializador.api.SerializacionException;

public class Juego implements IReceptorExterno {

  private final IDispatcher dispatcher;
  private final ISerializador serializador;

  public Juego(IDispatcher dispatcher, ISerializador serializador) {
    this.dispatcher = dispatcher;
    this.serializador = serializador;
  }

  @Override
  public void alRecibir(byte[] bytes) {
    try {
      String texto = this.serializador.deserializar(bytes, String.class);
      System.out.println("[Juego] Mensaje recibido: " + texto);
    } catch (SerializacionException e) {
      System.err.println("Mensaje inválido en Juego: " + e.getMessage());
    }
  }

  public void enviarMensaje(String host, int puerto, String texto) {
    this.dispatcher.enviar(host, puerto, this.serializador.serializar(texto));
  }
}
