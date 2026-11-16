package dominio.modelo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public final class Griton {

  private final Baraja baraja;
  private final Random aleatorio;

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
    return baraja.sacarSiguiente();
  }

  public boolean fueGritada(Carta carta) {
    return baraja.fueCantada(carta);
  }

  public boolean puedeGritar() {
    return !baraja.estaVacia();
  }

  public boolean estaVacio() {
    return baraja.estaVacia();
  }

  public int obtenerRestantes() {
    return baraja.obtenerRestantes();
  }

  public List<Carta> obtenerCartasGritadas() {
    return baraja.obtenerCantadas();
  }

  public List<Carta> obtenerCartasRestantes() {
    return baraja.obtenerCartasRestantes();
  }

  public Optional<Carta> obtenerUltimaCarta() {
    return baraja.obtenerUltimaCantada();
  }

  public Baraja obtenerBaraja() {
    return baraja;
  }
}
