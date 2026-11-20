package comun.evento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record EventoPartidaIniciada(
  List<String> nombresJugadores,
  List<List<Integer>> cartasPorJugador,
  int restantes
) implements Serializable {

  public EventoPartidaIniciada {
    nombresJugadores = nombresJugadores == null
      ? new ArrayList<>() : new ArrayList<>(nombresJugadores);
    List<List<Integer>> originales = cartasPorJugador == null ? List.of() : cartasPorJugador;
    List<List<Integer>> copias = new ArrayList<>();
    for (List<Integer> cartas : originales) {
      copias.add(cartas == null ? new ArrayList<>() : new ArrayList<>(cartas));
    }
    cartasPorJugador = copias;
  }
}
