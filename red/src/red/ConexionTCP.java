package red;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import red.api.IConexion;

final class ConexionTCP implements IConexion {

  private static final AtomicLong SECUENCIA = new AtomicLong();

  private final String id = Long.toString(SECUENCIA.incrementAndGet());
  private final Socket socket;
  private final DataOutputStream salida;
  private final AtomicBoolean abierta = new AtomicBoolean(true);
  private final Object candado = new Object();

  ConexionTCP(Socket socket) throws IOException {
    this.socket = socket;
    this.salida = new DataOutputStream(
      new BufferedOutputStream(socket.getOutputStream()));
  }

  @Override
  public String id() {
    return this.id;
  }

  @Override
  public String direccion() {
    return this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort();
  }

  @Override
  public void enviar(byte[] datos) throws IOException {
    if (datos == null) {
      throw new IllegalArgumentException("Los datos no pueden ser nulos");
    }
    synchronized (this.candado) {
      if (!this.abierta.get()) {
        throw new IOException("La conexión está cerrada");
      }
      this.salida.writeInt(datos.length);
      this.salida.write(datos);
      this.salida.flush();
    }
  }

  @Override
  public void cerrar() {
    if (!this.abierta.getAndSet(false)) {
      return;
    }
    try {
      this.socket.close();
    } catch (IOException ignored) {
    }
  }

  @Override
  public boolean estaAbierta() {
    return this.abierta.get() && !this.socket.isClosed();
  }
}
