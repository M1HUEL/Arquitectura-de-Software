package comun.evento;

import comun.EstadoJuego;
import java.io.Serializable;

public record EventoPartidaGanada(EstadoJuego estado) implements Serializable {
}
