package comun;

import java.io.Serializable;

public record ComandoPatron(String jugador, String patron) implements Serializable {
}
