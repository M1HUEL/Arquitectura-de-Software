package comun;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public record Mensaje(String tipo, byte[] contenido) {

  public static final int LONGITUD_MAXIMA_CONTENIDO = 1024 * 1024;

  public Mensaje {
    if (tipo == null || tipo.isBlank()) {
      throw new IllegalArgumentException("El tipo no puede estar vacío");
    }
    if (contenido == null) {
      throw new IllegalArgumentException("El contenido no puede ser nulo");
    }
    if (contenido.length > LONGITUD_MAXIMA_CONTENIDO) {
      throw new IllegalArgumentException(
        "El contenido excede el máximo de " + LONGITUD_MAXIMA_CONTENIDO + " bytes");
    }
    contenido = contenido.clone();
  }

  @Override
  public byte[] contenido() {
    return contenido.clone();
  }

  public byte[] aBytes() {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      try (DataOutputStream salida = new DataOutputStream(buffer)) {
        salida.writeUTF(tipo);
        salida.writeInt(contenido.length);
        salida.write(contenido);
      }
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo serializar el mensaje", e);
    }
  }

  public static Mensaje desdeBytes(byte[] datos) {
    if (datos == null) {
      throw new IllegalArgumentException("Mensaje inválido");
    }
    try (DataInputStream entrada = new DataInputStream(new ByteArrayInputStream(datos))) {
      String tipo = entrada.readUTF();
      int longitud = entrada.readInt();
      if (longitud < 0 || longitud > LONGITUD_MAXIMA_CONTENIDO) {
        throw new IllegalArgumentException("Longitud inválida: " + longitud);
      }
      byte[] contenido = new byte[longitud];
      entrada.readFully(contenido);
      if (entrada.available() != 0) {
        throw new IllegalArgumentException("El mensaje contiene datos sobrantes");
      }
      return new Mensaje(tipo, contenido);
    } catch (IOException e) {
      throw new IllegalArgumentException("Mensaje inválido", e);
    }
  }

  @Override
  public boolean equals(Object objeto) {
    if (this == objeto) {
      return true;
    }
    if (!(objeto instanceof Mensaje)) {
      return false;
    }
    Mensaje otro = (Mensaje) objeto;
    return tipo.equals(otro.tipo) && Arrays.equals(contenido, otro.contenido);
  }

  @Override
  public int hashCode() {
    return 31 * tipo.hashCode() + Arrays.hashCode(contenido);
  }
}
