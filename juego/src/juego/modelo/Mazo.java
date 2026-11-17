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
    for (String nombre : NOMBRES) {
      cartas.add(new Carta(nombre, crearNombreImagen(nombre)));
    }
    return cartas;
  }

  public static Carta crearCarta(String nombre) {
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre de la carta no puede estar vacío");
    }
    String buscado = nombre.trim();
    for (String actual : NOMBRES) {
      if (actual.equalsIgnoreCase(buscado)) {
        return new Carta(actual, crearNombreImagen(actual));
      }
    }
    return new Carta(buscado, "");
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
