package juego.modelo;

public class Carta {

  private final int id;
  private final String nombre;

  public Carta(int id, String nombre) {
    if (id < 0) {
      throw new IllegalArgumentException("El id de la carta no puede ser negativo");
    }
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre de la carta no puede estar vacío");
    }
    this.id = id;
    this.nombre = nombre.trim();
  }

  public int obtenerId() {
    return id;
  }

  public String obtenerNombre() {
    return nombre;
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
