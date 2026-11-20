package comun.serializador;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializador implements ISerializador {

  private final ObjectInputFilter filtro;

  public Serializador(String patron) {
    this.filtro = ObjectInputFilter.Config.createFilter(patron);
  }

  @Override
  public byte[] serializar(Object objeto) {
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ObjectOutputStream salida = new ObjectOutputStream(bytes)) {
      salida.writeObject(objeto);
      salida.flush();
      return bytes.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo serializar el objeto", e);
    }
  }

  @Override
  public <T> T deserializar(byte[] datos, Class<T> tipo) {
    if (datos == null) {
      throw new IllegalArgumentException("Los datos no pueden ser nulos");
    }
    if (tipo == null) {
      throw new IllegalArgumentException("El tipo no puede ser nulo");
    }
    try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(datos))) {
      entrada.setObjectInputFilter(this.filtro);
      return tipo.cast(entrada.readObject());
    } catch (IOException | ClassNotFoundException | ClassCastException e) {
      throw new IllegalStateException("No se pudo deserializar a " + tipo.getName(), e);
    }
  }
}
