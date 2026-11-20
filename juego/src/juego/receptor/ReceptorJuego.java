package juego.receptor;

import comun.evento.Evento;
import comun.evento.EventoCartaCantada;
import comun.evento.EventoCasillaMarcada;
import comun.evento.EventoError;
import comun.evento.EventoPartidaFinalizada;
import comun.evento.EventoPartidaGanada;
import comun.evento.EventoPartidaIniciada;
import comun.evento.EventoPatronDeclarado;
import comun.evento.EventoPuntajeActualizado;
import comunicacion.api.IReceptorExterno;
import java.util.ArrayList;
import java.util.List;
import juego.modelo.Carta;
import juego.modelo.Jugador;
import juego.modelo.ModeloJuego;
import javax.swing.SwingUtilities;
import comun.serializador.ISerializador;

public class ReceptorJuego implements IReceptorExterno {

  private final ISerializador serializador;
  private final ModeloJuego modelo;

  public ReceptorJuego(ISerializador serializador, ModeloJuego modelo) {
    if (serializador == null) {
      throw new IllegalArgumentException("El serializador no puede ser nulo");
    }
    if (modelo == null) {
      throw new IllegalArgumentException("El modelo no puede ser nulo");
    }
    this.serializador = serializador;
    this.modelo = modelo;
  }

  @Override
  public void alRecibir(byte[] datos) {
    Object sobre;
    try {
      sobre = serializador.deserializar(datos, Object.class);
    } catch (RuntimeException e) {
      modelo.registrarError(
        "No se pudo interpretar un mensaje del servidor: " + e.getMessage());
      return;
    }
    if (!(sobre instanceof Evento evento)) {
      modelo.registrarError("El servidor envió un mensaje inesperado");
      return;
    }
    Object contenido = evento.payload();
    SwingUtilities.invokeLater(() -> procesar(contenido));
  }

  private void procesar(Object contenido) {
    if (contenido instanceof EventoError evento) {
      modelo.registrarError(evento.mensaje());
    } else if (contenido instanceof EventoPartidaIniciada evento) {
      modelo.iniciarPartida(
        convertirJugadores(evento.nombresJugadores(), evento.cartasPorJugador()),
        evento.restantes());
    } else if (contenido instanceof EventoCartaCantada evento) {
      modelo.cantarCarta(evento.cartaId(), evento.restantes());
    } else if (contenido instanceof EventoCasillaMarcada evento) {
      modelo.marcarCasilla(evento.jugador(), evento.cartaId());
    } else if (contenido instanceof EventoPatronDeclarado) {
      modelo.declararPatron();
    } else if (contenido instanceof EventoPuntajeActualizado evento) {
      modelo.actualizarPuntaje(evento.jugador(), evento.puntos());
    } else if (contenido instanceof EventoPartidaGanada evento) {
      modelo.ganarPartida(evento.jugador());
    } else if (contenido instanceof EventoPartidaFinalizada) {
      modelo.finalizarPartida();
    }
  }

  private List<Jugador> convertirJugadores(
    List<String> nombres,
    List<List<Integer>> cartasPorJugador
  ) {
    List<Jugador> resultado = new ArrayList<>();
    int cantidad = Math.min(nombres.size(), cartasPorJugador.size());
    for (int i = 0; i < cantidad; i++) {
      List<Carta> cartas = new ArrayList<>();
      for (Integer id : cartasPorJugador.get(i)) {
        cartas.add(Carta.desdeId(id));
      }
      resultado.add(new Jugador(nombres.get(i), cartas));
    }
    return List.copyOf(resultado);
  }
}
