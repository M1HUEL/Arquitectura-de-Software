package juego.manejador;

import comun.CartaMensaje;
import comun.EstadoJuego;
import comun.evento.EventoCartaCantada;
import comun.evento.EventoError;
import comun.evento.EventoPartidaFinalizada;
import comun.evento.EventoEstadoSincronizado;
import comun.evento.EventoPartidaGanada;
import comun.JugadorMensaje;
import comunicacion.api.IReceptorExterno;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import juego.modelo.Carta;
import juego.modelo.EstadoPartida;
import juego.modelo.Jugador;
import juego.modelo.ModeloJuego;
import javax.swing.SwingUtilities;
import serializador.ISerializador;

public class ManejadorSocket implements IReceptorExterno {

  private final ISerializador serializador;
  private final ModeloJuego modelo;

  public ManejadorSocket(ISerializador serializador, ModeloJuego modelo) {
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
    Object contenido;
    try {
      contenido = serializador.deserializar(datos, Object.class);
    } catch (RuntimeException e) {
      modelo.registrarError(
        "No se pudo interpretar un mensaje del servidor: " + e.getMessage());
      return;
    }
    SwingUtilities.invokeLater(() -> procesar(contenido));
  }

  private void procesar(Object contenido) {
    if (contenido instanceof EventoError evento) {
      modelo.registrarError(evento.mensaje());
    } else if (contenido instanceof EventoEstadoSincronizado evento) {
      actualizarModelo(evento.estado());
    } else if (contenido instanceof EventoCartaCantada evento) {
      actualizarModelo(evento.estado());
    } else if (contenido instanceof EventoPartidaGanada evento) {
      actualizarModelo(evento.estado());
    } else if (contenido instanceof EventoPartidaFinalizada evento) {
      actualizarModelo(evento.estado());
    }
  }

  private void actualizarModelo(EstadoJuego estado) {
    modelo.actualizar(
      convertirEstado(estado.estado()),
      convertirJugadores(estado.jugadores()),
      convertirCartas(estado.cartasCantadas()),
      estado.restantes(),
      estado.ganador()
    );
  }

  private EstadoPartida convertirEstado(String estado) {
    if (estado == null || estado.isBlank()) {
      return EstadoPartida.NO_INICIADA;
    }
    return switch (estado.trim().toUpperCase(Locale.ROOT)) {
      case "EN_CURSO" ->
        EstadoPartida.EN_CURSO;
      case "FINALIZADA", "TERMINADA" ->
        EstadoPartida.TERMINADA;
      default ->
        EstadoPartida.NO_INICIADA;
    };
  }

  private List<Jugador> convertirJugadores(List<JugadorMensaje> mensajes) {
    List<Jugador> resultado = new ArrayList<>();
    for (JugadorMensaje mensaje : mensajes) {
      List<Carta> cartas = new ArrayList<>();
      Map<Integer, Carta> cartasPorId = new HashMap<>();
      for (CartaMensaje cartaMensaje : mensaje.cartas()) {
        Carta carta = new Carta(cartaMensaje.id(), cartaMensaje.nombre());
        cartas.add(carta);
        cartasPorId.put(cartaMensaje.id(), carta);
      }
      Jugador jugador = new Jugador(mensaje.nombre(), cartas);
      for (Integer id : mensaje.marcadas()) {
        Carta carta = cartasPorId.get(id);
        if (carta != null) {
          jugador.marcarCasilla(carta);
        }
      }
      jugador.establecerPuntaje(mensaje.aciertos());
      resultado.add(jugador);
    }
    return List.copyOf(resultado);
  }

  private List<Carta> convertirCartas(List<CartaMensaje> mensajes) {
    List<Carta> resultado = new ArrayList<>();
    for (CartaMensaje mensaje : mensajes) {
      resultado.add(new Carta(mensaje.id(), mensaje.nombre()));
    }
    return List.copyOf(resultado);
  }
}
