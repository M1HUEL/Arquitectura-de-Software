package juego.modelo;

import java.util.ArrayList;
import java.util.List;

public class Jugador {

  private final String nombre;
  private final String imagen;
  private int puntaje;
  private final List<Carta> cartas;

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

  public void agregarPuntos(int puntos) {
    puntaje += puntos;
  }

  public void restarPuntos(int puntos) {
    puntaje = Math.max(0, puntaje - puntos);
  }
}
