package broker;

import comun.evento.Tema;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GestorSuscriptores {

  private final Map<Tema, CopyOnWriteArrayList<Suscriptor>> suscriptores = new ConcurrentHashMap<>();

  public CopyOnWriteArrayList<Suscriptor> obtener(Tema tema) {
    Objects.requireNonNull(tema, "El tema no puede ser nulo");
    return suscriptores.computeIfAbsent(tema, clave -> new CopyOnWriteArrayList<>());
  }
}
