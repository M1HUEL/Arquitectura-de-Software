package comun.evento;

public record EventoIniciarPartida(String jugador) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.INICIAR_PARTIDA;
  }
}
