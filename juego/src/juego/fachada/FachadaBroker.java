package juego.fachada;

import comun.evento.EventoCerrarPartida;
import comun.evento.EventoIniciarPartida;
import comun.evento.EventoMarcarCasilla;
import comun.evento.EventoSeleccionarPatron;
import comun.evento.EventoGritarLoteria;
import comun.evento.Evento;
import comun.evento.Tema;
import comunicacion.api.IDispatcher;
import juego.modelo.Patron;
import comun.serializador.ISerializador;
import java.io.Serializable;

public class FachadaBroker implements Fachada {

  private final String jugadorLocal;
  private final String hostBroker;
  private final int puertoBroker;
  private final ISerializador serializador;
  private final IDispatcher dispatcher;

  public FachadaBroker(
    String jugadorLocal,
    String hostBroker,
    int puertoBroker,
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
    if (serializador == null) {
      throw new IllegalArgumentException("El serializador no puede ser nulo");
    }
    if (dispatcher == null) {
      throw new IllegalArgumentException("El dispatcher no puede ser nulo");
    }
    this.jugadorLocal = jugadorLocal.trim();
    this.hostBroker = hostBroker.trim();
    this.puertoBroker = puertoBroker;
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
  public void seleccionarCasilla(int cartaId) {
    enviar(new EventoMarcarCasilla(jugadorLocal, cartaId));
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

  private void enviar(Serializable evento) {
    byte[] datos = serializador.serializar(new Evento(temaDe(evento), evento));
    dispatcher.enviar(hostBroker, puertoBroker, datos);
  }

  private Tema temaDe(Serializable evento) {
    if (evento instanceof EventoIniciarPartida) {
      return Tema.INICIAR_PARTIDA;
    }
    if (evento instanceof EventoMarcarCasilla) {
      return Tema.MARCAR_CASILLA;
    }
    if (evento instanceof EventoSeleccionarPatron) {
      return Tema.SELECCIONAR_PATRON;
    }
    if (evento instanceof EventoGritarLoteria) {
      return Tema.GRITAR_LOTERIA;
    }
    if (evento instanceof EventoCerrarPartida) {
      return Tema.CERRAR_PARTIDA;
    }
    throw new IllegalArgumentException("Evento sin tema: " + evento.getClass().getSimpleName());
  }
}
