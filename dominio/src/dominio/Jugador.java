package dominio;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Jugador {

  private final String nombre;
  private final String imagen;
  private final Tablero tablero;
  private final Set<Carta> casillasSeleccionadas = new LinkedHashSet<>();
  private int puntaje;

  public Jugador(String nombre, String imagen, List<Carta> cartas) {
    this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo.");
    this.imagen = Objects.requireNonNull(imagen, "La imagen no puede ser nula.");
    this.tablero = new Tablero(cartas);
    this.puntaje = 0;
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

  public Tablero obtenerTablero() {
    return tablero;
  }

  public List<Carta> obtenerCartas() {
    return tablero.obtenerCasillas();
  }

  public boolean marcarCasilla(Carta carta) {
    if (carta == null || !tablero.contiene(carta)) {
      return false;
    }
    return casillasSeleccionadas.add(carta);
  }

  public List<Carta> obtenerCasillasSeleccionadas() {
    return new ArrayList<>(casillasSeleccionadas);
  }

  public void agregarPuntos(int puntos) {
    if (puntos > 0) {
      puntaje += puntos;
    }
  }

  public void restarPuntos(int puntos) {
    puntaje = Math.max(0, puntaje - puntos);
  }
}
