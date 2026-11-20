package comun.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public final class Baraja<T> {

  private final List<T> cartas;

  public Baraja(List<T> cartas) {
    Objects.requireNonNull(cartas, "cartas");
    for (T carta : cartas) {
      Objects.requireNonNull(carta, "Las cartas no pueden contener valores nulos");
    }
    Set<T> unicas = new HashSet<>(cartas);
    if (unicas.size() != cartas.size()) {
      throw new IllegalArgumentException("La baraja no puede contener cartas repetidas");
    }
    this.cartas = new ArrayList<>(cartas);
  }

  public void revolver(Random aleatorio) {
    Objects.requireNonNull(aleatorio, "aleatorio");
    Collections.shuffle(cartas, aleatorio);
  }

  public T sacarSiguiente() {
    if (estaVacia()) {
      throw new NoSuchElementException("No quedan cartas en la baraja");
    }
    return cartas.remove(cartas.size() - 1);
  }

  public boolean estaVacia() {
    return cartas.isEmpty();
  }

  public int obtenerRestantes() {
    return cartas.size();
  }
}
