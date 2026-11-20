package dominio.modelo;

import comun.CartasLoteria;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

public final class CatalogoCartas {

  private static final List<Carta> CARTAS = IntStream.rangeClosed(1, CartasLoteria.NOMBRES.size())
    .mapToObj(id -> new Carta(id, CartasLoteria.NOMBRES.get(id - 1)))
    .toList();

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

}
