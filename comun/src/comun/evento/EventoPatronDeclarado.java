package comun.evento;

import java.io.Serializable;

public record EventoPatronDeclarado(String jugador, String patron, int puntos)
  implements Serializable {
}
