package comun.evento;

public record EventoGritarLoteria(String jugador, String patron) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.GRITAR_LOTERIA;
  }
}
