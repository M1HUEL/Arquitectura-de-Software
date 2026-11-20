package comun.evento;

public record EventoSeleccionarPatron(String jugador, String patron) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.SELECCIONAR_PATRON;
  }
}
