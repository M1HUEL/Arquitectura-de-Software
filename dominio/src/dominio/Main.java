package dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

  private static final int CARTAS_POR_JUGADOR = 16;

  public static void main(String[] args) {
    Mazo mazo = new Mazo();
    mazo.barajar();

    List<Carta> cartas = mazo.obtenerCartas();
    Partida partida = new Partida(mazo);
    partida.agregarJugador(crearJugador("Jugador 1", "jugador1.png", cartas));
    partida.agregarJugador(crearJugador("Jugador 2", "jugador2.png", cartas));
    partida.establecerPatron(Patron.LINEA);
    partida.iniciar();

    while (partida.obtenerEstadoPartida() == EstadoPartida.EN_CURSO
      && !hayGanador(partida)) {
      partida.cantarSiguienteCarta();
    }

    System.out.println("Cartas cantadas: " + partida.obtenerNumeroCartasCantadas());
    System.out.println("Cartas restantes: " + partida.obtenerNumeroCartasRestantes());
    System.out.println("Estado: " + partida.obtenerEstadoPartida());
    for (Jugador jugador : partida.obtenerJugadores()) {
      System.out.println(jugador.obtenerNombre()
        + " - LINEA: " + partida.cumplePatron(jugador, Patron.LINEA)
        + " - Puntaje: " + jugador.obtenerPuntaje());
    }
  }

  private static boolean hayGanador(Partida partida) {
    for (Jugador jugador : partida.obtenerJugadores()) {
      if (partida.cumplePatron(jugador, Patron.LINEA)) {
        return true;
      }
    }
    return false;
  }

  private static Jugador crearJugador(String nombre, String imagen, List<Carta> cartas) {
    List<Carta> copia = new ArrayList<>(cartas);
    Collections.shuffle(copia);
    return new Jugador(nombre, imagen, copia.subList(0, CARTAS_POR_JUGADOR));
  }
}
