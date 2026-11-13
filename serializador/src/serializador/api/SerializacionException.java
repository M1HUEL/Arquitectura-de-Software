package serializador.api;

public class SerializacionException extends RuntimeException {

  public SerializacionException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
