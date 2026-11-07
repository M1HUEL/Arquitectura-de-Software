package comun;

public final class Tipos {

  public static final String MOVIMIENTO = "movimiento";
  public static final String MOVIMIENTO_PROCESADO = "movimiento.procesado";

  public static final String SUSCRIBIR = "$suscribir";
  public static final String DESUSCRIBIR = "$desuscribir";
  public static final String PUBLICAR = "$publicar";
  public static final String PUBLICACION = "$publicacion";
  public static final String SUSCRITO = "$suscrito";
  public static final String ERROR = "$error";

  public static final String TEMA_MOVIMIENTO = "juego.movimiento";
  public static final String TEMA_MOVIMIENTO_PROCESADO = "dominio.movimiento.procesado";
  public static final String TEMA_SINCRONIZAR = "juego.sincronizar";
  public static final String TEMA_PARTIDA_ESTADO = "partida.estado";
  public static final String TEMA_PARTIDA_EVENTO = "partida.evento";

  private Tipos() {
    // ...
  }
}
