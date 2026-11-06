package dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mazo {

  private static final String[] NOMBRES = {
    "El gallo",
    "El diablito",
    "La dama",
    "El catrín",
    "El paraguas",
    "La sirena",
    "La escalera",
    "La botella",
    "El barril",
    "El árbol",
    "El melón",
    "El valiente",
    "El gorrito",
    "La muerte",
    "La pera",
    "La bandera",
    "El bandolón",
    "El violoncello",
    "La garza",
    "El pájaro",
    "La mano",
    "La bota",
    "La luna",
    "El cotorro",
    "El borracho",
    "El negrito",
    "El corazón",
    "La sandía",
    "El tambor",
    "El camarón",
    "Las jaras",
    "El músico",
    "La araña",
    "El soldado",
    "La estrella",
    "El cazo",
    "El mundo",
    "El apache",
    "El nopal",
    "El alacrán",
    "La rosa",
    "La calavera",
    "La campana",
    "El cantarito",
    "El venado",
    "El sol",
    "La corona",
    "La chalupa",
    "El pino",
    "El pescado",
    "La palma",
    "La maceta",
    "El arpa",
    "La jarra"
  };

  private final List<Carta> cartas;

  public Mazo() {
    this.cartas = new ArrayList<>();
    for (String nombre : NOMBRES) {
      cartas.add(new Carta(nombre, crearNombreImagen(nombre)));
    }
  }

  public void barajar() {
    Collections.shuffle(cartas);
  }

  public Carta tomarSiguiente() {
    if (cartas.isEmpty()) {
      return null;
    }
    return cartas.remove(0);
  }

  public boolean contiene(Carta carta) {
    return cartas.contains(carta);
  }

  public List<Carta> obtenerCartas() {
    return new ArrayList<>(cartas);
  }

  public int obtenerNumeroCartas() {
    return cartas.size();
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
