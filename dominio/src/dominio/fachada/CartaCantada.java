package dominio.fachada;

public record CartaCantada(int cartaId, int restantes) {

  public CartaCantada {
    if (cartaId <= 0) {
      throw new IllegalArgumentException("El id de la carta debe ser positivo");
    }
    if (restantes < 0) {
      throw new IllegalArgumentException("Las cartas restantes no pueden ser negativas");
    }
  }
}
