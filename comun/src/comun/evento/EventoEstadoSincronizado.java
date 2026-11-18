package comun.evento;

import comun.EstadoJuego;
import java.io.Serializable;

public record EventoEstadoSincronizado(EstadoJuego estado) implements Serializable {
}
