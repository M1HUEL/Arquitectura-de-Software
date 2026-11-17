package comunicacion;

import comunicacion.api.IDispatcher;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

public class Dispatcher implements IDispatcher {

  private record Sobre(String host, int puerto, byte[] datos) {

  }

  private final BlockingQueue<Sobre> cola = new LinkedBlockingDeque<>();
  private final List<ObservadorDispatcher> observadores = new CopyOnWriteArrayList<>();
  private Thread hilo;
  private volatile boolean activo;

  public Dispatcher(ObservadorDispatcher... observadores) {
    this.observadores.addAll(List.of(observadores));
  }

  public void agregarObservador(ObservadorDispatcher observador) {
    this.observadores.add(observador);
  }

  public void eliminarObservador(ObservadorDispatcher observador) {
    this.observadores.remove(observador);
  }

  private void notificarObservadores(Sobre sobre) {
    for (ObservadorDispatcher observador : this.observadores) {
      try {
        observador.alEnviar(sobre.host(), sobre.puerto(), sobre.datos());
      } catch (IOException e) {
        System.err.println("No se pudo enviar a " + sobre.host() + ":" + sobre.puerto()
          + ": " + e.getMessage());
      }
    }
  }

  @Override
  public synchronized void enviar(String host, int puerto, byte[] datos) {
    if (!this.activo) {
      throw new IllegalStateException("El dispatcher no está activo. Llama a iniciar() primero.");
    }
    try {
      this.cola.put(datos != null ? new Sobre(host, puerto, datos) : new Sobre(host, puerto, new byte[0]));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public synchronized void iniciar() {
    if (this.activo) {
      return;
    }
    this.activo = true;
    this.cola.clear();
    this.hilo = new Thread(() -> {
      while (this.activo) {
        try {
          Sobre sobre = this.cola.take();
          notificarObservadores(sobre);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }, "dispatcher");
    this.hilo.setDaemon(true);
    this.hilo.start();
  }

  public synchronized void detener() {
    if (!this.activo) {
      return;
    }
    this.activo = false;
    Sobre pendiente;
    while ((pendiente = this.cola.poll()) != null) {
      notificarObservadores(pendiente);
    }
    if (this.hilo != null) {
      this.hilo.interrupt();
      try {
        this.hilo.join(2000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      this.hilo = null;
    }
  }

  public boolean estaActivo() {
    return this.activo;
  }
}
