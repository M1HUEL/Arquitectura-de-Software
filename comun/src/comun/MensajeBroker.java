package comun;

import java.io.Serializable;

public record MensajeBroker(String tema, Object contenido) implements Serializable {

  public MensajeBroker {
    if (tema == null || tema.isBlank()) {
      throw new IllegalArgumentException("El tema no puede estar vacío");
    }
  }
}
