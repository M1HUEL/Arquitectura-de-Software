package broker;

import comun.Mensaje;
import comun.Protocolo;
import comun.Tipos;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import red.api.IConexion;
import red.api.IObservadorConexion;

final class ManejadorSesion implements IObservadorConexion {

  private final RegistroTemas registro;
  private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();

  ManejadorSesion(RegistroTemas registro) {
    this.registro = registro;
  }

  @Override
  public void alConectar(IConexion conexion) {
    Sesion sesion = new Sesion(conexion);
    this.sesiones.put(conexion.id(), sesion);
    this.registro.agregar(sesion);
    System.out.println("Broker: cliente conectado " + conexion.direccion());
  }

  @Override
  public void alRecibir(IConexion conexion, byte[] datos) {
    Sesion sesion = this.sesiones.get(conexion.id());
    if (sesion == null) {
      return;
    }
    try {
      procesar(sesion, Mensaje.desdeBytes(datos));
    } catch (RuntimeException e) {
      responderErrorYCerrar(sesion, "protocolo", e.getMessage());
    }
  }

  @Override
  public void alDesconectar(IConexion conexion) {
    Sesion sesion = this.sesiones.remove(conexion.id());
    if (sesion == null) {
      return;
    }
    this.registro.quitar(sesion);
    sesion.cerrar();
    System.out.println("Broker: cliente desconectado " + conexion.direccion());
  }

  void limpiar() {
    for (Sesion sesion : this.sesiones.values()) {
      this.registro.quitar(sesion);
      sesion.cerrar();
    }
    this.sesiones.clear();
  }

  private void procesar(Sesion sesion, Mensaje mensaje) {
    switch (mensaje.tipo()) {
      case Tipos.SUSCRIBIR -> suscribir(sesion, mensaje);
      case Tipos.DESUSCRIBIR -> desuscribir(sesion, mensaje);
      case Tipos.PUBLICAR -> publicar(mensaje);
      default -> responderErrorYCerrar(sesion, "tipo",
        "Tipo de mensaje no soportado: " + mensaje.tipo());
    }
  }

  private void suscribir(Sesion sesion, Mensaje mensaje) {
    String tema = Protocolo.leerTema(mensaje);
    this.registro.suscribir(sesion, tema);
    sesion.encolar(Protocolo.suscrito(tema).aBytes());
  }

  private void desuscribir(Sesion sesion, Mensaje mensaje) {
    String tema = Protocolo.leerTema(mensaje);
    this.registro.desuscribir(sesion, tema);
  }

  private void publicar(Mensaje mensaje) {
    Protocolo.Publicacion publicacion = Protocolo.leerPublicacion(mensaje);
    byte[] frame = new Mensaje(Tipos.PUBLICACION, mensaje.contenido()).aBytes();
    this.registro.publicar(publicacion.tema(), frame);
  }

  private void responderErrorYCerrar(Sesion sesion, String codigo, String detalle) {
    try {
      sesion.conexion().enviar(Protocolo.error(codigo, detalle).aBytes());
    } catch (IOException e) {
      // La sesión ya no está disponible.
    } finally {
      sesion.cerrar();
    }
  }
}
