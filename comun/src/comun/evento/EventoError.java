package comun.evento;

public record EventoError(String mensaje) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.ERROR;
  }
}
