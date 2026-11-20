package dominio.griton;

import comun.evento.Evento;
import comun.evento.EventoCartaCantada;
import comun.evento.Tema;
import comun.serializador.ISerializador;
import comunicacion.api.IDispatcher;
import dominio.fachada.Fachada;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Griton {

  private final Fachada fachada;
  private final ISerializador serializador;
  private final IDispatcher dispatcher;
  private final String hostBroker;
  private final int puertoBroker;
  private final long intervaloMs;
  private ScheduledExecutorService planificador;

  public Griton(
    Fachada fachada,
    ISerializador serializador,
    IDispatcher dispatcher,
    String hostBroker,
    int puertoBroker,
    long intervaloMs
  ) {
    this.fachada = Objects.requireNonNull(fachada, "fachada");
    this.serializador = Objects.requireNonNull(serializador, "serializador");
    this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
    if (hostBroker == null || hostBroker.isBlank()) {
      throw new IllegalArgumentException("El host del broker no puede estar vacío");
    }
    if (puertoBroker < 0 || puertoBroker > 65535) {
      throw new IllegalArgumentException("El puerto del broker no es válido: " + puertoBroker);
    }
    if (intervaloMs <= 0) {
      throw new IllegalArgumentException("El intervalo debe ser positivo");
    }
    this.hostBroker = hostBroker.trim();
    this.puertoBroker = puertoBroker;
    this.intervaloMs = intervaloMs;
  }

  public void iniciar() {
    detener();
    planificador = Executors.newSingleThreadScheduledExecutor(tarea -> {
      Thread hilo = new Thread(tarea, "griton-cartas");
      hilo.setDaemon(true);
      return hilo;
    });
    planificador.scheduleWithFixedDelay(this::cantar, 0, intervaloMs, TimeUnit.MILLISECONDS);
  }

  public void detener() {
    if (planificador != null) {
      planificador.shutdownNow();
      planificador = null;
    }
  }

  private void cantar() {
    try {
      OptionalInt carta = fachada.cantarSiguienteCarta();
      if (carta.isEmpty()) {
        return;
      }
      EventoCartaCantada contenido = new EventoCartaCantada(
        carta.getAsInt(), fachada.obtenerRestantes());
      byte[] datos = serializador.serializar(
        new Evento(Tema.CARTA_CANTADA, contenido));
      dispatcher.enviar(hostBroker, puertoBroker, datos);
    } catch (RuntimeException e) {
      System.err.println("Error al gritar: " + e.getMessage());
    }
  }
}
