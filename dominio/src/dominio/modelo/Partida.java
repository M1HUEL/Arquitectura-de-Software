package dominio.modelo;

import java.util.ArrayList;
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
  private final List<Jugador> ganadores;
  private EstadoPartida estado;

  public Partida(String id, List<Jugador> jugadores, Griton griton, TipoVictoria tipoVictoria) {
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
    this.ganadores = new ArrayList<>();
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
      jugadores.add(new Jugador(limpio, Tabla.generar(aleatorio)));
    }
    return new Partida(UUID.randomUUID().toString(), jugadores, Griton.completo(aleatorio), tipoVictoria);
  }

  public static Partida crear(List<String> nombresJugadores, Random aleatorio) {
    return crear(nombresJugadores, TipoVictoria.CHORRO, aleatorio);
  }

  public void iniciar() {
    if (estado == EstadoPartida.FINALIZADA) {
      throw new IllegalStateException("La partida ya terminó");
    }
    if (estado != EstadoPartida.CREADA) {
      throw new IllegalStateException("La partida no se puede iniciar en el estado " + estado);
    }
    estado = EstadoPartida.EN_CURSO;
  }

  public void pausar() {
    exigirEnCurso();
    estado = EstadoPartida.PAUSADA;
  }

  public void reanudar() {
    if (estado == EstadoPartida.FINALIZADA) {
      throw new IllegalStateException("La partida ya terminó");
    }
    if (estado != EstadoPartida.PAUSADA) {
      throw new IllegalStateException("La partida no se puede reanudar en el estado " + estado);
    }
    estado = EstadoPartida.EN_CURSO;
  }

  public void finalizar() {
    estado = EstadoPartida.FINALIZADA;
  }

  public Carta cantarSiguiente() {
    exigirEnCurso();
    return griton.gritarSiguiente();
  }

  public boolean marcar(Jugador jugador, Carta carta) {
    Objects.requireNonNull(jugador, "jugador");
    Objects.requireNonNull(carta, "carta");
    exigirEnCurso();
    Jugador participante = buscarJugador(jugador.obtenerId())
      .orElseThrow(() -> new IllegalStateException("El jugador no participa en la partida"));
    if (!griton.fueGritada(carta)) {
      throw new IllegalArgumentException("La carta todavía no se ha cantado");
    }
    if (!participante.obtenerTabla().contiene(carta)) {
      throw new IllegalArgumentException("El jugador no tiene esa carta en su tabla");
    }
    boolean marcada = participante.marcar(carta);
    if (marcada) {
      participante.sumarPunto();
    }
    return marcada;
  }

  public boolean gritarLoteria(Jugador jugador) {
    return gritarLoteria(jugador, tipoVictoria);
  }

  public boolean gritarLoteria(Jugador jugador, TipoVictoria tipoVictoria) {
    Objects.requireNonNull(jugador, "jugador");
    Objects.requireNonNull(tipoVictoria, "tipoVictoria");
    exigirEnCurso();
    Jugador participante = buscarJugador(jugador.obtenerId())
      .orElseThrow(() -> new IllegalStateException("El jugador no participa en la partida"));
    if (!participante.tieneLoteria(tipoVictoria)) {
      return false;
    }
    if (!ganadores.contains(participante)) {
      ganadores.add(participante);
    }
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

  public List<Jugador> obtenerJugadoresConLoteria() {
    return jugadores.stream()
      .filter(jugador -> jugador.tieneLoteria(tipoVictoria))
      .toList();
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

  public List<Jugador> obtenerGanadores() {
    return List.copyOf(ganadores);
  }

  public List<Carta> obtenerCartasCantadas() {
    return griton.obtenerCartasGritadas();
  }

  public List<Carta> obtenerCartasRestantes() {
    return griton.obtenerCartasRestantes();
  }

  public Optional<Carta> obtenerUltimaCarta() {
    return griton.obtenerUltimaCarta();
  }

  private void exigirEnCurso() {
    if (estado == EstadoPartida.FINALIZADA) {
      throw new IllegalStateException("La partida ya terminó");
    }
    if (estado != EstadoPartida.EN_CURSO) {
      throw new IllegalStateException("La partida no está en curso");
    }
  }
}
