package comun;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class MensajesJuego {

  public static final String CASILLA = "casilla";
  public static final String PATRON = "patron";
  public static final String VICTORIA = "victoria";

  public static final String PARTIDA_INICIADA = "partida.iniciada";
  public static final String CARTA_CANTADA = "carta.cantada";
  public static final String PARTIDA_TERMINADA = "partida.terminada";

  private static final int MAX_JUGADORES = 16;
  private static final int MAX_CARTAS = 64;
  private static final int MAX_CANTADAS = 128;

  private MensajesJuego() {
    // ...
  }

  public static byte[] movimiento(Movimiento movimiento) {
    return serializar(salida -> {
      salida.writeUTF(movimiento.jugador());
      salida.writeUTF(movimiento.tipo());
      salida.writeUTF(movimiento.carta());
      salida.writeUTF(movimiento.patron());
    });
  }

  public static Movimiento leerMovimiento(byte[] datos) {
    return leer(datos, entrada -> new Movimiento(
      entrada.readUTF(), entrada.readUTF(), entrada.readUTF(), entrada.readUTF()));
  }

  public static byte[] movimientoProcesado(MovimientoProcesado movimiento) {
    return serializar(salida -> {
      salida.writeUTF(movimiento.jugador());
      salida.writeUTF(movimiento.tipo());
      salida.writeUTF(movimiento.carta());
      salida.writeUTF(movimiento.patron());
      salida.writeBoolean(movimiento.valido());
      salida.writeUTF(movimiento.detalle());
    });
  }

  public static MovimientoProcesado leerMovimientoProcesado(byte[] datos) {
    return leer(datos, entrada -> new MovimientoProcesado(
      entrada.readUTF(), entrada.readUTF(), entrada.readUTF(), entrada.readUTF(),
      entrada.readBoolean(), entrada.readUTF()));
  }

  public static byte[] evento(EventoPartida evento) {
    return serializar(salida -> {
      salida.writeUTF(evento.tipo());
      salida.writeUTF(evento.carta());
      salida.writeUTF(evento.detalle());
    });
  }

  public static EventoPartida leerEvento(byte[] datos) {
    return leer(datos, entrada -> new EventoPartida(
      entrada.readUTF(), entrada.readUTF(), entrada.readUTF()));
  }

  public static byte[] estado(EstadoPartidaJuego estado) {
    return serializar(salida -> {
      salida.writeUTF(estado.estado());
      salida.writeInt(estado.cartasRestantes());
      salida.writeInt(estado.numeroCartasCantadas());
      salida.writeUTF(estado.ganador());
      salida.writeUTF(estado.patron());
      salida.writeInt(estado.jugadores().size());
      for (JugadorEstado jugador : estado.jugadores()) {
        salida.writeUTF(jugador.nombre());
        salida.writeUTF(jugador.imagen());
        salida.writeInt(jugador.puntaje());
        salida.writeInt(jugador.cartas().size());
        for (String carta : jugador.cartas()) {
          salida.writeUTF(carta);
        }
        salida.writeInt(jugador.seleccionadas().size());
        for (String carta : jugador.seleccionadas()) {
          salida.writeUTF(carta);
        }
      }
      salida.writeInt(estado.cartasCantadas().size());
      for (String carta : estado.cartasCantadas()) {
        salida.writeUTF(carta);
      }
    });
  }

  public static EstadoPartidaJuego leerEstado(byte[] datos) {
    return leer(datos, entrada -> {
      String estado = entrada.readUTF();
      int restantes = entrada.readInt();
      int numeroCantadas = entrada.readInt();
      String ganador = entrada.readUTF();
      String patron = entrada.readUTF();
      int totalJugadores = leerTamano(entrada, MAX_JUGADORES);
      List<JugadorEstado> jugadores = new ArrayList<>();
      for (int i = 0; i < totalJugadores; i++) {
        String nombre = entrada.readUTF();
        String imagen = entrada.readUTF();
        int puntaje = entrada.readInt();
        int totalCartas = leerTamano(entrada, MAX_CARTAS);
        List<String> cartas = new ArrayList<>();
        for (int j = 0; j < totalCartas; j++) {
          cartas.add(entrada.readUTF());
        }
        int totalSeleccionadas = leerTamano(entrada, MAX_CARTAS);
        List<String> seleccionadas = new ArrayList<>();
        for (int j = 0; j < totalSeleccionadas; j++) {
          seleccionadas.add(entrada.readUTF());
        }
        jugadores.add(new JugadorEstado(nombre, imagen, puntaje, cartas, seleccionadas));
      }
      int totalCantadas = leerTamano(entrada, MAX_CANTADAS);
      List<String> cartasCantadas = new ArrayList<>();
      for (int i = 0; i < totalCantadas; i++) {
        cartasCantadas.add(entrada.readUTF());
      }
      return new EstadoPartidaJuego(estado, restantes, numeroCantadas, ganador, patron,
        jugadores, cartasCantadas);
    });
  }

  private static byte[] serializar(Escritor escritor) {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      try (DataOutputStream salida = new DataOutputStream(buffer)) {
        escritor.escribir(salida);
      }
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo serializar el mensaje de juego", e);
    }
  }

  private static <T> T leer(byte[] datos, Lector<T> lector) {
    if (datos == null) {
      throw new IllegalArgumentException("Los datos no pueden ser nulos");
    }
    try (DataInputStream entrada = new DataInputStream(new ByteArrayInputStream(datos))) {
      T valor = lector.leer(entrada);
      if (entrada.available() != 0) {
        throw new IllegalArgumentException("El mensaje contiene datos sobrantes");
      }
      return valor;
    } catch (IOException e) {
      throw new IllegalArgumentException("Mensaje de juego inválido", e);
    }
  }

  private static int leerTamano(DataInputStream entrada, int maximo) throws IOException {
    int tamano = entrada.readInt();
    if (tamano < 0 || tamano > maximo) {
      throw new IllegalArgumentException("Tamaño inválido: " + tamano);
    }
    return tamano;
  }

  private static String textoRequerido(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
      throw new IllegalArgumentException("El campo " + campo + " no puede estar vacío");
    }
    return valor;
  }

  private static String textoOpcional(String valor) {
    return valor == null ? "" : valor;
  }

  @FunctionalInterface
  private interface Escritor {

    void escribir(DataOutputStream salida) throws IOException;
  }

  @FunctionalInterface
  private interface Lector<T> {

    T leer(DataInputStream entrada) throws IOException;
  }

  public record Movimiento(String jugador, String tipo, String carta, String patron) {

    public Movimiento {
      jugador = textoRequerido(jugador, "jugador");
      tipo = textoRequerido(tipo, "tipo");
      carta = textoOpcional(carta);
      patron = textoOpcional(patron);
    }
  }

  public record MovimientoProcesado(String jugador, String tipo, String carta, String patron,
      boolean valido, String detalle) {

    public MovimientoProcesado {
      jugador = textoRequerido(jugador, "jugador");
      tipo = textoRequerido(tipo, "tipo");
      carta = textoOpcional(carta);
      patron = textoOpcional(patron);
      detalle = textoOpcional(detalle);
    }
  }

  public record EventoPartida(String tipo, String carta, String detalle) {

    public EventoPartida {
      tipo = textoRequerido(tipo, "tipo");
      carta = textoOpcional(carta);
      detalle = textoOpcional(detalle);
    }
  }

  public record JugadorEstado(String nombre, String imagen, int puntaje, List<String> cartas,
      List<String> seleccionadas) {

    public JugadorEstado {
      nombre = textoRequerido(nombre, "nombre");
      imagen = textoOpcional(imagen);
      if (puntaje < 0) {
        throw new IllegalArgumentException("El puntaje no puede ser negativo");
      }
      if (cartas == null) {
        throw new IllegalArgumentException("Las cartas no pueden ser nulas");
      }
      cartas = List.copyOf(cartas);
      if (seleccionadas == null) {
        throw new IllegalArgumentException("Las casillas seleccionadas no pueden ser nulas");
      }
      seleccionadas = List.copyOf(seleccionadas);
      for (String carta : seleccionadas) {
        if (!cartas.contains(carta)) {
          throw new IllegalArgumentException("La casilla seleccionada no pertenece al tablero");
        }
      }
    }
  }

  public record EstadoPartidaJuego(String estado, int cartasRestantes,
      int numeroCartasCantadas, String ganador, String patron, List<JugadorEstado> jugadores,
      List<String> cartasCantadas) {

    public EstadoPartidaJuego {
      estado = textoRequerido(estado, "estado");
      if (cartasRestantes < 0 || numeroCartasCantadas < 0) {
        throw new IllegalArgumentException("Los contadores no pueden ser negativos");
      }
      ganador = textoOpcional(ganador);
      patron = textoOpcional(patron);
      if (jugadores == null) {
        throw new IllegalArgumentException("Los jugadores no pueden ser nulos");
      }
      jugadores = List.copyOf(jugadores);
      if (cartasCantadas == null) {
        throw new IllegalArgumentException("Las cartas cantadas no pueden ser nulas");
      }
      cartasCantadas = List.copyOf(cartasCantadas);
      if (numeroCartasCantadas != cartasCantadas.size()) {
        throw new IllegalArgumentException("El número de cartas cantadas no coincide");
      }
    }
  }
}
