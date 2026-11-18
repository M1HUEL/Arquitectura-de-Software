package comun.evento;

import java.io.Serializable;

public record EventoError(String mensaje) implements Serializable {
}
