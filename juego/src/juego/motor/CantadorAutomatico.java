package juego.motor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import juego.modelo.EstadoPartida;
import juego.modelo.ModeloJuego;

public class CantadorAutomatico {

  private final ModeloJuego modelo;
  private final long intervaloMs;
  private final ScheduledExecutorService ejecutor;

  public CantadorAutomatico(ModeloJuego modelo, long intervaloMs) {
    this.modelo = modelo;
    this.intervaloMs = intervaloMs;
    this.ejecutor = Executors.newSingleThreadScheduledExecutor(tarea -> {
      Thread hilo = new Thread(tarea, "cantador-automatico");
      hilo.setDaemon(true);
      return hilo;
    });
  }

  public void iniciar() {
    ejecutor.scheduleAtFixedRate(
      this::cantarSiguienteCarta, intervaloMs, intervaloMs, TimeUnit.MILLISECONDS);
  }

  public void detener() {
    ejecutor.shutdown();
  }

  private void cantarSiguienteCarta() {
    modelo.cantarSiguienteCarta();
    if (modelo.obtenerEstadoPartida() == EstadoPartida.TERMINADA
      || modelo.obtenerNumeroCartasRestantes() == 0) {
      detener();
    }
  }
}
