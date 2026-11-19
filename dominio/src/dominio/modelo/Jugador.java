package dominio.modelo;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Jugador {

  static final int PUNTOS_POR_CARTA = 1;
  static final int PUNTOS_POR_PATRON = 5;
  static final int PUNTOS_POR_LOTERIA = 10;
  static final int PENALIZACION_FALSA = 3;

  private final String id;
  private final String nombre;
  private final Tablero tablero;
  private final Set<TipoVictoria> patronesPuntuados = EnumSet.noneOf(TipoVictoria.class);
  private int puntos;

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
    if (!tablero.marcar(carta)) {
      return false;
    }
    puntos += PUNTOS_POR_CARTA;
    return true;
  }

  public boolean tieneLoteria(TipoVictoria tipoVictoria) {
    return tablero.cumple(tipoVictoria);
  }

  boolean declararPatron(TipoVictoria patron) {
    Objects.requireNonNull(patron, "patron");
    if (!tieneLoteria(patron) || !patronesPuntuados.add(patron)) {
      return false;
    }
    puntos += PUNTOS_POR_PATRON;
    return true;
  }

  void premiarLoteria() {
    puntos += PUNTOS_POR_LOTERIA;
  }

  void penalizarFalsa() {
    puntos = Math.max(0, puntos - PENALIZACION_FALSA);
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
