package comun.evento;

public record EventoPartidaFinalizada() implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.PARTIDA_FINALIZADA;
  }
}
