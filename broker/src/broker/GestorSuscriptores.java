package broker;

import comun.evento.Tema;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GestorSuscriptores {

  private final Map<Tema, CopyOnWriteArrayList<Suscriptor>> suscriptores = new ConcurrentHashMap<>();

  public void agregar(Tema tema, String host, int puerto) {
    Objects.requireNonNull(tema, "El tema no puede ser nulo");
    CopyOnWriteArrayList<Suscriptor> lista = suscriptores.computeIfAbsent(
      tema, clave -> new CopyOnWriteArrayList<>());
    lista.addIfAbsent(new Suscriptor(tema, host, puerto));
  }

  public void eliminar(Tema tema, String host, int puerto) {
    Objects.requireNonNull(tema, "El tema no puede ser nulo");
    suscriptores.computeIfPresent(tema, (clave, lista) -> {
      lista.removeIf(suscriptor -> suscriptor.obtenerHost().equals(host)
        && suscriptor.obtenerPuerto() == puerto);
      return lista.isEmpty() ? null : lista;
    });
  }

  public List<Suscriptor> obtener(Tema tema) {
    Objects.requireNonNull(tema, "El tema no puede ser nulo");
    List<Suscriptor> lista = suscriptores.get(tema);
    return lista == null ? List.of() : List.copyOf(lista);
  }
}
