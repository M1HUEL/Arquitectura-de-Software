package dominio.modelo;

import comun.CartasLoteria;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public final class CatalogoCartas {

  private static final List<Carta> CARTAS = crearCartas();

  private static List<Carta> crearCartas() {
    List<String> nombres = CartasLoteria.NOMBRES;
    List<Carta> cartas = new ArrayList<>(nombres.size());
    for (int i = 0; i < nombres.size(); i++) {
      cartas.add(new Carta(i + 1, nombres.get(i)));
    }
    return List.copyOf(cartas);
  }

  private CatalogoCartas() {
  }

  public static List<Carta> obtenerTodas() {
    return CARTAS;
  }

  public static Carta obtenerPorId(int id) {
    return CARTAS.stream()
      .filter(carta -> carta.obtenerId() == id)
      .findFirst()
      .orElseThrow(() -> new NoSuchElementException("No existe una carta con id " + id));
  }

  public static Carta obtenerPorNombre(String nombre) {
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre de la carta no puede estar vacío");
    }
    String buscado = nombre.trim();
    return CARTAS.stream()
      .filter(carta -> carta.obtenerNombre().equalsIgnoreCase(buscado))
      .findFirst()
      .orElseThrow(() -> new NoSuchElementException("No existe una carta llamada " + buscado));
  }

  public static boolean contiene(Carta carta) {
    return carta != null && CARTAS.contains(carta);
  }

  public static int obtenerTotal() {
    return CARTAS.size();
  }
}
