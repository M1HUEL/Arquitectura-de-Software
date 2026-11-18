package juego.fachada;

import comun.evento.EventoCerrarPartida;
import comun.evento.EventoIniciarPartida;
import comun.evento.EventoMarcarCasilla;
import comun.evento.EventoSeleccionarPatron;
import comun.evento.EventoGritarLoteria;
import comun.EventoBroker;
import comunicacion.api.IDispatcher;
import juego.modelo.Carta;
import juego.modelo.Patron;
import serializador.ISerializador;

public class FachadaBroker implements Fachada {

  private final String jugadorLocal;
  private final String hostBroker;
  private final int puertoBroker;
  private final String temaEventosCliente;
  private final ISerializador serializador;
  private final IDispatcher dispatcher;

  public FachadaBroker(
    String jugadorLocal,
    String hostBroker,
    int puertoBroker,
    String temaEventosCliente,
    ISerializador serializador,
    IDispatcher dispatcher
  ) {
    if (jugadorLocal == null || jugadorLocal.isBlank()) {
      throw new IllegalArgumentException("El jugador local no puede estar vacío");
    }
    if (hostBroker == null || hostBroker.isBlank()) {
      throw new IllegalArgumentException("El host del broker no puede estar vacío");
    }
    if (puertoBroker < 0 || puertoBroker > 65535) {
      throw new IllegalArgumentException("El puerto del broker no es válido: " + puertoBroker);
    }
    if (temaEventosCliente == null || temaEventosCliente.isBlank()) {
      throw new IllegalArgumentException("El tema de eventos del cliente no puede estar vacío");
    }
    if (serializador == null) {
      throw new IllegalArgumentException("El serializador no puede ser nulo");
    }
    if (dispatcher == null) {
      throw new IllegalArgumentException("El dispatcher no puede ser nulo");
    }
    this.jugadorLocal = jugadorLocal.trim();
    this.hostBroker = hostBroker.trim();
    this.puertoBroker = puertoBroker;
    this.temaEventosCliente = temaEventosCliente.trim();
    this.serializador = serializador;
    this.dispatcher = dispatcher;
  }

  @Override
  public void iniciar() {
    enviar(new EventoIniciarPartida(jugadorLocal));
  }

  @Override
  public void cerrar() {
    enviar(new EventoCerrarPartida(jugadorLocal));
  }

  @Override
  public void seleccionarCasilla(Carta carta) {
    if (carta != null) {
      enviar(new EventoMarcarCasilla(jugadorLocal, carta.obtenerId()));
    }
  }

  @Override
  public void seleccionarPatron(Patron patron) {
    if (patron != null) {
      enviar(new EventoSeleccionarPatron(jugadorLocal, patron.name()));
    }
  }

  @Override
  public void seleccionarVictoria(Patron patron) {
    if (patron != null) {
      enviar(new EventoGritarLoteria(jugadorLocal, patron.name()));
    }
  }

  private void enviar(Object evento) {
    byte[] datos = serializador.serializar(new EventoBroker(temaEventosCliente, evento));
    dispatcher.enviar(hostBroker, puertoBroker, datos);
  }
}
