package dominio.modelo;

import java.util.Objects;

public final class Casilla {

  private final int fila;
  private final int columna;
  private final Carta carta;
  private boolean marcada;

  public Casilla(int fila, int columna, Carta carta) {
    if (fila < 0 || columna < 0) {
      throw new IllegalArgumentException("La fila y la columna no pueden ser negativas");
    }
    this.fila = fila;
    this.columna = columna;
    this.carta = Objects.requireNonNull(carta, "carta");
  }

  public int obtenerFila() {
    return fila;
  }

  public int obtenerColumna() {
    return columna;
  }

  public Carta obtenerCarta() {
    return carta;
  }

  public boolean estaMarcada() {
    return marcada;
  }

  boolean marcar() {
    if (marcada) {
      return false;
    }
    marcada = true;
    return true;
  }

  public boolean contiene(Carta carta) {
    return this.carta.equals(carta);
  }

  @Override
  public boolean equals(Object objeto) {
    if (this == objeto) {
      return true;
    }
    if (!(objeto instanceof Casilla casilla)) {
      return false;
    }
    return fila == casilla.fila && columna == casilla.columna;
  }

  @Override
  public int hashCode() {
    return Objects.hash(fila, columna);
  }

  @Override
  public String toString() {
    return "(" + fila + ", " + columna + ") " + carta;
  }
}
