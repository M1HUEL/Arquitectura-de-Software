package dominio.modelo;

import java.util.List;
import java.util.NoSuchElementException;

public final class CatalogoCartas {

  private static final List<Carta> CARTAS = List.of(
    new Carta(1, "El Gallo"),
    new Carta(2, "El Diablito"),
    new Carta(3, "La Dama"),
    new Carta(4, "El Catrín"),
    new Carta(5, "El Paraguas"),
    new Carta(6, "La Sirena"),
    new Carta(7, "La Escalera"),
    new Carta(8, "La Botella"),
    new Carta(9, "El Barril"),
    new Carta(10, "El Árbol"),
    new Carta(11, "El Melón"),
    new Carta(12, "El Valiente"),
    new Carta(13, "El Gorrito"),
    new Carta(14, "La Muerte"),
    new Carta(15, "La Pera"),
    new Carta(16, "La Bandera"),
    new Carta(17, "El Bandolón"),
    new Carta(18, "El Violoncello"),
    new Carta(19, "La Garza"),
    new Carta(20, "El Pájaro"),
    new Carta(21, "La Mano"),
    new Carta(22, "La Bota"),
    new Carta(23, "La Luna"),
    new Carta(24, "El Cotorro"),
    new Carta(25, "El Borracho"),
    new Carta(26, "El Negrito"),
    new Carta(27, "El Corazón"),
    new Carta(28, "La Sandía"),
    new Carta(29, "El Tambor"),
    new Carta(30, "El Camarón"),
    new Carta(31, "Las Jaras"),
    new Carta(32, "El Músico"),
    new Carta(33, "La Araña"),
    new Carta(34, "El Soldado"),
    new Carta(35, "La Estrella"),
    new Carta(36, "El Cazo"),
    new Carta(37, "El Mundo"),
    new Carta(38, "El Apache"),
    new Carta(39, "El Nopal"),
    new Carta(40, "El Alacrán"),
    new Carta(41, "La Rosa"),
    new Carta(42, "La Calavera"),
    new Carta(43, "La Campana"),
    new Carta(44, "El Cantarito"),
    new Carta(45, "El Venado"),
    new Carta(46, "El Sol"),
    new Carta(47, "La Corona"),
    new Carta(48, "La Chalupa"),
    new Carta(49, "El Pino"),
    new Carta(50, "El Pescado"),
    new Carta(51, "La Palma"),
    new Carta(52, "La Maceta"),
    new Carta(53, "El Arpa"),
    new Carta(54, "La Rana")
  );

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
