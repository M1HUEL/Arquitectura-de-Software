package dominio.fachada;

import java.util.Optional;

public interface Fachada {

  void iniciar();

  void detener();

  boolean estaActivo();

  Optional<CartaCantada> cantarSiguienteCarta();
}
