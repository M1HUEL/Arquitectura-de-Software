package juego.modelo;

public class Carta {

  private final int id;
  private final String nombre;
  private final String imagen;

  public Carta(int id, String nombre, String imagen) {
    if (id < 0) {
      throw new IllegalArgumentException("El id de la carta no puede ser negativo");
    }
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre de la carta no puede estar vacío");
    }
    this.id = id;
    this.nombre = nombre.trim();
    this.imagen = imagen == null ? "" : imagen;
  }

  public int obtenerId() {
    return id;
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
