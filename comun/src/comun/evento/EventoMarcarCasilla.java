package comun.evento;

import java.io.Serializable;

public record EventoMarcarCasilla(String jugador, int cartaId) implements Serializable {
}
