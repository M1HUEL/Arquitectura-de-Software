package comun.evento;

import java.io.Serializable;

public record EventoSeleccionarPatron(String jugador, String patron) implements Serializable {
}
