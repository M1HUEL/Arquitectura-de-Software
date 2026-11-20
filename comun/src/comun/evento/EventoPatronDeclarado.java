package comun.evento;

public record EventoPatronDeclarado(String jugador, String patron, int puntos)
  implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.PATRON_DECLARADO;
  }
}
