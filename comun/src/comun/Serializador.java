package comun;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class Serializador {

  private Serializador() {
    // ...
  }

  public static byte[] serializar(Escritor escritor) {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      try (DataOutputStream salida = new DataOutputStream(buffer)) {
        escritor.escribir(salida);
      }
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo serializar", e);
    }
  }

  @FunctionalInterface
  public interface Escritor {

    void escribir(DataOutputStream salida) throws IOException;
  }
}
