package comun.evento;

import java.io.Serializable;

public record EventoCartaCantada(int cartaId, int restantes) implements Serializable {
}
