package dominio.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public final class Baraja {

  private final List<Carta> cartas;
  private final List<Carta> cartasCantadas;
  private final Set<Carta> cantadasRegistradas;

  public Baraja(List<Carta> cartas) {
    Objects.requireNonNull(cartas, "cartas");
    for (Carta carta : cartas) {
      Objects.requireNonNull(carta, "Las cartas no pueden contener valores nulos");
    }
    Set<Carta> unicas = new HashSet<>(cartas);
    if (unicas.size() != cartas.size()) {
      throw new IllegalArgumentException("La baraja no puede contener cartas repetidas");
    }
    this.cartas = new ArrayList<>(cartas);
    this.cartasCantadas = new ArrayList<>();
    this.cantadasRegistradas = new HashSet<>();
  }

  public void revolver(Random aleatorio) {
    Objects.requireNonNull(aleatorio, "aleatorio");
    Collections.shuffle(cartas, aleatorio);
  }

  public Carta sacarSiguiente() {
    if (estaVacia()) {
      throw new NoSuchElementException("No quedan cartas en la baraja");
    }
    Carta carta = cartas.remove(cartas.size() - 1);
    cartasCantadas.add(carta);
    cantadasRegistradas.add(carta);
    return carta;
  }

  public boolean estaVacia() {
    return cartas.isEmpty();
  }

  public int obtenerRestantes() {
    return cartas.size();
  }

  public List<Carta> obtenerCartasRestantes() {
    return List.copyOf(cartas);
  }

  public List<Carta> obtenerCantadas() {
    return List.copyOf(cartasCantadas);
  }

  public boolean fueCantada(Carta carta) {
    return cantadasRegistradas.contains(carta);
  }

  public Optional<Carta> obtenerUltimaCantada() {
    if (cartasCantadas.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(cartasCantadas.get(cartasCantadas.size() - 1));
  }

  public boolean contiene(Carta carta) {
    return cartas.contains(carta);
  }
}
