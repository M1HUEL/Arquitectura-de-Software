package dominio.modelo;

import java.util.Objects;

public final class Carta {

  private final int id;
  private final String nombre;

  public Carta(int id, String nombre) {
    if (id <= 0) {
      throw new IllegalArgumentException("El id de la carta debe ser positivo");
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
    return id == carta.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return id + " - " + nombre;
  }
}
