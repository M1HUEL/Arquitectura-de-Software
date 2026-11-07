package comun;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public final class Protocolo {

  public static final int LONGITUD_MAXIMA_TEMA = 256;
  public static final int LONGITUD_MAXIMA_DETALLE = 1024;

  private Protocolo() {
    // ...
  }

  public static Mensaje suscribir(String tema) {
    validarTema(tema);
    return new Mensaje(Tipos.SUSCRIBIR, escribirTema(tema));
  }

  public static Mensaje desuscribir(String tema) {
    validarTema(tema);
    return new Mensaje(Tipos.DESUSCRIBIR, escribirTema(tema));
  }

  public static Mensaje publicar(String tema, byte[] contenido) {
    return new Mensaje(Tipos.PUBLICAR, escribirPublicacion(tema, contenido));
  }

  public static Mensaje publicacion(String tema, byte[] contenido) {
    return new Mensaje(Tipos.PUBLICACION, escribirPublicacion(tema, contenido));
  }

  public static Mensaje suscrito(String tema) {
    validarTema(tema);
    return new Mensaje(Tipos.SUSCRITO, escribirTema(tema));
  }

  public static Mensaje error(String codigo, String detalle) {
    if (codigo == null || codigo.isBlank()) {
      throw new IllegalArgumentException("El código no puede estar vacío");
    }
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      try (DataOutputStream salida = new DataOutputStream(buffer)) {
        salida.writeUTF(codigo);
        salida.writeUTF(detalle == null ? "" : recortar(detalle));
      }
      return new Mensaje(Tipos.ERROR, buffer.toByteArray());
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo serializar el error", e);
    }
  }

  public static String leerTema(Mensaje mensaje) {
    exigirTipo(mensaje, Tipos.SUSCRIBIR, Tipos.DESUSCRIBIR, Tipos.SUSCRITO);
    try (DataInputStream entrada = new DataInputStream(
      new ByteArrayInputStream(mensaje.contenido()))) {
      String tema = entrada.readUTF();
      validarTema(tema);
      exigirFin(entrada);
      return tema;
    } catch (IOException e) {
      throw new IllegalArgumentException("Tema inválido", e);
    }
  }

  public static Publicacion leerPublicacion(Mensaje mensaje) {
    exigirTipo(mensaje, Tipos.PUBLICAR, Tipos.PUBLICACION);
    try (DataInputStream entrada = new DataInputStream(
      new ByteArrayInputStream(mensaje.contenido()))) {
      String tema = entrada.readUTF();
      validarTema(tema);
      int longitud = entrada.readInt();
      if (longitud < 0 || longitud > Mensaje.LONGITUD_MAXIMA_CONTENIDO) {
        throw new IllegalArgumentException("Longitud de contenido inválida: " + longitud);
      }
      byte[] contenido = new byte[longitud];
      entrada.readFully(contenido);
      exigirFin(entrada);
      return new Publicacion(tema, contenido);
    } catch (IOException e) {
      throw new IllegalArgumentException("Publicación inválida", e);
    }
  }

  public static Error leerError(Mensaje mensaje) {
    exigirTipo(mensaje, Tipos.ERROR);
    try (DataInputStream entrada = new DataInputStream(
      new ByteArrayInputStream(mensaje.contenido()))) {
      String codigo = entrada.readUTF();
      String detalle = entrada.readUTF();
      exigirFin(entrada);
      return new Error(codigo, detalle);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error inválido", e);
    }
  }

  public static Mensaje comoPublicacion(Mensaje mensaje) {
    leerPublicacion(mensaje);
    return new Mensaje(Tipos.PUBLICACION, mensaje.contenido());
  }

  public static void validarTema(String tema) {
    if (tema == null || tema.isBlank()) {
      throw new IllegalArgumentException("El tema no puede estar vacío");
    }
    if (tema.length() > LONGITUD_MAXIMA_TEMA) {
      throw new IllegalArgumentException(
        "El tema excede el máximo de " + LONGITUD_MAXIMA_TEMA + " caracteres");
    }
    if (tema.startsWith("$")) {
      throw new IllegalArgumentException("El tema no puede empezar con $");
    }
  }

  private static byte[] escribirTema(String tema) {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      try (DataOutputStream salida = new DataOutputStream(buffer)) {
        salida.writeUTF(tema);
      }
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo serializar el tema", e);
    }
  }

  private static byte[] escribirPublicacion(String tema, byte[] contenido) {
    validarTema(tema);
    if (contenido == null) {
      throw new IllegalArgumentException("El contenido no puede ser nulo");
    }
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      try (DataOutputStream salida = new DataOutputStream(buffer)) {
        salida.writeUTF(tema);
        salida.writeInt(contenido.length);
        salida.write(contenido);
      }
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo serializar la publicación", e);
    }
  }

  private static void exigirTipo(Mensaje mensaje, String... tipos) {
    if (mensaje == null) {
      throw new IllegalArgumentException("El mensaje no puede ser nulo");
    }
    for (String tipo : tipos) {
      if (tipo.equals(mensaje.tipo())) {
        return;
      }
    }
    throw new IllegalArgumentException("Tipo de mensaje inesperado: " + mensaje.tipo());
  }

  private static void exigirFin(DataInputStream entrada) throws IOException {
    if (entrada.available() != 0) {
      throw new IllegalArgumentException("El mensaje contiene datos sobrantes");
    }
  }

  private static String recortar(String detalle) {
    if (detalle.length() <= LONGITUD_MAXIMA_DETALLE) {
      return detalle;
    }
    return detalle.substring(0, LONGITUD_MAXIMA_DETALLE);
  }

  public record Publicacion(String tema, byte[] contenido) {

    public Publicacion {
      validarTema(tema);
      if (contenido == null) {
        throw new IllegalArgumentException("El contenido no puede ser nulo");
      }
      contenido = contenido.clone();
    }

    @Override
    public byte[] contenido() {
      return contenido.clone();
    }

    @Override
    public boolean equals(Object objeto) {
      if (this == objeto) {
        return true;
      }
      if (!(objeto instanceof Publicacion)) {
        return false;
      }
      Publicacion otra = (Publicacion) objeto;
      return tema.equals(otra.tema) && Arrays.equals(contenido, otra.contenido);
    }

    @Override
    public int hashCode() {
      return 31 * tema.hashCode() + Arrays.hashCode(contenido);
    }
  }

  public record Error(String codigo, String detalle) {

  }
}
