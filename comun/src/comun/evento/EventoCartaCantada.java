package comun.evento;

public record EventoCartaCantada(int cartaId, int restantes) implements Instruccion {

  @Override
  public Tema tema() {
    return Tema.CARTA_CANTADA;
  }
}
