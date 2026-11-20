package comun;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record EstadoJuego(
  String idPartida,
  String estado,
  String patron,
  List<JugadorMensaje> jugadores,
  List<Integer> cartasCantadas,
  int restantes,
  String ganador
) implements Serializable {

  public EstadoJuego {
    jugadores = jugadores == null ? new ArrayList<>() : new ArrayList<>(jugadores);
    cartasCantadas = cartasCantadas == null
      ? new ArrayList<>() : new ArrayList<>(cartasCantadas);
  }
}
