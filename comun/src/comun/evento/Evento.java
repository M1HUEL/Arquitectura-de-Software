package comun.evento;

import java.io.Serializable;
import java.util.Objects;

public record Evento(Tema tema, Serializable payload) implements Serializable {

  public Evento {
    Objects.requireNonNull(tema, "El tema no puede ser nulo");
    Objects.requireNonNull(payload, "El payload no puede ser nulo");
  }
}
