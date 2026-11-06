package juego.fachada;

import java.util.List;
import juego.modelo.Carta;
import juego.modelo.Jugador;
import juego.modelo.Patron;

public class FachadaJuego implements Fachada {

  @Override
  public boolean validarCasilla(Carta carta, Jugador jugador) {
    return contieneCarta(jugador.obtenerCartas(), carta);
  }

  @Override
  public boolean validarCantar(Carta carta, List<Carta> cartas, List<Carta> cartasCantadas) {
    if (!contieneCarta(cartas, carta)) {
      return false;
    }
    return !contieneCarta(cartasCantadas, carta);
  }

  @Override
  public boolean validarPatron(Jugador jugador, List<Carta> cartasCantadas, Patron patron) {
    List<Carta> cartas = jugador.obtenerCartas();
    int total = cartas.size();
    if (total == 0) {
      return false;
    }

    int columnas = (int) Math.sqrt(total);
    if (columnas * columnas != total) {
      return false;
    }

    boolean[] marcadas = new boolean[total];
    for (int i = 0; i < total; i++) {
      marcadas[i] = contieneCarta(cartasCantadas, cartas.get(i));
    }

    switch (patron) {
      case LINEA:
        return algunaLineaMarcada(marcadas, columnas);
      case COLUMNA:
        return algunaColumnaMarcada(marcadas, columnas);
      case DIAGONAL:
        return diagonalMarcada(marcadas, columnas)
          || diagonalInversaMarcada(marcadas, columnas);
      case CUATRO_ESQUINAS:
        return marcadas[0]
          && marcadas[columnas - 1]
          && marcadas[total - columnas]
          && marcadas[total - 1];
      case TABLA_LLENA:
        return todasMarcadas(marcadas);
      default:
        return false;
    }
  }

  @Override
  public boolean validarVictoria(Jugador jugador, List<Carta> cartasCantadas, Patron patron) {
    return validarPatron(jugador, cartasCantadas, patron);
  }

  private boolean contieneCarta(List<Carta> cartas, Carta buscada) {
    for (Carta carta : cartas) {
      if (carta.obtenerNombre().equals(buscada.obtenerNombre())) {
        return true;
      }
    }
    return false;
  }

  private boolean todasMarcadas(boolean[] marcadas) {
    for (boolean marcada : marcadas) {
      if (!marcada) {
        return false;
      }
    }
    return true;
  }

  private boolean algunaLineaMarcada(boolean[] marcadas, int columnas) {
    for (int fila = 0; fila < columnas; fila++) {
      boolean completa = true;
      for (int columna = 0; columna < columnas; columna++) {
        if (!marcadas[fila * columnas + columna]) {
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

  private boolean algunaColumnaMarcada(boolean[] marcadas, int columnas) {
    for (int columna = 0; columna < columnas; columna++) {
      boolean completa = true;
      for (int fila = 0; fila < columnas; fila++) {
        if (!marcadas[fila * columnas + columna]) {
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

  private boolean diagonalMarcada(boolean[] marcadas, int columnas) {
    for (int i = 0; i < columnas; i++) {
      if (!marcadas[i * columnas + i]) {
        return false;
      }
    }
    return true;
  }

  private boolean diagonalInversaMarcada(boolean[] marcadas, int columnas) {
    for (int i = 0; i < columnas; i++) {
      if (!marcadas[i * columnas + (columnas - 1 - i)]) {
        return false;
      }
    }
    return true;
  }
}
