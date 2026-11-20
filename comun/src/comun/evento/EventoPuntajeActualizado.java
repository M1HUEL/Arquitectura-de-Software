package comun.evento;

public record EventoPuntajeActualizado(String jugador, int puntos) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.PUNTAJE_ACTUALIZADO;
  }
}
