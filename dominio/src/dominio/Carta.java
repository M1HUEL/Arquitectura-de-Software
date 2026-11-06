package dominio;

import java.util.Objects;

public final class Carta {

  private final String nombre;
  private final String imagen;

  public Carta(String nombre, String imagen) {
    this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo.");
    this.imagen = Objects.requireNonNull(imagen, "La imagen no puede ser nula.");
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
    if (!(objeto instanceof Carta)) {
      return false;
    }
    Carta carta = (Carta) objeto;
    return nombre.equals(carta.nombre);
  }

  @Override
  public int hashCode() {
    return nombre.hashCode();
  }

  @Override
  public String toString() {
    return nombre;
  }
}
