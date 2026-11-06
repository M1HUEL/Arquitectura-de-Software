package red.api;

public interface IDispatcher {

  void enviar(String host, int puerto, byte[] datos);

  void detener();

  boolean estaActivo();
}
