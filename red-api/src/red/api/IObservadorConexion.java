package red.api;

public interface IObservadorConexion {

  void alConectar(IConexion conexion);

  void alRecibir(IConexion conexion, byte[] datos);

  void alDesconectar(IConexion conexion);
}
