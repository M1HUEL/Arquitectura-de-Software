package dominio.griton;

import dominio.modelo.Baraja;
import dominio.modelo.Carta;
import dominio.modelo.CatalogoCartas;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.SequencedSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Griton {

  private final Baraja baraja;
  private final long intervaloMs;
  private final Runnable accionGritar;
  private final SequencedSet<Carta> gritadas = new LinkedHashSet<>();
  private ScheduledExecutorService planificador;

  public Griton(Baraja baraja, long intervaloMs, Runnable accionGritar) {
    this.baraja = Objects.requireNonNull(baraja, "baraja");
    if (intervaloMs <= 0) {
      throw new IllegalArgumentException("El intervalo debe ser positivo");
    }
    this.intervaloMs = intervaloMs;
    this.accionGritar = Objects.requireNonNull(accionGritar, "accionGritar");
  }

  public static Griton completo(Random aleatorio, long intervaloMs, Runnable accionGritar) {
    Objects.requireNonNull(aleatorio, "aleatorio");
    Baraja baraja = new Baraja(CatalogoCartas.obtenerTodas());
    baraja.revolver(aleatorio);
    return new Griton(baraja, intervaloMs, accionGritar);
  }

  public void iniciar() {
    detener();
    planificador = Executors.newSingleThreadScheduledExecutor(tarea -> {
      Thread hilo = new Thread(tarea, "griton-cartas");
      hilo.setDaemon(true);
      return hilo;
    });
    planificador.scheduleWithFixedDelay(() -> {
      try {
        accionGritar.run();
      } catch (RuntimeException e) {
        System.err.println("Error al gritar: " + e.getMessage());
      }
    }, intervaloMs, intervaloMs, TimeUnit.MILLISECONDS);
  }

  public void detener() {
    if (planificador != null) {
      planificador.shutdownNow();
      planificador = null;
    }
  }

  public Carta gritarSiguiente() {
    Carta carta = baraja.sacarSiguiente();
    gritadas.add(carta);
    return carta;
  }

  public boolean fueGritada(Carta carta) {
    return gritadas.contains(carta);
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
    return Optional.of(gritadas.getLast());
  }
}
