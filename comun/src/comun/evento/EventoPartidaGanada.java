package comun.evento;

public record EventoPartidaGanada(String jugador, int puntos) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.PARTIDA_GANADA;
  }
}
