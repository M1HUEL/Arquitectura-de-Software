package juego.modelo;

import java.util.ArrayList;
import java.util.List;

public class Mazo {

  private static final String[] NOMBRES = {
    "El Gallo",
    "El Diablito",
    "La Dama",
    "El Catrín",
    "El Paraguas",
    "La Sirena",
    "La Escalera",
    "La Botella",
    "El Barril",
    "El Árbol",
    "El Melón",
    "El Valiente",
    "El Gorrito",
    "La Muerte",
    "La Pera",
    "La Bandera",
    "El Bandolón",
    "El Violoncello",
    "La Garza",
    "El Pájaro",
    "La Mano",
    "La Bota",
    "La Luna",
    "El Cotorro",
    "El Borracho",
    "El Negrito",
    "El Corazón",
    "La Sandía",
    "El Tambor",
    "El Camarón",
    "Las Jaras",
    "El Músico",
    "La Araña",
    "El Soldado",
    "La Estrella",
    "El Cazo",
    "El Mundo",
    "El Apache",
    "El Nopal",
    "El Alacrán",
    "La Rosa",
    "La Calavera",
    "La Campana",
    "El Cantarito",
    "El Venado",
    "El Sol",
    "La Corona",
    "La Chalupa",
    "El Pino",
    "El Pescado",
    "La Palma",
    "La Maceta",
    "El Arpa",
    "La Rana"
  };

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
    return NOMBRES.length;
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
