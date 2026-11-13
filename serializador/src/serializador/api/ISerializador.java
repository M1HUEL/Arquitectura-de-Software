package serializador.api;

public interface ISerializador {

  byte[] serializar(Object objeto);

  <T> T deserializar(byte[] datos, Class<T> tipo);
}
