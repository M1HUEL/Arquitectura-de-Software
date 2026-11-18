package juego.modelo;

import comun.CartasLoteria;
import java.util.ArrayList;
import java.util.List;

public class Mazo {

  private static final List<String> NOMBRES = CartasLoteria.NOMBRES;

  private Mazo() {
  }

  public static List<Carta> crearCartas() {
    List<Carta> cartas = new ArrayList<>();
    for (int i = 0; i < NOMBRES.size(); i++) {
      String nombre = NOMBRES.get(i);
      cartas.add(new Carta(i + 1, nombre, crearNombreImagen(nombre)));
    }
    return cartas;
  }

  public static Carta crearCarta(String nombre) {
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre de la carta no puede estar vacío");
    }
    String buscado = nombre.trim();
    for (int i = 0; i < NOMBRES.size(); i++) {
      String actual = NOMBRES.get(i);
      if (actual.equalsIgnoreCase(buscado)) {
        return new Carta(i + 1, actual, crearNombreImagen(actual));
      }
    }
    return new Carta(0, buscado, "");
  }

  public static Carta crearCarta(int id, String nombre) {
    return new Carta(id, nombre, crearNombreImagen(nombre));
  }

  public static int obtenerTotal() {
    return NOMBRES.size();
  }

  private static String crearNombreImagen(String nombre) {
    return nombre.toLowerCase()
      .replace("á", "a")
      .replace("é", "e")
      .replace("í", "i")
      .replace("ó", "o")
      .replace("ú", "u")
      .replace("ñ", "n")
      .replace(" ", "_")
      + ".png";
  }
}
