package red;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClienteTCPPersistente implements AutoCloseable {

  private static final int TIEMPO_CONEXION = 5000;
  private static final int LONGITUD_MAXIMA_MENSAJE = 2 * 1024 * 1024;

  private final Socket socket;
  private final DataOutputStream salida;
  private final ObservadorCliente observador;
  private final Thread hilo;
  private volatile boolean activo = true;

  public ClienteTCPPersistente(String host, int puerto, ObservadorCliente observador)
    throws IOException {
    if (host == null || host.isBlank()) {
      throw new IllegalArgumentException("El host no puede estar vacío");
    }
    if (puerto <= 0 || puerto > 65535) {
      throw new IllegalArgumentException("Puerto inválido: " + puerto);
    }
    if (observador == null) {
      throw new IllegalArgumentException("El observador no puede ser nulo");
    }

    this.observador = observador;
    this.socket = new Socket();
    try {
      this.socket.connect(new InetSocketAddress(host, puerto), TIEMPO_CONEXION);
      this.socket.setTcpNoDelay(true);
      this.salida = new DataOutputStream(
        new BufferedOutputStream(this.socket.getOutputStream()));
    } catch (IOException e) {
      cerrarSilencioso(this.socket);
      throw e;
    }

    this.hilo = new Thread(this::leer, "lector-cliente-" + host + ":" + puerto);
    this.hilo.setDaemon(true);
    this.hilo.start();
  }

  public void enviar(byte[] datos) throws IOException {
    if (datos == null) {
      throw new IllegalArgumentException("Los datos no pueden ser nulos");
    }
    synchronized (this.salida) {
      if (!this.activo) {
        throw new IOException("El cliente está cerrado");
      }
      this.salida.writeInt(datos.length);
      this.salida.write(datos);
      this.salida.flush();
    }
  }

  public boolean estaActivo() {
    return this.activo && !this.socket.isClosed();
  }

  public void cerrar() {
    if (!this.activo) {
      return;
    }
    this.activo = false;
    cerrarSilencioso(this.socket);
  }

  @Override
  public void close() {
    cerrar();
  }

  private void leer() {
    Throwable causa = null;
    try (DataInputStream entrada = new DataInputStream(
      new BufferedInputStream(this.socket.getInputStream()))) {
      while (this.activo && !this.socket.isClosed()) {
        int longitud = entrada.readInt();
        if (longitud <= 0 || longitud > LONGITUD_MAXIMA_MENSAJE) {
          throw new IOException("Longitud de mensaje inválida: " + longitud);
        }
        byte[] datos = new byte[longitud];
        entrada.readFully(datos);
        this.observador.alRecibir(datos);
      }
    } catch (IOException | RuntimeException e) {
      causa = e;
    } finally {
      boolean avisar = this.activo;
      this.activo = false;
      cerrarSilencioso(this.socket);
      if (avisar) {
        try {
          this.observador.alDesconectar(causa);
        } catch (RuntimeException e) {
          System.err.println("Error al notificar la desconexión: " + e.getMessage());
        }
      }
    }
  }

  private static void cerrarSilencioso(Socket socket) {
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }
}
