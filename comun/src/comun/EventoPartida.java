package comun;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record EventoPartida(
  String tipo,
  String idPartida,
  String estado,
  String patron,
  List<JugadorMensaje> jugadores,
  List<CartaMensaje> cartasCantadas,
  int restantes,
  String ganador,
  String mensaje
) implements Serializable {

  public EventoPartida {
    jugadores = jugadores == null ? new ArrayList<>() : new ArrayList<>(jugadores);
    cartasCantadas = cartasCantadas == null
      ? new ArrayList<>() : new ArrayList<>(cartasCantadas);
  }

  public static final String SINCRONIZACION = "SINCRONIZACION";
  public static final String CARTA_CANTADA = "CARTA_CANTADA";
  public static final String VICTORIA = "VICTORIA";
  public static final String FIN = "FIN";
  public static final String ERROR = "ERROR";
}
