package comun;

import java.io.Serializable;

public record ComandoJuego(
  String accion,
  String jugador,
  int cartaId,
  String patron
) implements Serializable {

  public static final String INICIAR = "INICIAR";
  public static final String CERRAR = "CERRAR";
  public static final String MARCAR = "MARCAR";
  public static final String PATRON = "PATRON";
  public static final String VICTORIA = "VICTORIA";
}
