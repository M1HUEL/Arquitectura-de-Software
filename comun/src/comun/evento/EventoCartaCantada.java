package comun.evento;

import comun.EstadoJuego;
import java.io.Serializable;

public record EventoCartaCantada(EstadoJuego estado) implements Serializable {
}
