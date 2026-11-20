package broker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GestorSuscriptores {

  public record Suscriptor(String tema, String host, int puerto) {

    public Suscriptor {
      validarTema(tema);
      if (host == null || host.isBlank()) {
        throw new IllegalArgumentException("El host no puede estar vacío");
      }
      if (puerto <= 0 || puerto > 65535) {
        throw new IllegalArgumentException("El puerto no es válido: " + puerto);
      }
    }
  }

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
      lista.remove(new Suscriptor(tema, host, puerto));
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
