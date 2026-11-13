package serializador;

import serializador.api.SerializacionException;
import serializador.api.ISerializador;
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
      throw new SerializacionException("No se pudo serializar " + objeto.getClass(), e);
    }
  }

  @Override
  public <T> T deserializar(byte[] datos, Class<T> tipo) {
    try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(datos))) {
      entrada.setObjectInputFilter(this.filtro);
      return tipo.cast(entrada.readObject());
    } catch (IOException | ClassNotFoundException | ClassCastException e) {
      throw new SerializacionException("No se pudo deserializar a " + tipo, e);
    }
  }
}
