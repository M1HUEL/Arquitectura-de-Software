package red;

public interface ObservadorCliente {

  void alRecibir(byte[] datos);

  default void alDesconectar(Throwable causa) {
  }
}
