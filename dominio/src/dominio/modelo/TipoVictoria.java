package dominio.modelo;

public enum TipoVictoria {
  CHORRO("Chorro"),
  CUATRO_ESQUINAS("Cuatro esquinas"),
  CENTRO("Centro"),
  TABLA_LLENA("Tabla llena");

  private final String descripcion;

  TipoVictoria(String descripcion) {
    this.descripcion = descripcion;
  }

  public String obtenerDescripcion() {
    return descripcion;
  }
}
