package broker;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class RegistroTemas {

  private final ConcurrentMap<String, Set<Sesion>> porTema = new ConcurrentHashMap<>();
  private final Set<Sesion> sesiones = ConcurrentHashMap.newKeySet();

  void agregar(Sesion sesion) {
    this.sesiones.add(sesion);
  }

  void suscribir(Sesion sesion, String tema) {
    this.sesiones.add(sesion);
    this.porTema.computeIfAbsent(tema, clave -> ConcurrentHashMap.newKeySet())
      .add(sesion);
    sesion.agregarTema(tema);
  }

  void desuscribir(Sesion sesion, String tema) {
    Set<Sesion> suscriptores = this.porTema.get(tema);
    if (suscriptores != null) {
      suscriptores.remove(sesion);
      if (suscriptores.isEmpty()) {
        this.porTema.remove(tema, suscriptores);
      }
    }
    sesion.quitarTema(tema);
  }

  void quitar(Sesion sesion) {
    for (String tema : sesion.temas()) {
      desuscribir(sesion, tema);
    }
    this.sesiones.remove(sesion);
  }

  void publicar(String tema, byte[] frame) {
    Set<Sesion> suscriptores = this.porTema.get(tema);
    if (suscriptores == null) {
      return;
    }
    for (Sesion suscriptor : suscriptores) {
      suscriptor.encolar(frame);
    }
  }

  int cantidadSesiones() {
    return this.sesiones.size();
  }

  int cantidadSuscriptores(String tema) {
    Set<Sesion> suscriptores = this.porTema.get(tema);
    return suscriptores == null ? 0 : suscriptores.size();
  }
}
