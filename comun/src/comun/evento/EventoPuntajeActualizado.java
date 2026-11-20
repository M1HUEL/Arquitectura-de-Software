package comun.evento;

import java.io.Serializable;

public record EventoPuntajeActualizado(String jugador, int puntos) implements Serializable {
}
