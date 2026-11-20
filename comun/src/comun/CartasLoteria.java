package comun;

import java.util.List;

public final class CartasLoteria {

  public static final List<String> NOMBRES = List.of(
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
  );

  private CartasLoteria() {
  }

  public static String nombreDe(int id) {
    if (id < 1 || id > NOMBRES.size()) {
      throw new IllegalArgumentException("No existe una carta con id " + id);
    }
    return NOMBRES.get(id - 1);
  }
}
