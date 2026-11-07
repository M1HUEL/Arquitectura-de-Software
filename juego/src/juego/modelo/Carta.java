package juego.modelo;

public class Carta {

  private final String nombre;
  private final String imagen;

  public Carta(String nombre, String imagen) {
    this.nombre = nombre;
    this.imagen = imagen;
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
}
