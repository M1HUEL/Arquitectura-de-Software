package comun.evento;

public record EventoCasillaMarcada(String jugador, int cartaId, int puntos)
  implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.CASILLA_MARCADA;
  }
}
