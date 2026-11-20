package broker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GestorSuscriptores {

  private final Map<String, CopyOnWriteArrayList<Suscriptor>> suscriptores = new ConcurrentHashMap<>();

  public void agregar(String tema, String host, int puerto) {
    validarTema(tema);
    CopyOnWriteArrayList<Suscriptor> lista = suscriptores.computeIfAbsent(
      tema, clave -> new CopyOnWriteArrayList<>());
    lista.addIfAbsent(new Suscriptor(tema, host, puerto));
  }

  public void eliminar(String tema, String host, int puerto) {
    validarTema(tema);
    suscriptores.computeIfPresent(tema, (clave, lista) -> {
      lista.removeIf(suscriptor -> suscriptor.getHost().equals(host)
        && suscriptor.getPuerto() == puerto);
      return lista.isEmpty() ? null : lista;
    });
  }

  public List<Suscriptor> obtener(String tema) {
    validarTema(tema);
    List<Suscriptor> lista = suscriptores.get(tema);
    return lista == null ? List.of() : List.copyOf(lista);
  }

  private static void validarTema(String tema) {
    if (tema == null || tema.isBlank()) {
      throw new IllegalArgumentException("El tema no puede estar vacío");
    }
  }
}
