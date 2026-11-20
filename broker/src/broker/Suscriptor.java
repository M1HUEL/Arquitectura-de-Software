package broker;

import java.util.Objects;

public class Suscriptor {

  private final String tema;
  private final String host;
  private final int puerto;

  public Suscriptor(String tema, String host, int puerto) {
    if (tema == null || tema.isBlank()) {
      throw new IllegalArgumentException("El tema no puede estar vacío");
    }
    if (host == null || host.isBlank()) {
      throw new IllegalArgumentException("El host no puede estar vacío");
    }
    if (puerto <= 0 || puerto > 65535) {
      throw new IllegalArgumentException("El puerto no es válido: " + puerto);
    }
    this.tema = tema;
    this.host = host;
    this.puerto = puerto;
  }

  public String getTema() {
    return tema;
  }

  public String getHost() {
    return host;
  }

  public int getPuerto() {
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
      && tema.equals(otro.tema)
      && host.equals(otro.host);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tema, host, puerto);
  }
}
