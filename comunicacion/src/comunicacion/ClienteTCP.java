package comunicacion;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClienteTCP implements ObservadorDispatcher {

  @Override
  public void alEnviar(String host, int puerto, byte[] datos) throws IOException {
    byte[] bytesAEnviar = datos != null ? datos : new byte[0];

    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, puerto), 5000);

      try (DataOutputStream salida = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
        salida.writeInt(bytesAEnviar.length);
        salida.write(bytesAEnviar);
        salida.flush();
      }
    }
  }
}
