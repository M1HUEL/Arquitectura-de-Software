package comun.evento;

import java.io.Serializable;

public record EventoGritarLoteria(String jugador, String patron) implements Serializable {
}
