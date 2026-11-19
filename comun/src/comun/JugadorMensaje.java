package comun;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record JugadorMensaje(
  String id,
  String nombre,
  List<CartaMensaje> cartas,
  List<Integer> marcadas,
  int puntos
) implements Serializable {

  public JugadorMensaje {
    cartas = cartas == null ? new ArrayList<>() : new ArrayList<>(cartas);
    marcadas = marcadas == null ? new ArrayList<>() : new ArrayList<>(marcadas);
  }
}
