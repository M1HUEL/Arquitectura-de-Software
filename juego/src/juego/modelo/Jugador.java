package juego.modelo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Jugador {

  private static final int FILAS = 4;
  private static final int COLUMNAS = 4;

  private final String nombre;
  private final String imagen;
  private final List<Carta> cartas;
  private final Set<Carta> casillasSeleccionadas = new LinkedHashSet<>();
  private int puntaje;

  public Jugador(String nombre, String imagen, List<Carta> cartas) {
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
    }
    if (cartas == null) {
      throw new IllegalArgumentException("Las cartas del jugador no pueden ser nulas");
    }
    this.nombre = nombre.trim();
    this.imagen = imagen == null ? "" : imagen;
    this.cartas = List.copyOf(cartas);
  }

  public String obtenerNombre() {
    return nombre;
  }

  public String obtenerImagen() {
    return imagen;
  }

  public int obtenerPuntaje() {
    return puntaje;
  }

  public List<Carta> obtenerCartas() {
    return cartas;
  }

  public boolean marcarCasilla(Carta carta) {
    if (carta == null || !cartas.contains(carta)) {
      return false;
    }
    return casillasSeleccionadas.add(carta);
  }

  public boolean desmarcarCasilla(Carta carta) {
    return carta != null && casillasSeleccionadas.remove(carta);
  }

  public boolean estaSeleccionada(Carta carta) {
    return carta != null && casillasSeleccionadas.contains(carta);
  }

  public List<Carta> obtenerCasillasSeleccionadas() {
    return List.copyOf(casillasSeleccionadas);
  }

  public boolean cumple(Patron patron) {
    if (patron == null || cartas.size() != FILAS * COLUMNAS) {
      return false;
    }
    return switch (patron) {
      case CHORRO ->
        tieneLinea() || tieneColumna() || tieneDiagonal();
      case CUATRO_ESQUINAS ->
        tieneCuatroEsquinas();
      case CENTRO ->
        tieneCentro();
      case TABLA_LLENA ->
        tieneTablaLlena();
    };
  }

  private boolean tieneLinea() {
    for (int fila = 0; fila < FILAS; fila++) {
      boolean completa = true;
      for (int columna = 0; columna < COLUMNAS; columna++) {
        if (!estaSeleccionada(cartas.get(indice(fila, columna)))) {
          completa = false;
          break;
        }
      }
      if (completa) {
        return true;
      }
    }
    return false;
  }

  private boolean tieneColumna() {
    for (int columna = 0; columna < COLUMNAS; columna++) {
      boolean completa = true;
      for (int fila = 0; fila < FILAS; fila++) {
        if (!estaSeleccionada(cartas.get(indice(fila, columna)))) {
          completa = false;
          break;
        }
      }
      if (completa) {
        return true;
      }
    }
    return false;
  }

  private boolean tieneDiagonal() {
    boolean diagonal = true;
    boolean diagonalInversa = true;
    for (int i = 0; i < FILAS; i++) {
      if (!estaSeleccionada(cartas.get(indice(i, i)))) {
        diagonal = false;
      }
      if (!estaSeleccionada(cartas.get(indice(i, COLUMNAS - 1 - i)))) {
        diagonalInversa = false;
      }
    }
    return diagonal || diagonalInversa;
  }

  private boolean tieneCuatroEsquinas() {
    return estaSeleccionada(cartas.get(indice(0, 0)))
      && estaSeleccionada(cartas.get(indice(0, COLUMNAS - 1)))
      && estaSeleccionada(cartas.get(indice(FILAS - 1, 0)))
      && estaSeleccionada(cartas.get(indice(FILAS - 1, COLUMNAS - 1)));
  }

  private boolean tieneCentro() {
    int fila = FILAS / 2 - 1;
    int columna = COLUMNAS / 2 - 1;
    return estaSeleccionada(cartas.get(indice(fila, columna)))
      && estaSeleccionada(cartas.get(indice(fila, columna + 1)))
      && estaSeleccionada(cartas.get(indice(fila + 1, columna)))
      && estaSeleccionada(cartas.get(indice(fila + 1, columna + 1)));
  }

  private boolean tieneTablaLlena() {
    for (Carta carta : cartas) {
      if (!estaSeleccionada(carta)) {
        return false;
      }
    }
    return true;
  }

  private int indice(int fila, int columna) {
    return fila * COLUMNAS + columna;
  }

  public void agregarPuntos(int puntos) {
    puntaje = Math.max(0, puntaje + puntos);
  }

  public void restarPuntos(int puntos) {
    puntaje = Math.max(0, puntaje - puntos);
  }
}
