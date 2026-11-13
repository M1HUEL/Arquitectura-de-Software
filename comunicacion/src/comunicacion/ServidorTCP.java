package comunicacion;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServidorTCP {

  private final List<ObservadorReceptor> observadores = new CopyOnWriteArrayList<>();
  private final Set<Socket> clientes = ConcurrentHashMap.newKeySet();
  private volatile ServerSocket server;
  private Thread hilo;
  private volatile boolean activo;

  public ServidorTCP(ObservadorReceptor... observadores) {
    if (observadores != null) {
      for (ObservadorReceptor obs : observadores) {
        if (obs != null) {
          this.observadores.add(obs);
        }
      }
    }
  }

  public void agregarObservador(ObservadorReceptor observador) {
    this.observadores.add(observador);
  }

  public void eliminarObservador(ObservadorReceptor observador) {
    this.observadores.remove(observador);
  }

  public void notificarObservadores(byte[] datos) {
    for (ObservadorReceptor observador : this.observadores) {
      observador.alRecibir(datos);
    }
  }

  public synchronized void iniciar(int puerto) {
    if (this.activo) {
      return;
    }

    try {
      this.server = new ServerSocket(puerto);
      this.activo = true;
      this.hilo = new Thread(this::aceptar, "servidor-tcp");
      this.hilo.setDaemon(true);
      this.hilo.start();
    } catch (IOException e) {
      // ...
    }
  }

  public synchronized void detener() {
    this.activo = false;

    if (this.server != null) {
      try {
        this.server.close();
      } catch (IOException e) {
        // ...
      }
    }
    this.server = null;
    for (Socket cliente : this.clientes) {
      try {
        cliente.close();
      } catch (IOException e) {
        // ...
      }
    }
    this.clientes.clear();
  }

  private void aceptar() {
    while (this.activo) {
      ServerSocket servidor = this.server;
      if (servidor == null) {
        return;
      }

      try {
        Socket socket = servidor.accept();
        this.clientes.add(socket);
        Thread lector = new Thread(() -> {
          try (DataInputStream entrada = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
            int longitud = entrada.readInt();
            if (longitud < 0 || longitud > 10 * 1024 * 1024) {
              throw new IOException("Tamaño de paquete no permitido: " + longitud);
            }
            byte[] datos = new byte[longitud];
            entrada.readFully(datos);
            notificarObservadores(datos);
          } catch (Exception e) {
            if (this.activo && !(e instanceof EOFException)) {
              System.err.println("Error al leer del cliente " + socket.getPort() + ": " + e.getMessage());
            }
          } finally {
            this.clientes.remove(socket);
            try {
              socket.close();
            } catch (IOException e) {
              // ...
            }
          }
        }, "lector-tcp-" + socket.getPort());
        lector.setDaemon(true);
        lector.start();
      } catch (IOException e) {
        if (this.activo) {
          System.err.println("Error en el servidor TCP: " + e.getMessage());
        }
        this.activo = false;
      }
    }
  }

  public boolean estaActivo() {
    return this.activo;
  }

  public int obtenerPuerto() {
    ServerSocket socket = this.server;
    return socket == null ? -1 : socket.getLocalPort();
  }
}
