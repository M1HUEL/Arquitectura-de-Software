package juego.red;

import broker.cliente.ClienteBroker;
import comun.MensajesJuego;
import comun.Protocolo;
import comun.Tipos;
import java.io.IOException;
import juego.modelo.ModeloJuego;
import juego.modelo.Patron;
import juego.modelo.PublicadorMovimiento;

public final class ConexionBroker implements ClienteBroker.Observador,
    PublicadorMovimiento, AutoCloseable {

  private final ClienteBroker cliente;
  private final ModeloJuego modelo;

  public ConexionBroker(String host, int puerto, ModeloJuego modelo) throws IOException {
    if (modelo == null) {
      throw new IllegalArgumentException("El modelo no puede ser nulo");
    }
    this.modelo = modelo;
    this.cliente = new ClienteBroker(host, puerto, this);
    try {
      this.cliente.suscribir(Tipos.TEMA_PARTIDA_ESTADO);
      this.cliente.suscribir(Tipos.TEMA_PARTIDA_EVENTO);
      this.cliente.suscribir(Tipos.TEMA_MOVIMIENTO_PROCESADO);
      this.cliente.publicar(Tipos.TEMA_SINCRONIZAR, new byte[0]);
    } catch (IOException e) {
      this.cliente.close();
      throw e;
    }
  }

  @Override
  public void publicar(String jugador, String tipo, String carta, Patron patron)
      throws IOException {
    this.cliente.publicar(Tipos.TEMA_MOVIMIENTO, MensajesJuego.movimiento(
      new MensajesJuego.Movimiento(jugador, tipo, carta,
        patron == null ? "" : patron.name())));
  }

  @Override
  public void alRecibirPublicacion(Protocolo.Publicacion publicacion) {
    switch (publicacion.tema()) {
      case Tipos.TEMA_PARTIDA_ESTADO -> this.modelo.aplicarEstado(
        MensajesJuego.leerEstado(publicacion.contenido()));
      case Tipos.TEMA_PARTIDA_EVENTO -> this.modelo.aplicarEvento(
        MensajesJuego.leerEvento(publicacion.contenido()));
      case Tipos.TEMA_MOVIMIENTO_PROCESADO -> this.modelo.aplicarMovimientoProcesado(
        MensajesJuego.leerMovimientoProcesado(publicacion.contenido()));
      default -> {
      }
    }
  }

  @Override
  public void alError(Protocolo.Error error) {
    System.err.println("Juego: error del broker " + error.codigo() + ": " + error.detalle());
  }

  @Override
  public void alDesconectar(Throwable causa) {
    this.modelo.registrarDesconexion(causa);
  }

  @Override
  public void close() {
    this.cliente.close();
  }
}
