package dominio.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public final class Tablero {

  public static final int FILAS = 4;
  public static final int COLUMNAS = 4;
  public static final int TOTAL_CARTAS = FILAS * COLUMNAS;

  private final Casilla[][] casillas;

  public Tablero(List<Carta> cartas) {
    Objects.requireNonNull(cartas, "cartas");
    if (cartas.size() != TOTAL_CARTAS) {
      throw new IllegalArgumentException("El tablero debe tener exactamente " + TOTAL_CARTAS + " cartas");
    }
    for (Carta carta : cartas) {
      Objects.requireNonNull(carta, "Las cartas no pueden contener valores nulos");
    }
    Set<Carta> unicas = new HashSet<>(cartas);
    if (unicas.size() != TOTAL_CARTAS) {
      throw new IllegalArgumentException("El tablero no puede contener cartas repetidas");
    }
    this.casillas = new Casilla[FILAS][COLUMNAS];
    for (int i = 0; i < TOTAL_CARTAS; i++) {
      int fila = i / COLUMNAS;
      int columna = i % COLUMNAS;
      this.casillas[fila][columna] = new Casilla(fila, columna, cartas.get(i));
    }
  }

  public static Tablero generar(Random aleatorio) {
    Objects.requireNonNull(aleatorio, "aleatorio");
    List<Carta> disponibles = new ArrayList<>(CatalogoCartas.obtenerTodas());
    Collections.shuffle(disponibles, aleatorio);
    return new Tablero(disponibles.subList(0, TOTAL_CARTAS));
  }

  public Casilla obtenerCasilla(int fila, int columna) {
    validarPosicion(fila, columna);
    return casillas[fila][columna];
  }

  public Carta obtenerCarta(int fila, int columna) {
    return obtenerCasilla(fila, columna).obtenerCarta();
  }

  public boolean estaMarcada(int fila, int columna) {
    return obtenerCasilla(fila, columna).estaMarcada();
  }

  public boolean contiene(Carta carta) {
    return buscarCasilla(carta) != null;
  }

  boolean marcar(Carta carta) {
    Casilla casilla = buscarCasilla(carta);
    return casilla != null && casilla.marcar();
  }

  boolean desmarcar(Carta carta) {
    Casilla casilla = buscarCasilla(carta);
    return casilla != null && casilla.desmarcar();
  }

  void reiniciar() {
    for (int fila = 0; fila < FILAS; fila++) {
      for (int columna = 0; columna < COLUMNAS; columna++) {
        casillas[fila][columna].reiniciar();
      }
    }
  }

  public int obtenerAciertos() {
    int aciertos = 0;
    for (int fila = 0; fila < FILAS; fila++) {
      for (int columna = 0; columna < COLUMNAS; columna++) {
        if (casillas[fila][columna].estaMarcada()) {
          aciertos++;
        }
      }
    }
    return aciertos;
  }

  public List<Casilla> obtenerCasillas() {
    List<Casilla> resultado = new ArrayList<>(TOTAL_CARTAS);
    for (int fila = 0; fila < FILAS; fila++) {
      for (int columna = 0; columna < COLUMNAS; columna++) {
        resultado.add(casillas[fila][columna]);
      }
    }
    return List.copyOf(resultado);
  }

  public List<Casilla> obtenerCasillasMarcadas() {
    List<Casilla> resultado = new ArrayList<>(TOTAL_CARTAS);
    for (int fila = 0; fila < FILAS; fila++) {
      for (int columna = 0; columna < COLUMNAS; columna++) {
        Casilla casilla = casillas[fila][columna];
        if (casilla.estaMarcada()) {
          resultado.add(casilla);
        }
      }
    }
    return List.copyOf(resultado);
  }

  public List<Carta> obtenerCartas() {
    List<Carta> resultado = new ArrayList<>(TOTAL_CARTAS);
    for (int fila = 0; fila < FILAS; fila++) {
      for (int columna = 0; columna < COLUMNAS; columna++) {
        resultado.add(casillas[fila][columna].obtenerCarta());
      }
    }
    return List.copyOf(resultado);
  }

  public List<Carta> obtenerCartasMarcadas() {
    List<Carta> resultado = new ArrayList<>(TOTAL_CARTAS);
    for (int fila = 0; fila < FILAS; fila++) {
      for (int columna = 0; columna < COLUMNAS; columna++) {
        Casilla casilla = casillas[fila][columna];
        if (casilla.estaMarcada()) {
          resultado.add(casilla.obtenerCarta());
        }
      }
    }
    return List.copyOf(resultado);
  }

  public boolean cumple(TipoVictoria tipoVictoria) {
    Objects.requireNonNull(tipoVictoria, "tipoVictoria");
    return switch (tipoVictoria) {
      case CHORRO ->
        tieneLinea();
      case CUATRO_ESQUINAS ->
        tieneCuatroEsquinas();
      case CENTRO ->
        tieneCentro();
      case TABLA_LLENA ->
        tieneTableroLleno();
    };
  }

  public boolean tieneLinea() {
    for (int fila = 0; fila < FILAS; fila++) {
      boolean completa = true;
      for (int columna = 0; columna < COLUMNAS; columna++) {
        if (!casillas[fila][columna].estaMarcada()) {
          completa = false;
          break;
        }
      }
      if (completa) {
        return true;
      }
    }
    for (int columna = 0; columna < COLUMNAS; columna++) {
      boolean completa = true;
      for (int fila = 0; fila < FILAS; fila++) {
        if (!casillas[fila][columna].estaMarcada()) {
          completa = false;
          break;
        }
      }
      if (completa) {
        return true;
      }
    }
    boolean diagonal = true;
    boolean diagonalInversa = true;
    for (int i = 0; i < FILAS; i++) {
      if (!casillas[i][i].estaMarcada()) {
        diagonal = false;
      }
      if (!casillas[i][COLUMNAS - 1 - i].estaMarcada()) {
        diagonalInversa = false;
      }
    }
    return diagonal || diagonalInversa;
  }

  public boolean tieneCentro() {
    int fila = FILAS / 2 - 1;
    int columna = COLUMNAS / 2 - 1;
    return casillas[fila][columna].estaMarcada()
      && casillas[fila][columna + 1].estaMarcada()
      && casillas[fila + 1][columna].estaMarcada()
      && casillas[fila + 1][columna + 1].estaMarcada();
  }

  public boolean tieneCuatroEsquinas() {
    return casillas[0][0].estaMarcada()
      && casillas[0][COLUMNAS - 1].estaMarcada()
      && casillas[FILAS - 1][0].estaMarcada()
      && casillas[FILAS - 1][COLUMNAS - 1].estaMarcada();
  }

  public boolean tieneTableroLleno() {
    return obtenerAciertos() == TOTAL_CARTAS;
  }

  private Casilla buscarCasilla(Carta carta) {
    if (carta == null) {
      return null;
    }
    for (int fila = 0; fila < FILAS; fila++) {
      for (int columna = 0; columna < COLUMNAS; columna++) {
        if (casillas[fila][columna].contiene(carta)) {
          return casillas[fila][columna];
        }
      }
    }
    return null;
  }

  private void validarPosicion(int fila, int columna) {
    if (fila < 0 || fila >= FILAS || columna < 0 || columna >= COLUMNAS) {
      throw new IllegalArgumentException("La posición (" + fila + ", " + columna + ") no existe en el tablero");
    }
  }
}
