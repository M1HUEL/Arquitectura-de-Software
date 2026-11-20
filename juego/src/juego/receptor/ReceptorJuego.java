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
    SwingUtilities.invokeLater(() -> {
      if (contenido instanceof EventoError error) {
        modelo.registrarError(error.mensaje());
      } else if (contenido instanceof EventoPartidaIniciada inicio) {
        modelo.iniciarPartida(
          convertirJugadores(inicio.nombresJugadores(), inicio.cartasPorJugador()),
          inicio.restantes());
      } else if (contenido instanceof EventoCartaCantada carta) {
        modelo.cantarCarta(carta.cartaId(), carta.restantes());
      } else if (contenido instanceof EventoCasillaMarcada casilla) {
        modelo.marcarCasilla(casilla.jugador(), casilla.cartaId());
      } else if (contenido instanceof EventoPatronDeclarado) {
        modelo.declararPatron();
      } else if (contenido instanceof EventoPuntajeActualizado punto) {
        modelo.actualizarPuntaje(punto.jugador(), punto.puntos());
      } else if (contenido instanceof EventoPartidaGanada ganada) {
        modelo.ganarPartida(ganada.jugador());
      } else if (contenido instanceof EventoPartidaFinalizada) {
        modelo.finalizarPartida();
      }
    });
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
