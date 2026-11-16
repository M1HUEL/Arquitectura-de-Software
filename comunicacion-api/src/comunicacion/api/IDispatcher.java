package comunicacion.api;

public interface IDispatcher {

  void enviar(String host, int puerto, byte[] datos);
}
