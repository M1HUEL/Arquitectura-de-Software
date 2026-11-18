package juego.modelo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Jugador {

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

  public void agregarPuntos(int puntos) {
    puntaje = Math.max(0, puntaje + puntos);
  }

  public void restarPuntos(int puntos) {
    puntaje = Math.max(0, puntaje - puntos);
  }
}
