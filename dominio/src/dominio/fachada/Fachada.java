package dominio.fachada;

import java.util.OptionalInt;

public interface Fachada {

  void iniciar();

  void detener();

  boolean estaActivo();

  OptionalInt cantarSiguienteCarta();

  int obtenerRestantes();
}
