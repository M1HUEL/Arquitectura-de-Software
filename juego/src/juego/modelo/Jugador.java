package juego.modelo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Jugador {

  private final String nombre;
  private final List<Carta> cartas;
  private final Set<Carta> casillasSeleccionadas = new LinkedHashSet<>();
  private int puntaje;

  public Jugador(String nombre, List<Carta> cartas) {
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
    }
    if (cartas == null) {
      throw new IllegalArgumentException("Las cartas del jugador no pueden ser nulas");
    }
    this.nombre = nombre.trim();
    this.cartas = List.copyOf(cartas);
  }

  public String obtenerNombre() {
    return nombre;
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

  public boolean estaSeleccionada(Carta carta) {
    return carta != null && casillasSeleccionadas.contains(carta);
  }

  public void agregarPuntos(int puntos) {
    puntaje = Math.max(0, puntaje + puntos);
  }
}
