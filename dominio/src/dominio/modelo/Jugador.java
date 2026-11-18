package dominio.modelo;

import java.util.Objects;
import java.util.UUID;

public final class Jugador {

  private final String id;
  private final String nombre;
  private final Tabla tabla;
  private int puntos;

  public Jugador(String nombre, Tabla tabla) {
    this(UUID.randomUUID().toString(), nombre, tabla);
  }

  public Jugador(String id, String nombre, Tabla tabla) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("El id del jugador no puede estar vacío");
    }
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
    }
    this.id = id.trim();
    this.nombre = nombre.trim();
    this.tabla = Objects.requireNonNull(tabla, "tabla");
  }

  public String obtenerId() {
    return id;
  }

  public String obtenerNombre() {
    return nombre;
  }

  public Tabla obtenerTabla() {
    return tabla;
  }

  boolean marcar(Carta carta) {
    return tabla.marcar(carta);
  }

  public boolean tieneLoteria(TipoVictoria tipoVictoria) {
    return tabla.cumple(tipoVictoria);
  }

  public int obtenerAciertos() {
    return tabla.obtenerAciertos();
  }

  public void sumarPunto() {
    puntos++;
  }

  public void sumarPuntos(int cantidad) {
    if (cantidad < 0) {
      throw new IllegalArgumentException("La cantidad de puntos no puede ser negativa");
    }
    puntos += cantidad;
  }

  public int obtenerPuntos() {
    return puntos;
  }

  @Override
  public boolean equals(Object objeto) {
    if (this == objeto) {
      return true;
    }
    if (!(objeto instanceof Jugador jugador)) {
      return false;
    }
    return id.equals(jugador.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return nombre;
  }
}
