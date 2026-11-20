package comun.evento;

public record EventoCerrarPartida() implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.CERRAR_PARTIDA;
  }
}
