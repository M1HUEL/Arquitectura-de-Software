package broker.cliente;

import comun.Mensaje;
import comun.Protocolo;
import comun.Tipos;
import java.io.IOException;
import red.ClienteTCPPersistente;
import red.ObservadorCliente;

public final class ClienteBroker implements ObservadorCliente, AutoCloseable {

  @FunctionalInterface
  public interface Observador {

    void alRecibirPublicacion(Protocolo.Publicacion publicacion);

    default void alSuscribir(String tema) {
    }

    default void alError(Protocolo.Error error) {
    }

    default void alDesconectar(Throwable causa) {
    }
  }

  private final ClienteTCPPersistente conexion;
  private final Observador observador;

  public ClienteBroker(String host, int puerto, Observador observador) throws IOException {
    if (observador == null) {
      throw new IllegalArgumentException("El observador no puede ser nulo");
    }
    this.observador = observador;
    this.conexion = new ClienteTCPPersistente(host, puerto, this);
  }

  public void suscribir(String tema) throws IOException {
    enviar(Protocolo.suscribir(tema));
  }

  public void desuscribir(String tema) throws IOException {
    enviar(Protocolo.desuscribir(tema));
  }

  public void publicar(String tema, byte[] contenido) throws IOException {
    enviar(Protocolo.publicar(tema, contenido));
  }

  public boolean estaConectado() {
    return this.conexion.estaActivo();
  }

  @Override
  public void alRecibir(byte[] datos) {
    try {
      procesar(Mensaje.desdeBytes(datos));
    } catch (RuntimeException e) {
      notificarError("cliente", e.getMessage());
    }
  }

  @Override
  public void alDesconectar(Throwable causa) {
    this.observador.alDesconectar(causa);
  }

  @Override
  public void close() {
    this.conexion.cerrar();
  }

  private void procesar(Mensaje mensaje) {
    switch (mensaje.tipo()) {
      case Tipos.PUBLICACION -> this.observador.alRecibirPublicacion(
        Protocolo.leerPublicacion(mensaje));
      case Tipos.SUSCRITO -> this.observador.alSuscribir(Protocolo.leerTema(mensaje));
      case Tipos.ERROR -> this.observador.alError(Protocolo.leerError(mensaje));
      default -> {
        // El broker no envía otros tipos al cliente.
      }
    }
  }

  private void enviar(Mensaje mensaje) throws IOException {
    if (!this.conexion.estaActivo()) {
      throw new IOException("La conexión con el broker está cerrada");
    }
    this.conexion.enviar(mensaje.aBytes());
  }

  private void notificarError(String codigo, String detalle) {
    try {
      this.observador.alError(new Protocolo.Error(codigo, detalle == null ? "" : detalle));
    } catch (RuntimeException ignored) {
      // El observador ya no está disponible.
    }
  }
}
