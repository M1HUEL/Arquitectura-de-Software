package red;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import red.api.IReceptorExterno;

public class Receptor implements ObservadorReceptor {

  private final BlockingQueue<byte[]> cola = new LinkedBlockingQueue<>();
  private final List<IReceptorExterno> receptores = new CopyOnWriteArrayList<>();
  private final ServidorTCP servidor;
  private Thread hilo;
  private volatile boolean activo;

  public Receptor(IReceptorExterno... receptores) {
    if (receptores == null) {
      throw new IllegalArgumentException("Los receptores no pueden ser nulos");
    }
    for (IReceptorExterno receptor : receptores) {
      if (receptor == null) {
        throw new IllegalArgumentException("El receptor no puede ser nulo");
      }
      this.receptores.add(receptor);
    }
    this.servidor = new ServidorTCP(this);
  }

  @Override
  public void alRecibir(byte[] datos) {
    if (!this.activo || datos == null) {
      return;
    }
    try {
      this.cola.put(datos);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public synchronized void iniciar(int puerto) throws IOException {
    if (this.activo) {
      return;
    }
    this.activo = true;
    try {
      this.servidor.iniciar(puerto);
    } catch (IOException e) {
      this.activo = false;
      throw e;
    }
    this.hilo = new Thread(this::procesar, "receptor");
    this.hilo.setDaemon(true);
    this.hilo.start();
  }

  public synchronized void detener() {
    if (!this.activo && !this.servidor.estaActivo()) {
      return;
    }
    this.activo = false;
    this.servidor.detener();
    this.cola.clear();
    if (this.hilo != null) {
      this.hilo.interrupt();
      this.hilo = null;
    }
  }

  public boolean estaActivo() {
    return this.activo && this.servidor.estaActivo();
  }

  public int obtenerPuerto() {
    return this.servidor.obtenerPuerto();
  }

  private void procesar() {
    while (this.activo) {
      try {
        byte[] datos = this.cola.take();
        if (!this.activo) {
          return;
        }
        for (IReceptorExterno receptor : this.receptores) {
          try {
            receptor.alRecibir(datos);
          } catch (RuntimeException e) {
            System.err.println("Error al procesar un mensaje: " + e.getMessage());
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }
}
