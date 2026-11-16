package dominio.modelo;

public enum TipoVictoria {
  LINEA("Línea"),
  CUADRO("Cuadro"),
  CUATRO_ESQUINAS("Cuatro esquinas"),
  TABLA_LLENA("Tabla llena");

  private final String descripcion;

  TipoVictoria(String descripcion) {
    this.descripcion = descripcion;
  }

  public String obtenerDescripcion() {
    return descripcion;
  }
}
