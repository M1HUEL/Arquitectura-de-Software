package red;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

public class ClienteTCP implements ObservadorDispatcher, AutoCloseable {

  private static final int TIEMPO_CONEXION = 5000;

  private volatile boolean cerrado;

  @Override
  public void alEnviar(String host, int puerto, byte[] datos) throws IOException {
    Objects.requireNonNull(host, "El host no puede ser nulo");
    Objects.requireNonNull(datos, "Los datos no pueden ser nulos");
    if (this.cerrado) {
      throw new IOException("El cliente TCP está cerrado");
    }

    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, puerto), TIEMPO_CONEXION);
      socket.setTcpNoDelay(true);
      try (DataOutputStream salida = new DataOutputStream(
        new BufferedOutputStream(socket.getOutputStream()))) {
        salida.writeInt(datos.length);
        salida.write(datos);
        salida.flush();
      }
    }
  }

  @Override
  public void cerrar() {
    this.cerrado = true;
  }

  @Override
  public void close() {
    cerrar();
  }
}
