package juego.modelo;

import java.io.IOException;

public interface PublicadorMovimiento {

  void publicar(String jugador, String tipo, String carta, Patron patron) throws IOException;
}
