package comun;

import java.io.Serializable;

public record EventoBroker(String tema, Object contenido) implements Serializable {

  public EventoBroker {
    if (tema == null || tema.isBlank()) {
      throw new IllegalArgumentException("El tema no puede estar vacío");
    }
    if (contenido == null) {
      throw new IllegalArgumentException("El contenido no puede ser nulo");
    }
  }
}
