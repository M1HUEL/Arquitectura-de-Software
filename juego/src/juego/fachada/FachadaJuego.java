package juego.fachada;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Timer;
import juego.modelo.Carta;
import juego.modelo.EstadoPartida;
import juego.modelo.Jugador;
import juego.modelo.Mazo;
import juego.modelo.Mensaje;
import juego.modelo.Patron;

public class FachadaJuego implements Fachada {

  private static final int FILAS = 4;
  private static final int COLUMNAS = 4;
  private static final int CARTAS_POR_JUGADOR = FILAS * COLUMNAS;
  private static final int RETARDO_CANTADA = 3000;
  private static final int TIEMPO_FINAL = 10000;

  private final String jugadorLocal;
  private final List<Jugador> jugadores;
  private final List<Carta> baraja;
  private final List<Carta> cartasCantadas;
  private final List<Runnable> oyentes;
  private final Random aleatorio;
  private final Timer temporizador;
  private final Timer finalizador;
  private EstadoPartida estadoPartida;
  private String ganador;
  private Patron patron;

  public FachadaJuego(String jugadorLocal) {
    this(jugadorLocal, new Random().nextLong());
  }

  public FachadaJuego(String jugadorLocal, long semilla) {
    if (jugadorLocal == null || jugadorLocal.isBlank()) {
      throw new IllegalArgumentException("El jugador local no puede estar vacío");
    }
    this.jugadorLocal = jugadorLocal.trim();
    this.aleatorio = new Random(semilla);
    this.jugadores = new ArrayList<>();
    List<Carta> catalogo = Mazo.crearCartas();
    this.baraja = new ArrayList<>(catalogo);
    this.cartasCantadas = new ArrayList<>();
    this.oyentes = new CopyOnWriteArrayList<>();
    this.estadoPartida = EstadoPartida.NO_INICIADA;
    this.ganador = "";
    this.patron = Patron.CHORRO;
    this.temporizador = new Timer(RETARDO_CANTADA, evento -> cantarSiguiente());
    this.finalizador = new Timer(TIEMPO_FINAL, evento -> finalizarPorAgotamiento());
    this.finalizador.setRepeats(false);
    crearJugadores(catalogo);
    Collections.shuffle(baraja, aleatorio);
  }

  @Override
  public void iniciar() {
    if (estadoPartida != EstadoPartida.NO_INICIADA) {
      return;
    }
    estadoPartida = EstadoPartida.EN_CURSO;
    cantarSiguiente();
    if (!baraja.isEmpty()) {
      temporizador.start();
    }
  }

  @Override
  public void cerrar() {
    temporizador.stop();
    finalizador.stop();
  }

  @Override
  public void agregarOyente(Runnable oyente) {
    if (oyente != null) {
      oyentes.add(oyente);
    }
  }

  @Override
  public void eliminarOyente(Runnable oyente) {
    oyentes.remove(oyente);
  }

  @Override
  public String obtenerJugadorLocal() {
    return jugadorLocal;
  }

  @Override
  public List<Jugador> obtenerJugadores() {
    return List.copyOf(jugadores);
  }

  @Override
  public List<Carta> obtenerCartasCantadas() {
    return List.copyOf(cartasCantadas);
  }

  @Override
  public int obtenerNumeroCartasRestantes() {
    return baraja.size();
  }

  @Override
  public EstadoPartida obtenerEstadoPartida() {
    return estadoPartida;
  }

  @Override
  public String obtenerGanador() {
    return ganador;
  }

  @Override
  public Patron obtenerPatron() {
    return patron;
  }

  @Override
  public String obtenerUltimoError() {
    return "";
  }

  @Override
  public Mensaje seleccionarCasilla(Jugador jugador, Carta carta) {
    if (!puedeJugar(jugador) || carta == null) {
      return Mensaje.CASILLA_INVALIDA;
    }
    if (!jugador.obtenerCartas().contains(carta)) {
      return Mensaje.CASILLA_INVALIDA;
    }
    if (!fueCantada(carta)) {
      return Mensaje.CASILLA_INVALIDA;
    }
    if (!jugador.marcarCasilla(carta)) {
      return Mensaje.CASILLA_INVALIDA;
    }
    jugador.agregarPuntos(1);
    return Mensaje.CASILLA_VALIDA;
  }

  @Override
  public Mensaje seleccionarPatron(Jugador jugador, Patron patron) {
    if (patron == null) {
      return Mensaje.PATRON_INVALIDO;
    }
    this.patron = patron;
    if (!puedeJugar(jugador) || !cumplePatron(jugador, patron)) {
      return Mensaje.PATRON_INVALIDO;
    }
    return Mensaje.PATRON_VALIDO;
  }

  @Override
  public Mensaje seleccionarVictoria(Jugador jugador, Patron patron) {
    if (patron == null || !puedeJugar(jugador) || !cumplePatron(jugador, patron)) {
      return Mensaje.VICTORIA_INVALIDA;
    }
    this.patron = patron;
    this.ganador = jugador.obtenerNombre();
    this.estadoPartida = EstadoPartida.TERMINADA;
    this.temporizador.stop();
    this.finalizador.stop();
    jugador.agregarPuntos(10);
    return Mensaje.VICTORIA_VALIDA;
  }

  private void crearJugadores(List<Carta> catalogo) {
    jugadores.add(crearJugador(jugadorLocal, catalogo));
    jugadores.add(crearJugador("Oponente 1", catalogo));
    jugadores.add(crearJugador("Oponente 2", catalogo));
  }

  private Jugador crearJugador(String nombre, List<Carta> catalogo) {
    List<Carta> disponibles = new ArrayList<>(catalogo);
    Collections.shuffle(disponibles, aleatorio);
    List<Carta> cartas = new ArrayList<>(
      disponibles.subList(0, CARTAS_POR_JUGADOR));
    return new Jugador(nombre, "", cartas);
  }

  private void cantarSiguiente() {
    if (estadoPartida != EstadoPartida.EN_CURSO) {
      return;
    }
    if (baraja.isEmpty()) {
      return;
    }
    Carta carta = baraja.remove(baraja.size() - 1);
    cartasCantadas.add(carta);
    marcarOponentes(carta);
    if (baraja.isEmpty()) {
      temporizador.stop();
      finalizador.start();
    }
    notificarOyentes();
  }

  private void finalizarPorAgotamiento() {
    if (estadoPartida == EstadoPartida.EN_CURSO && baraja.isEmpty()) {
      estadoPartida = EstadoPartida.TERMINADA;
      notificarOyentes();
    }
  }

  private void marcarOponentes(Carta carta) {
    for (Jugador jugador : jugadores) {
      if (!jugador.obtenerNombre().equals(jugadorLocal)
        && jugador.marcarCasilla(carta)) {
        jugador.agregarPuntos(1);
      }
    }
  }

  private boolean puedeJugar(Jugador jugador) {
    return estadoPartida == EstadoPartida.EN_CURSO
      && jugador != null
      && jugador.obtenerNombre().equals(jugadorLocal);
  }

  private boolean fueCantada(Carta carta) {
    return cartasCantadas.contains(carta);
  }

  private boolean cumplePatron(Jugador jugador, Patron patron) {
    return jugador != null && jugador.cumple(patron);
  }

  private void notificarOyentes() {
    for (Runnable oyente : oyentes) {
      oyente.run();
    }
  }
}
