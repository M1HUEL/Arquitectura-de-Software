package red.api;

import java.io.IOException;

public interface IConexion {

  String id();

  String direccion();

  void enviar(byte[] datos) throws IOException;

  void cerrar();

  boolean estaAbierta();
}
