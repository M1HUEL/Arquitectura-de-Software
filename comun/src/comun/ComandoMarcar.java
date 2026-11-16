package comun;

import java.io.Serializable;

public record ComandoMarcar(String jugador, int cartaId) implements Serializable {
}
