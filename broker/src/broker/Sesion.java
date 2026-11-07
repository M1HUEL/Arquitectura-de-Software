package broker;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import red.api.IConexion;

final class Sesion implements AutoCloseable {

  private static final int CAPACIDAD_COLA = 1024;

  private final IConexion conexion;
  private final Set<String> temas = ConcurrentHashMap.newKeySet();
  private final BlockingQueue<byte[]> salida = new LinkedBlockingQueue<>(CAPACIDAD_COLA);
  private final Thread escritor;
  private volatile boolean activa = true;

  Sesion(IConexion conexion) {
    if (conexion == null) {
      throw new IllegalArgumentException("La conexión no puede ser nula");
    }
    this.conexion = conexion;
    this.escritor = new Thread(this::procesarSalida, "escritor-broker-" + conexion.id());
    this.escritor.setDaemon(true);
    this.escritor.start();
  }

  IConexion conexion() {
    return this.conexion;
  }

  boolean estaActiva() {
    return this.activa && this.conexion.estaAbierta();
  }

  void agregarTema(String tema) {
    this.temas.add(tema);
  }

  void quitarTema(String tema) {
    this.temas.remove(tema);
  }

  Set<String> temas() {
    return Set.copyOf(this.temas);
  }

  void encolar(byte[] frame) {
    if (!estaActiva()) {
      return;
    }
    if (!this.salida.offer(frame)) {
      cerrar();
    }
  }

  @Override
  public void close() {
    cerrar();
  }

  void cerrar() {
    if (!this.activa) {
      return;
    }
    this.activa = false;
    this.salida.clear();
    this.escritor.interrupt();
    this.conexion.cerrar();
  }

  private void procesarSalida() {
    while (this.activa) {
      try {
        byte[] frame = this.salida.take();
        if (!this.activa) {
          return;
        }
        this.conexion.enviar(frame);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      } catch (IOException e) {
        cerrar();
        return;
      }
    }
  }
}
