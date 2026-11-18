package juego.fachada;

import comun.ComandoCerrar;
import comun.ComandoIniciar;
import comun.ComandoMarcar;
import comun.ComandoPatron;
import comun.ComandoVictoria;
import comun.EventoBroker;
import comunicacion.Dispatcher;
import comunicacion.DispatcherFactory;
import juego.modelo.Carta;
import juego.modelo.Patron;
import serializador.ISerializador;
import serializador.Serializador;

public class FachadaBroker implements Fachada {

  private static final String PATRON_FILTRO = "comun.*;java.lang.*;java.util.*;!*";

  private final String jugadorLocal;
  private final String hostBroker;
  private final int puertoBroker;
  private final String temaComandos;
  private final ISerializador serializador;
  private final Dispatcher dispatcher;

  public FachadaBroker(
    String jugadorLocal,
    String hostBroker,
    int puertoBroker,
    String temaComandos
  ) {
    this(
      jugadorLocal,
      hostBroker,
      puertoBroker,
      temaComandos,
      new Serializador(PATRON_FILTRO),
      DispatcherFactory.crear()
    );
  }

  public FachadaBroker(
    String jugadorLocal,
    String hostBroker,
    int puertoBroker,
    String temaComandos,
    ISerializador serializador,
    Dispatcher dispatcher
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
    if (temaComandos == null || temaComandos.isBlank()) {
      throw new IllegalArgumentException("El tema de comandos no puede estar vacío");
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
    this.temaComandos = temaComandos.trim();
    this.serializador = serializador;
    this.dispatcher = dispatcher;
  }

  @Override
  public void iniciar() {
    if (dispatcher.estaActivo()) {
      return;
    }
    dispatcher.iniciar();
    enviar(new ComandoIniciar(jugadorLocal));
  }

  @Override
  public void cerrar() {
    if (dispatcher.estaActivo()) {
      enviar(new ComandoCerrar(jugadorLocal));
    }
    desconectar();
  }

  @Override
  public void desconectar() {
    dispatcher.detener();
  }

  @Override
  public void seleccionarCasilla(Carta carta) {
    if (carta != null) {
      enviar(new ComandoMarcar(jugadorLocal, carta.obtenerId()));
    }
  }

  @Override
  public void seleccionarPatron(Patron patron) {
    if (patron != null) {
      enviar(new ComandoPatron(jugadorLocal, patron.name()));
    }
  }

  @Override
  public void seleccionarVictoria(Patron patron) {
    if (patron != null) {
      enviar(new ComandoVictoria(jugadorLocal, patron.name()));
    }
  }

  private void enviar(Object comando) {
    if (!dispatcher.estaActivo()) {
      return;
    }
    byte[] datos = serializador.serializar(new EventoBroker(temaComandos, comando));
    dispatcher.enviar(hostBroker, puertoBroker, datos);
  }
}
