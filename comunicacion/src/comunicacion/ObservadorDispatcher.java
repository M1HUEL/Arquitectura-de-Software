package comunicacion;

import java.io.IOException;

public interface ObservadorDispatcher {

  void alEnviar(String host, int puerto, byte[] datos) throws IOException;
}
