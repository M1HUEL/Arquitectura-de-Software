package comun.evento;

public record EventoGritarLoteria(String jugador) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.GRITAR_LOTERIA;
  }
}
