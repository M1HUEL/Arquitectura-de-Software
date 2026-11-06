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
}
