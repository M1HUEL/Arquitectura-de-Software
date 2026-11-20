package comun.evento;

import java.io.Serializable;

public record EventoCasillaMarcada(String jugador, int cartaId, int puntos)
  implements Serializable {
}
