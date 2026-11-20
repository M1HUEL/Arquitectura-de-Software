package comun.evento;

import java.io.Serializable;
import java.util.Objects;

public final class Evento implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Tema tema;
  private final Serializable contenido;

  public Evento(Instruccion contenido) {
    this.contenido = Objects.requireNonNull(contenido, "El contenido no puede ser nulo");
    this.tema = Objects.requireNonNull(contenido.tema(), "El tema no puede ser nulo");
  }

  public Tema tema() {
    return tema;
  }

  public Serializable payload() {
    return contenido;
  }
}
