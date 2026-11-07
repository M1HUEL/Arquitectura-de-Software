package juego.modelo;

import comun.MensajesJuego;
import comun.MensajesJuego.EstadoPartidaJuego;
import comun.MensajesJuego.EventoPartida;
import comun.MensajesJuego.JugadorEstado;
import comun.MensajesJuego.MovimientoProcesado;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import juego.observador.Observador;

public class ModeloJuego implements Modelo {

  private final Map<String, Carta> catalogo = new LinkedHashMap<>();
  private final List<Observador> observadores = new CopyOnWriteArrayList<>();
  private volatile PublicadorMovimiento publicador;
  private volatile List<Jugador> jugadores = List.of();
  private volatile List<Carta> cartasCantadas = List.of();
  private volatile int numeroCartasRestantes;
  private volatile Patron patron;
  private volatile EstadoPartida estadoPartida = EstadoPartida.NO_INICIADA;
  private volatile Mensaje mensaje;
  private volatile long versionMensaje;
  private volatile String ganador = "";
  private volatile String ultimoEvento = "";
  private volatile boolean desconectado;

  public ModeloJuego(List<Carta> cartas) {
    if (cartas == null) {
      throw new IllegalArgumentException("Las cartas no pueden ser nulas");
    }
    for (Carta carta : cartas) {
      this.catalogo.put(carta.obtenerNombre(), carta);
    }
    this.numeroCartasRestantes = this.catalogo.size();
  }

  public void establecerPublicador(PublicadorMovimiento publicador) {
    this.publicador = publicador;
  }

  public void seleccionarCasilla(Carta carta, Jugador jugador) {
    String nombre = carta == null ? "" : carta.obtenerNombre();
    enviarMovimiento(jugador, MensajesJuego.CASILLA, nombre, null);
  }

  public void seleccionarPatron(Jugador jugador, Patron patron) {
    enviarMovimiento(jugador, MensajesJuego.PATRON, "", patron);
  }

  public void seleccionarVictoria(Jugador jugador, Patron patron) {
    enviarMovimiento(jugador, MensajesJuego.VICTORIA, "", patron);
  }

  public void aplicarEstado(EstadoPartidaJuego estado) {
    if (estado == null) {
      return;
    }
    List<Jugador> nuevosJugadores = new ArrayList<>();
    for (JugadorEstado jugadorEstado : estado.jugadores()) {
      List<Carta> cartasJugador = new ArrayList<>();
      for (String nombre : jugadorEstado.cartas()) {
        cartasJugador.add(convertirCarta(nombre));
      }
      Jugador jugador = new Jugador(jugadorEstado.nombre(), jugadorEstado.imagen(),
        cartasJugador);
      jugador.agregarPuntos(jugadorEstado.puntaje());
      nuevosJugadores.add(jugador);
    }

    List<Carta> nuevasCantadas = new ArrayList<>();
    for (String nombre : estado.cartasCantadas()) {
      nuevasCantadas.add(convertirCarta(nombre));
    }

    this.jugadores = List.copyOf(nuevosJugadores);
    this.cartasCantadas = List.copyOf(nuevasCantadas);
    this.numeroCartasRestantes = estado.cartasRestantes();
    this.patron = leerPatron(estado.patron());
    this.estadoPartida = leerEstado(estado.estado());
    this.ganador = estado.ganador();
    notificarObservadores();
  }

  public void aplicarEvento(EventoPartida evento) {
    if (evento == null) {
      return;
    }
    this.ultimoEvento = evento.tipo();
    notificarObservadores();
  }

  public void aplicarMovimientoProcesado(MovimientoProcesado movimiento) {
    if (movimiento == null) {
      return;
    }
    Mensaje resultado = null;
    switch (movimiento.tipo()) {
      case MensajesJuego.CASILLA ->
        resultado = movimiento.valido() ? Mensaje.CASILLA_VALIDA : Mensaje.CASILLA_INVALIDA;
      case MensajesJuego.PATRON ->
        resultado = movimiento.valido() ? Mensaje.PATRON_VALIDO : Mensaje.PATRON_INVALIDO;
      case MensajesJuego.VICTORIA ->
        resultado = movimiento.valido() ? Mensaje.VICTORIA_VALIDA : Mensaje.VICTORIA_INVALIDA;
      default -> {
        return;
      }
    }
    notificarObservadores(resultado);
  }

  public void registrarDesconexion(Throwable causa) {
    this.desconectado = true;
    notificarObservadores();
  }

  public void agregarObservador(Observador observador) {
    if (observador != null) {
      observadores.add(observador);
    }
  }

  public void eliminarObservador(Observador observador) {
    observadores.remove(observador);
  }

  public void notificarObservadores() {
    for (Observador observador : observadores) {
      observador.actualizar(this);
    }
  }

  private void notificarObservadores(Mensaje mensaje) {
    this.mensaje = mensaje;
    this.versionMensaje++;
    notificarObservadores();
  }

  private void enviarMovimiento(Jugador jugador, String tipo, String carta, Patron patron) {
    PublicadorMovimiento actual = this.publicador;
    if (actual == null || this.desconectado || jugador == null) {
      return;
    }
    try {
      actual.publicar(jugador.obtenerNombre(), tipo, carta, patron);
    } catch (IOException e) {
      registrarDesconexion(e);
    }
  }

  private Carta convertirCarta(String nombre) {
    Carta carta = catalogo.get(nombre);
    return carta != null ? carta : new Carta(nombre, "");
  }

  private Patron leerPatron(String valor) {
    if (valor == null || valor.isBlank()) {
      return null;
    }
    try {
      return Patron.valueOf(valor);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private EstadoPartida leerEstado(String valor) {
    try {
      return EstadoPartida.valueOf(valor);
    } catch (IllegalArgumentException | NullPointerException e) {
      return EstadoPartida.NO_INICIADA;
    }
  }

  @Override
  public Mensaje obtenerMensaje() {
    return mensaje;
  }

  @Override
  public long obtenerVersionMensaje() {
    return versionMensaje;
  }

  @Override
  public List<Jugador> obtenerJugadores() {
    return jugadores;
  }

  public List<Carta> obtenerCartas() {
    return List.copyOf(catalogo.values());
  }

  public int obtenerNumeroCartas() {
    return catalogo.size();
  }

  @Override
  public int obtenerNumeroCartasRestantes() {
    return numeroCartasRestantes;
  }

  @Override
  public List<Carta> obtenerCartasCantadas() {
    return cartasCantadas;
  }

  public int obtenerNumeroCartasCantadas() {
    return cartasCantadas.size();
  }

  public Patron obtenerPatron() {
    return patron;
  }

  public String obtenerGanador() {
    return ganador;
  }

  public String obtenerUltimoEvento() {
    return ultimoEvento;
  }

  @Override
  public EstadoPartida obtenerEstadoPartida() {
    return estadoPartida;
  }

  @Override
  public boolean estaDesconectado() {
    return desconectado;
  }
}
