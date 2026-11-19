package dominio.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class Partida {

  private final String id;
  private final List<Jugador> jugadores;
  private final Griton griton;
  private final TipoVictoria tipoVictoria;
  private Jugador ganador;
  private EstadoPartida estado;

  Partida(String id, List<Jugador> jugadores, Griton griton, TipoVictoria tipoVictoria) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("El id de la partida no puede estar vacío");
    }
    Objects.requireNonNull(jugadores, "jugadores");
    if (jugadores.isEmpty()) {
      throw new IllegalArgumentException("La partida debe tener al menos un jugador");
    }
    Set<String> ids = new HashSet<>();
    for (Jugador jugador : jugadores) {
      Objects.requireNonNull(jugador, "Los jugadores no pueden contener valores nulos");
      if (!ids.add(jugador.obtenerId())) {
        throw new IllegalArgumentException("No puede haber jugadores repetidos en la partida");
      }
    }
    this.id = id.trim();
    this.jugadores = new ArrayList<>(jugadores);
    this.griton = Objects.requireNonNull(griton, "griton");
    this.tipoVictoria = Objects.requireNonNull(tipoVictoria, "tipoVictoria");
    this.estado = EstadoPartida.CREADA;
  }

  public static Partida crear(List<String> nombresJugadores, TipoVictoria tipoVictoria, Random aleatorio) {
    Objects.requireNonNull(nombresJugadores, "nombresJugadores");
    Objects.requireNonNull(tipoVictoria, "tipoVictoria");
    Objects.requireNonNull(aleatorio, "aleatorio");
    if (nombresJugadores.isEmpty()) {
      throw new IllegalArgumentException("La partida debe tener al menos un jugador");
    }
    List<Jugador> jugadores = new ArrayList<>();
    Set<String> nombres = new HashSet<>();
    for (String nombre : nombresJugadores) {
      if (nombre == null || nombre.isBlank()) {
        throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
      }
      String limpio = nombre.trim();
      if (!nombres.add(limpio.toLowerCase(Locale.ROOT))) {
        throw new IllegalArgumentException("No puede haber jugadores con el mismo nombre");
      }
      jugadores.add(new Jugador(limpio, Tablero.generar(aleatorio)));
    }
    return new Partida(UUID.randomUUID().toString(), jugadores, Griton.completo(aleatorio), tipoVictoria);
  }

  public void iniciar() {
    exigirEstado(EstadoPartida.CREADA);
    estado = EstadoPartida.EN_CURSO;
  }

  public void pausar() {
    exigirEstado(EstadoPartida.EN_CURSO);
    estado = EstadoPartida.PAUSADA;
  }

  public void reanudar() {
    exigirEstado(EstadoPartida.PAUSADA);
    estado = EstadoPartida.EN_CURSO;
  }

  public void finalizar() {
    exigirEstado(EstadoPartida.EN_CURSO, EstadoPartida.PAUSADA);
    estado = EstadoPartida.FINALIZADA;
  }

  public Carta cantarSiguiente() {
    exigirEnCurso();
    return griton.gritarSiguiente();
  }

  public void cerrarPorBarajaAgotada() {
    exigirEnCurso();
    if (griton.puedeGritar()) {
      throw new IllegalStateException("Todavía quedan cartas por cantar");
    }
    finalizar();
  }

  public boolean marcar(String jugadorId, Carta carta) {
    Objects.requireNonNull(jugadorId, "jugadorId");
    Objects.requireNonNull(carta, "carta");
    exigirEnCurso();
    Jugador participante = buscarJugador(jugadorId)
      .orElseThrow(() -> new IllegalStateException("El jugador no participa en la partida"));
    if (!griton.fueGritada(carta)) {
      throw new IllegalArgumentException("La carta todavía no se ha cantado");
    }
    if (!participante.obtenerTablero().contiene(carta)) {
      throw new IllegalArgumentException("El jugador no tiene esa carta en su tablero");
    }
    return participante.marcar(carta);
  }

  public boolean declararPatron(String jugadorId, TipoVictoria patron) {
    Objects.requireNonNull(jugadorId, "jugadorId");
    Objects.requireNonNull(patron, "patron");
    exigirEnCurso();
    Jugador participante = buscarJugador(jugadorId)
      .orElseThrow(() -> new IllegalStateException("El jugador no participa en la partida"));
    return participante.declararPatron(patron);
  }

  public boolean gritarLoteria(String jugadorId) {
    Objects.requireNonNull(jugadorId, "jugadorId");
    exigirEnCurso();
    Jugador participante = buscarJugador(jugadorId)
      .orElseThrow(() -> new IllegalStateException("El jugador no participa en la partida"));
    if (!participante.tieneLoteria(tipoVictoria)) {
      participante.penalizarFalsa();
      return false;
    }
    participante.premiarLoteria();
    ganador = participante;
    estado = EstadoPartida.FINALIZADA;
    return true;
  }

  public Optional<Jugador> buscarJugador(String id) {
    if (id == null) {
      return Optional.empty();
    }
    return jugadores.stream()
      .filter(jugador -> jugador.obtenerId().equals(id))
      .findFirst();
  }

  public boolean puedeCantar() {
    return estado == EstadoPartida.EN_CURSO && griton.puedeGritar();
  }

  public boolean estaEnCurso() {
    return estado == EstadoPartida.EN_CURSO;
  }

  public boolean estaFinalizada() {
    return estado == EstadoPartida.FINALIZADA;
  }

  public String obtenerId() {
    return id;
  }

  public EstadoPartida obtenerEstado() {
    return estado;
  }

  public TipoVictoria obtenerTipoVictoria() {
    return tipoVictoria;
  }

  public List<Jugador> obtenerJugadores() {
    return List.copyOf(jugadores);
  }

  public Optional<Jugador> obtenerGanador() {
    return Optional.ofNullable(ganador);
  }

  public List<Jugador> obtenerClasificacion() {
    return jugadores.stream()
      .sorted(Comparator.comparingInt(Jugador::obtenerPuntos).reversed())
      .toList();
  }

  public List<Carta> obtenerCartasCantadas() {
    return griton.obtenerCartasGritadas();
  }

  public List<Carta> obtenerCartasRestantes() {
    return griton.obtenerCartasRestantes();
  }

  public int obtenerRestantes() {
    return griton.obtenerRestantes();
  }

  public Optional<Carta> obtenerUltimaCarta() {
    return griton.obtenerUltimaCarta();
  }

  private void exigirEnCurso() {
    exigirEstado(EstadoPartida.EN_CURSO);
  }

  private void exigirEstado(EstadoPartida... permitidos) {
    Objects.requireNonNull(permitidos, "permitidos");
    if (estado == EstadoPartida.FINALIZADA) {
      throw new IllegalStateException("La partida ya terminó");
    }
    for (EstadoPartida permitido : permitidos) {
      Objects.requireNonNull(permitido, "permitido");
      if (estado == permitido) {
        return;
      }
    }
    throw new IllegalStateException(
      "Se esperaba el estado " + Arrays.toString(permitidos) + " pero el estado actual es " + estado);
  }
}
