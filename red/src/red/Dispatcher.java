package red;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import red.api.IDispatcher;

public class Dispatcher implements IDispatcher {

  private final BlockingQueue<Sobre> cola = new LinkedBlockingQueue<>();
  private final List<ObservadorDispatcher> observadores = new CopyOnWriteArrayList<>();
  private final Thread hilo = new Thread(this::procesar, "dispatcher");
  private volatile boolean activo = true;

  public Dispatcher(ObservadorDispatcher... observadores) {
    if (observadores == null) {
      throw new IllegalArgumentException("Los observadores no pueden ser nulos");
    }
    for (ObservadorDispatcher observador : observadores) {
      this.observadores.add(Objects.requireNonNull(
        observador, "El observador no puede ser nulo"));
    }
    this.hilo.setDaemon(true);
    this.hilo.start();
  }

  public void agregarObservador(ObservadorDispatcher observador) {
    this.observadores.add(Objects.requireNonNull(
      observador, "El observador no puede ser nulo"));
  }

  public void quitarObservador(ObservadorDispatcher observador) {
    this.observadores.remove(observador);
  }

  @Override
  public void enviar(String host, int puerto, byte[] datos) {
    if (!this.activo) {
      throw new IllegalStateException("El dispatcher no está activo");
    }
    Objects.requireNonNull(host, "El host no puede ser nulo");
    if (host.isBlank()) {
      throw new IllegalArgumentException("El host no puede estar vacío");
    }
    if (puerto <= 0 || puerto > 65535) {
      throw new IllegalArgumentException("Puerto inválido: " + puerto);
    }
    Objects.requireNonNull(datos, "Los datos no pueden ser nulos");
    this.cola.add(new Sobre(host, puerto, datos.clone()));
  }

  @Override
  public void detener() {
    this.activo = false;
    this.hilo.interrupt();
    for (ObservadorDispatcher observador : this.observadores) {
      try {
        observador.cerrar();
      } catch (RuntimeException e) {
        System.err.println("Error al cerrar un observador: " + e.getMessage());
      }
    }
  }

  @Override
  public boolean estaActivo() {
    return this.activo && this.hilo.isAlive();
  }

  private void procesar() {
    try {
      while (this.activo) {
        Sobre sobre;
        try {
          sobre = this.cola.take();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
        if (!this.activo) {
          return;
        }
        notificar(sobre);
      }
    } finally {
      this.activo = false;
    }
  }

  private void notificar(Sobre sobre) {
    for (ObservadorDispatcher observador : this.observadores) {
      try {
        observador.alEnviar(sobre.host(), sobre.puerto(), sobre.datos());
      } catch (IOException | RuntimeException e) {
        System.err.println("No se pudo enviar el mensaje a "
          + sobre.host() + ":" + sobre.puerto() + ": " + e.getMessage());
      }
    }
  }

  private record Sobre(String host, int puerto, byte[] datos) {

  }
}
