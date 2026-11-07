package broker;

import java.io.IOException;
import red.ServidorTCP;

public class Broker {

  private final RegistroTemas registro = new RegistroTemas();
  private final ManejadorSesion manejador = new ManejadorSesion(this.registro);
  private final ServidorTCP servidor = new ServidorTCP();
  private volatile boolean activo;

  public Broker() {
    this.servidor.agregarObservadorConexion(this.manejador);
  }

  public synchronized void iniciar(int puerto) throws IOException {
    if (this.activo) {
      return;
    }
    this.servidor.iniciar(puerto);
    this.activo = true;
  }

  public synchronized void detener() {
    if (!this.activo && !this.servidor.estaActivo()) {
      return;
    }
    this.activo = false;
    this.servidor.detener();
    this.manejador.limpiar();
  }

  public boolean estaActivo() {
    return this.activo && this.servidor.estaActivo();
  }

  public int obtenerPuerto() {
    return this.servidor.obtenerPuerto();
  }

  public int cantidadSesiones() {
    return this.registro.cantidadSesiones();
  }

  public int cantidadSuscriptores(String tema) {
    return this.registro.cantidadSuscriptores(tema);
  }
}
