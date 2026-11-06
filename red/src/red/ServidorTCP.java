package red;

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

  private static final int LONGITUD_MAXIMA_MENSAJE = 2 * 1024 * 1024;
  private static final long ESPERA_REINTENTO_ACEPTAR_MS = 50;

  private final List<ObservadorReceptor> observadores = new CopyOnWriteArrayList<>();
  private final Set<Socket> clientes = ConcurrentHashMap.newKeySet();
  private volatile ServerSocket serverSocket;
  private volatile Thread hilo;
  private volatile boolean activo;

  public ServidorTCP(ObservadorReceptor... observadores) {
    this.observadores.addAll(List.of(observadores));
  }

  public void agregarObservador(ObservadorReceptor observador) {
    this.observadores.add(observador);
  }

  public void quitarObservador(ObservadorReceptor observador) {
    this.observadores.remove(observador);
  }

  public synchronized void iniciar(int puerto) throws IOException {
    if (this.activo) {
      return;
    }
    ServerSocket servidor = new ServerSocket(puerto);
    this.serverSocket = servidor;
    this.activo = true;
    this.hilo = new Thread(() -> aceptar(servidor), "servidor-tcp");
    this.hilo.setDaemon(true);
    this.hilo.start();
  }

  public synchronized void detener() {
    this.activo = false;
    cerrarServidor();
    for (Socket socket : this.clientes) {
      cerrar(socket);
    }
    this.clientes.clear();
    Thread anterior = this.hilo;
    this.hilo = null;
    if (anterior != null) {
      anterior.interrupt();
      if (anterior != Thread.currentThread()) {
        try {
          anterior.join(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  public boolean estaActivo() {
    Thread actual = this.hilo;
    return this.activo && actual != null && actual.isAlive();
  }

  public int obtenerPuerto() {
    ServerSocket servidor = this.serverSocket;
    return servidor == null ? -1 : servidor.getLocalPort();
  }

  private void aceptar(ServerSocket servidor) {
    while (esServidorActual(servidor)) {
      try {
        Socket socket = servidor.accept();
        if (!esServidorActual(servidor)) {
          cerrar(socket);
          return;
        }
        this.clientes.add(socket);
        Thread lector = new Thread(() -> leer(socket), "lector-tcp-" + socket.getPort());
        lector.setDaemon(true);
        lector.start();
      } catch (IOException e) {
        if (!esServidorActual(servidor)) {
          return;
        }
        System.err.println("Error al aceptar conexiones: " + e.getMessage());
        try {
          Thread.sleep(ESPERA_REINTENTO_ACEPTAR_MS);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }

  private void leer(Socket socket) {
    try (DataInputStream entrada = new DataInputStream(
      new BufferedInputStream(socket.getInputStream()))) {
      while (this.activo && !socket.isClosed()) {
        int longitud = entrada.readInt();
        if (longitud <= 0 || longitud > LONGITUD_MAXIMA_MENSAJE) {
          throw new IOException("Longitud de mensaje inválida: " + longitud);
        }
        byte[] datos = new byte[longitud];
        entrada.readFully(datos);
        notificar(datos);
      }
    } catch (IOException e) {
      if (this.activo && !socket.isClosed() && !(e instanceof EOFException)) {
        System.err.println("Error al leer del cliente " + socket.getPort() + ": " + e.getMessage());
      }
    } finally {
      this.clientes.remove(socket);
      cerrar(socket);
    }
  }

  private void notificar(byte[] datos) {
    for (ObservadorReceptor observador : this.observadores) {
      try {
        observador.alRecibir(datos);
      } catch (RuntimeException e) {
        System.err.println("Error al notificar un mensaje: " + e.getMessage());
      }
    }
  }

  private boolean esServidorActual(ServerSocket servidor) {
    return this.activo && this.serverSocket == servidor;
  }

  private void cerrarServidor() {
    if (this.serverSocket != null) {
      try {
        this.serverSocket.close();
      } catch (IOException ignored) {
      }
      this.serverSocket = null;
    }
  }

  private void cerrar(Socket socket) {
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }
}
