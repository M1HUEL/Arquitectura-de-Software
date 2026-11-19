package dominio.modelo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public final class Griton {

  private final Baraja baraja;
  private final Random aleatorio;
  private final List<Carta> gritadas = new ArrayList<>();
  private final Set<Carta> registradas = new HashSet<>();

  public Griton(Baraja baraja, Random aleatorio) {
    this.baraja = Objects.requireNonNull(baraja, "baraja");
    this.aleatorio = Objects.requireNonNull(aleatorio, "aleatorio");
  }

  public static Griton completo(Random aleatorio) {
    Objects.requireNonNull(aleatorio, "aleatorio");
    Griton griton = new Griton(new Baraja(CatalogoCartas.obtenerTodas()), aleatorio);
    griton.barajear();
    return griton;
  }

  public void barajear() {
    baraja.revolver(aleatorio);
  }

  public Carta gritarSiguiente() {
    Carta carta = baraja.sacarSiguiente();
    gritadas.add(carta);
    registradas.add(carta);
    return carta;
  }

  public boolean fueGritada(Carta carta) {
    return registradas.contains(carta);
  }

  public boolean puedeGritar() {
    return !baraja.estaVacia();
  }

  public int obtenerTurno() {
    return gritadas.size();
  }

  public int obtenerRestantes() {
    return baraja.obtenerRestantes();
  }

  public List<Carta> obtenerCartasGritadas() {
    return List.copyOf(gritadas);
  }

  public List<Carta> obtenerCartasRestantes() {
    return baraja.obtenerCartasRestantes();
  }

  public Optional<Carta> obtenerUltimaCarta() {
    if (gritadas.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(gritadas.get(gritadas.size() - 1));
  }
}
