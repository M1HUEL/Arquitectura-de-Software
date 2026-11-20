package comun.evento;

public record EventoCerrarPartida(String jugador) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.CERRAR_PARTIDA;
  }
}
