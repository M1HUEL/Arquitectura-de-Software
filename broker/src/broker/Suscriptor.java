package broker;

import comun.evento.Tema;
import java.util.Objects;

public class Suscriptor {

  private final Tema tema;
  private final String host;
  private final int puerto;

  public Suscriptor(Tema tema, String host, int puerto) {
    this.tema = Objects.requireNonNull(tema, "El tema no puede ser nulo");
    if (host == null || host.isBlank()) {
      throw new IllegalArgumentException("El host no puede estar vacío");
    }
    if (puerto <= 0 || puerto > 65535) {
      throw new IllegalArgumentException("El puerto no es válido: " + puerto);
    }
    this.host = host;
    this.puerto = puerto;
  }

  public Tema obtenerTema() {
    return tema;
  }

  public String obtenerHost() {
    return host;
  }

  public int obtenerPuerto() {
    return puerto;
  }

  @Override
  public boolean equals(Object objeto) {
    if (this == objeto) {
      return true;
    }
    if (!(objeto instanceof Suscriptor otro)) {
      return false;
    }
    return puerto == otro.puerto
      && tema == otro.tema
      && host.equals(otro.host);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tema, host, puerto);
  }
}
