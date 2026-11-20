package comun.evento;

public record EventoMarcarCasilla(String jugador, int cartaId) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.MARCAR_CASILLA;
  }
}
