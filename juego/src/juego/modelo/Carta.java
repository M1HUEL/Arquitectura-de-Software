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

  public static Carta crear(int id, String nombre) {
    return new Carta(id, nombre, crearNombreImagen(nombre));
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

  private static String crearNombreImagen(String nombre) {
    return nombre.toLowerCase()
      .replace("á", "a")
      .replace("é", "e")
      .replace("í", "i")
      .replace("ó", "o")
      .replace("ú", "u")
      .replace("ñ", "n")
      .replace(" ", "_")
      + ".png";
  }
}
