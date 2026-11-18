package comun.evento;

import comun.EstadoJuego;
import java.io.Serializable;

public record EventoPartidaFinalizada(EstadoJuego estado) implements Serializable {
}
