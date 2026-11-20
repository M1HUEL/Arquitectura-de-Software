package comun.evento;

public record EventoPatronDeclarado(String jugador, String patron) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.PATRON_DECLARADO;
  }
}
