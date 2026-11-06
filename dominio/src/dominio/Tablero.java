package dominio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Tablero {

  public static final int COLUMNAS = 4;
  public static final int NUMERO_CASILLAS = COLUMNAS * COLUMNAS;

  private final List<Carta> casillas;

  public Tablero(List<Carta> casillas) {
    if (casillas.size() != NUMERO_CASILLAS) {
      throw new IllegalArgumentException(
        "El tablero debe tener " + NUMERO_CASILLAS + " casillas.");
    }
    this.casillas = new ArrayList<>(casillas);
  }

  public List<Carta> obtenerCasillas() {
    return new ArrayList<>(casillas);
  }

  public boolean contiene(Carta carta) {
    return casillas.contains(carta);
  }

  public boolean cumplePatron(Patron patron, Collection<Carta> cartasCantadas) {
    boolean[] marcadas = new boolean[NUMERO_CASILLAS];
    for (int i = 0; i < NUMERO_CASILLAS; i++) {
      marcadas[i] = cartasCantadas.contains(casillas.get(i));
    }

    switch (patron) {
      case LINEA:
        return algunaLineaMarcada(marcadas);
      case COLUMNA:
        return algunaColumnaMarcada(marcadas);
      case DIAGONAL:
        return diagonalMarcada(marcadas) || diagonalInversaMarcada(marcadas);
      case CUATRO_ESQUINAS:
        return marcadas[0]
          && marcadas[COLUMNAS - 1]
          && marcadas[NUMERO_CASILLAS - COLUMNAS]
          && marcadas[NUMERO_CASILLAS - 1];
      case TABLA_LLENA:
        return todasMarcadas(marcadas);
      default:
        return false;
    }
  }

  private boolean todasMarcadas(boolean[] marcadas) {
    for (boolean marcada : marcadas) {
      if (!marcada) {
        return false;
      }
    }
    return true;
  }

  private boolean algunaLineaMarcada(boolean[] marcadas) {
    for (int fila = 0; fila < COLUMNAS; fila++) {
      boolean completa = true;
      for (int columna = 0; columna < COLUMNAS; columna++) {
        if (!marcadas[fila * COLUMNAS + columna]) {
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

  private boolean algunaColumnaMarcada(boolean[] marcadas) {
    for (int columna = 0; columna < COLUMNAS; columna++) {
      boolean completa = true;
      for (int fila = 0; fila < COLUMNAS; fila++) {
        if (!marcadas[fila * COLUMNAS + columna]) {
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

  private boolean diagonalMarcada(boolean[] marcadas) {
    for (int i = 0; i < COLUMNAS; i++) {
      if (!marcadas[i * COLUMNAS + i]) {
        return false;
      }
    }
    return true;
  }

  private boolean diagonalInversaMarcada(boolean[] marcadas) {
    for (int i = 0; i < COLUMNAS; i++) {
      if (!marcadas[i * COLUMNAS + (COLUMNAS - 1 - i)]) {
        return false;
      }
    }
    return true;
  }
}
