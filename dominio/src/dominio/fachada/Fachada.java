package dominio.fachada;

import comun.EstadoJuego;

public interface Fachada {

  void iniciar();

  void detener();

  boolean estaActivo();

  EstadoJuego cantarSiguienteCarta();
}
