package juego.modelo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Jugador {

  private final String nombre;
  private final String imagen;
  private int puntaje;
  private final List<Carta> cartas;
  private final Set<Carta> casillasSeleccionadas = new LinkedHashSet<>();

  public Jugador(String nombre, String imagen, List<Carta> cartas) {
    this.nombre = nombre;
    this.imagen = imagen;
    this.puntaje = 0;
    this.cartas = new ArrayList<>(cartas);
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

  public boolean estaSeleccionada(Carta carta) {
    return casillasSeleccionadas.contains(carta);
  }

  public List<Carta> obtenerCasillasSeleccionadas() {
    return new ArrayList<>(casillasSeleccionadas);
  }

  public void agregarPuntos(int puntos) {
    puntaje += puntos;
  }

  public void restarPuntos(int puntos) {
    puntaje = Math.max(0, puntaje - puntos);
  }
}
