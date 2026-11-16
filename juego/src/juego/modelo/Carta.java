package juego.modelo;

public class Carta {

  private final String nombre;
  private final String imagen;

  public Carta(String nombre, String imagen) {
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre de la carta no puede estar vacío");
    }
    this.nombre = nombre.trim();
    this.imagen = imagen == null ? "" : imagen;
  }

  public String obtenerNombre() {
    return nombre;
  }

  public String obtenerImagen() {
    return imagen;
  }

  @Override
  public boolean equals(Object objeto) {
    if (this == objeto) {
      return true;
    }
    if (!(objeto instanceof Carta carta)) {
      return false;
    }
    return nombre.equals(carta.nombre);
  }

  @Override
  public int hashCode() {
    return nombre.hashCode();
  }
}
