package dominio.modelo;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Jugador {

  private final String id;
  private final String nombre;
  private final Tablero tablero;
  private final Set<TipoVictoria> patronesPuntuados = EnumSet.noneOf(TipoVictoria.class);
  private int puntos;

  public static final int PUNTOS_POR_PATRON = 5;

  public Jugador(String nombre, Tablero tablero) {
    this(UUID.randomUUID().toString(), nombre, tablero);
  }

  public Jugador(String id, String nombre, Tablero tablero) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("El id del jugador no puede estar vacío");
    }
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
    }
    this.id = id.trim();
    this.nombre = nombre.trim();
    this.tablero = Objects.requireNonNull(tablero, "tablero");
  }

  public String obtenerId() {
    return id;
  }

  public String obtenerNombre() {
    return nombre;
  }

  public Tablero obtenerTablero() {
    return tablero;
  }

  boolean marcar(Carta carta) {
    return tablero.marcar(carta);
  }

  public boolean tieneLoteria(TipoVictoria tipoVictoria) {
    return tablero.cumple(tipoVictoria);
  }

  void sumarPunto() {
    puntos++;
  }

  void sumarPuntos(int cantidad) {
    if (cantidad < 0) {
      throw new IllegalArgumentException("La cantidad de puntos no puede ser negativa");
    }
    puntos += cantidad;
  }

  public int obtenerPuntos() {
    return puntos;
  }

  public boolean puntuarPatron(TipoVictoria tipoVictoria) {
    Objects.requireNonNull(tipoVictoria, "tipoVictoria");
    if (!tieneLoteria(tipoVictoria) || !patronesPuntuados.add(tipoVictoria)) {
      return false;
    }
    sumarPuntos(PUNTOS_POR_PATRON);
    return true;
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
