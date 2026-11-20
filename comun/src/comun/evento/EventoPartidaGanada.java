package comun.evento;

import java.io.Serializable;

public record EventoPartidaGanada(String jugador, int puntos) implements Serializable {
}
