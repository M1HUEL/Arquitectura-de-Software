package dominio;

import dominio.modelo.Baraja;
import dominio.modelo.Carta;
import dominio.modelo.Casilla;
import dominio.modelo.CatalogoCartas;
import dominio.modelo.EstadoPartida;
import dominio.modelo.Griton;
import dominio.modelo.Jugador;
import dominio.modelo.Partida;
import dominio.modelo.Tablero;
import dominio.modelo.TipoVictoria;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public final class Main {

  private static int verificaciones;

  private Main() {
  }

  public static void main(String[] args) {
    ejecutarPruebas();
    ejecutarDemostracion();
  }

  private static void ejecutarPruebas() {
    verificarCatalogo();
    verificarRevolver();
    verificarTablero();
    verificarBaraja();
    verificarGriton();
    verificarPartida();
    verificarReglas();
    verificarEncapsulamiento();
    verificarDeterminismo();
    System.out.println("OK: " + verificaciones + " verificaciones");
  }

  private static void ejecutarDemostracion() {
    Random generador = new Random(2026L);
    Partida partida = Partida.crear(
      List.of("Ana", "Luis", "Sofía"),
      TipoVictoria.TABLA_LLENA,
      generador
    );
    partida.iniciar();

    System.out.println("Partida " + partida.obtenerId());
    System.out.println("Victoria: " + partida.obtenerTipoVictoria().obtenerDescripcion());
    System.out.println("Patrones: +" + Jugador.PUNTOS_POR_PATRON + " puntos");

    while (partida.puedeCantar() && !partida.estaFinalizada()) {
      Carta carta = partida.cantarSiguiente();
      System.out.println("Carta cantada: " + carta);

      for (Jugador jugador : partida.obtenerJugadores()) {
        try {
          boolean marcada = partida.marcar(jugador, carta);
          System.out.println("  " + jugador.obtenerNombre() + " -> "
            + (marcada ? "marcó" : "ya estaba marcada"));
        } catch (IllegalArgumentException excepcion) {
          System.out.println("  " + jugador.obtenerNombre() + " -> no tiene la carta");
        }
      }

      for (Jugador jugador : partida.obtenerJugadoresConLoteria()) {
        if (partida.gritarLoteria(jugador)) {
          System.out.println("¡Lotería! Ganó " + jugador.obtenerNombre());
          break;
        }
      }
    }

    if (partida.obtenerGanadores().isEmpty()) {
      partida.finalizar();
      System.out.println("Se agotó el mazo sin ganador");
    }
  }

  private static void verificarCatalogo() {
    afirmar(CatalogoCartas.obtenerTotal() == 54, "El catálogo debe tener 54 cartas");
    Set<Integer> ids = new HashSet<>();
    Set<String> nombres = new HashSet<>();
    for (Carta carta : CatalogoCartas.obtenerTodas()) {
      afirmar(carta.obtenerId() >= 1 && carta.obtenerId() <= 54, "Id de carta fuera de rango");
      afirmar(ids.add(carta.obtenerId()), "Id de carta repetido");
      afirmar(nombres.add(carta.obtenerNombre().toLowerCase()), "Nombre de carta repetido");
    }
    afirmar(CatalogoCartas.obtenerPorId(1).obtenerNombre().equals("El Gallo"), "La carta 1 debe ser El Gallo");
    afirmar(CatalogoCartas.obtenerPorId(54).obtenerNombre().equals("La Rana"), "La carta 54 debe ser La Rana");
    afirmar(CatalogoCartas.obtenerPorNombre("el gallo").obtenerId() == 1, "La búsqueda por nombre debe ignorar mayúsculas");
    afirmar(CatalogoCartas.contiene(carta(54)), "El catálogo debe contener La Rana");
    afirmar(ids.size() == 54, "El catálogo debe tener 54 ids únicos");
    try {
      CatalogoCartas.obtenerPorId(99);
      fallar("Buscar un id inexistente debió lanzar una excepción");
    } catch (NoSuchElementException esperada) {
      verificar();
    }
    try {
      CatalogoCartas.obtenerPorNombre("La Carta Inexistente");
      fallar("Buscar un nombre inexistente debió lanzar una excepción");
    } catch (NoSuchElementException esperada) {
      verificar();
    }
  }

  private static void verificarRevolver() {
    Baraja primera = new Baraja(CatalogoCartas.obtenerTodas());
    Baraja segunda = new Baraja(CatalogoCartas.obtenerTodas());

    primera.revolver(new Random(9L));
    segunda.revolver(new Random(9L));

    afirmar(primera.obtenerRestantes() == 54, "Revolver no debe perder cartas de la baraja");
    afirmar(primera.obtenerCartasRestantes().equals(segunda.obtenerCartasRestantes()), "Revolver debe ser determinista con la misma semilla");
  }

  private static void verificarTablero() {
    Tablero vacia = tableroDesde(1);
    afirmar(!vacia.tieneLinea(), "Un tablero sin marcas no puede tener línea");
    afirmar(vacia.obtenerAciertos() == 0, "Un tablero nuevo debe tener cero aciertos");

    Tablero fila = tableroDesde(1);
    marcar(fila, 1, 2, 3, 4);
    afirmar(fila.tieneLinea(), "Cuatro cartas de una fila deben formar línea");
    afirmar(fila.cumple(TipoVictoria.CHORRO), "El tablero debe cumplir la victoria por línea");

    Tablero columna = tableroDesde(1);
    marcar(columna, 1, 5, 9, 13);
    afirmar(columna.tieneLinea(), "Cuatro cartas de una columna deben formar línea");

    Tablero diagonal = tableroDesde(1);
    marcar(diagonal, 1, 6, 11, 16);
    afirmar(diagonal.tieneLinea(), "La diagonal principal debe formar línea");

    Tablero diagonalInversa = tableroDesde(1);
    marcar(diagonalInversa, 4, 7, 10, 13);
    afirmar(diagonalInversa.tieneLinea(), "La diagonal inversa debe formar línea");

    Tablero centro = tableroDesde(1);
    marcar(centro, 6, 7, 10, 11);
    afirmar(centro.tieneCentro(), "El bloque central de 2x2 debe formar centro");
    afirmar(centro.cumple(TipoVictoria.CENTRO), "El tablero debe cumplir la victoria por centro");

    Tablero esquinas = tableroDesde(1);
    marcar(esquinas, 1, 4, 13, 16);
    afirmar(esquinas.tieneCuatroEsquinas(), "Las cuatro esquinas deben estar marcadas");
    afirmar(esquinas.cumple(TipoVictoria.CUATRO_ESQUINAS), "El tablero debe cumplir la victoria por esquinas");

    Tablero lleno = tableroDesde(1);
    for (int id = 1; id <= 16; id++) {
      afirmar(marcarTablero(lleno, carta(id)), "La carta " + id + " debe pertenecer al tablero");
    }
    afirmar(lleno.tieneTableroLleno(), "El tablero con 16 marcas debe estar lleno");
    afirmar(lleno.cumple(TipoVictoria.TABLA_LLENA), "El tablero debe cumplir la victoria por tablero lleno");
    afirmar(lleno.obtenerCartasMarcadas().size() == 16, "Deben existir 16 cartas marcadas");

    Tablero generada = Tablero.generar(new Random(7L));
    afirmar(generada.obtenerCartas().size() == 16, "El tablero generado debe tener 16 cartas");
    afirmar(new HashSet<>(generada.obtenerCartas()).size() == 16, "El tablero generado no debe repetir cartas");

    afirmar(!marcarTablero(vacia, carta(54)), "No se puede marcar una carta que no está en el tablero");
    afirmar(!vacia.estaMarcada(0, 0), "La posición debe iniciar sin marca");
    afirmar(!desmarcarTablero(vacia, carta(54)), "No se puede desmarcar una carta ausente");

    Tablero tableroCasillas = tableroDesde(1);
    Casilla casilla = tableroCasillas.obtenerCasilla(0, 0);
    afirmar(casilla.obtenerFila() == 0, "La casilla debe conservar su fila");
    afirmar(casilla.obtenerColumna() == 0, "La casilla debe conservar su columna");
    afirmar(casilla.obtenerCarta().equals(carta(1)), "La casilla debe conservar su carta");
    afirmar(casilla.contiene(carta(1)), "La casilla debe contener su carta");
    afirmar(!casilla.contiene(carta(2)), "La casilla no debe contener otra carta");
    afirmar(!casilla.estaMarcada(), "La casilla debe iniciar sin marca");
    afirmar(marcarCasilla(casilla), "La casilla debe marcarse la primera vez");
    afirmar(casilla.estaMarcada(), "La casilla debe quedar marcada");
    afirmar(!marcarCasilla(casilla), "La casilla no debe marcarse dos veces");
    afirmar(tableroCasillas.estaMarcada(0, 0), "El tablero debe reflejar la marca de la casilla");
    afirmar(tableroCasillas.obtenerCasillas().size() == 16, "El tablero debe exponer sus 16 casillas");
    afirmar(tableroCasillas.obtenerCasillasMarcadas().size() == 1, "El tablero debe exponer una casilla marcada");
    afirmar(desmarcarCasilla(casilla), "La casilla debe desmarcarse");
    afirmar(!desmarcarCasilla(casilla), "La casilla no debe desmarcarse dos veces");
    afirmar(!tableroCasillas.estaMarcada(0, 0), "El tablero debe reflejar el desmarque de la casilla");
    afirmar(marcarTablero(tableroCasillas, carta(1)), "El tablero debe marcar una carta propia");
    reiniciarTablero(tableroCasillas);
    afirmar(tableroCasillas.obtenerAciertos() == 0, "Reiniciar debe quitar todas las marcas");

    try {
      tableroCasillas.obtenerCasilla(4, 0);
      fallar("Una posición fuera del tablero debe lanzar una excepción");
    } catch (IllegalArgumentException esperada) {
      verificar();
    }

    try {
      List<Carta> repetidas = new ArrayList<>();
      for (int id = 1; id <= 15; id++) {
        repetidas.add(carta(id));
      }
      repetidas.add(carta(15));
      new Tablero(repetidas);
      fallar("El tablero debió rechazar cartas repetidas");
    } catch (IllegalArgumentException esperada) {
      verificar();
    }
  }

  private static void verificarBaraja() {
    Baraja completa = new Baraja(CatalogoCartas.obtenerTodas());
    completa.revolver(new Random(11L));
    afirmar(completa.obtenerRestantes() == 54, "La baraja completa debe tener 54 cartas");
    afirmar(completa.obtenerCantadas().isEmpty(), "La baraja nueva no debe tener cartas cantadas");
    Set<Carta> sacadas = new HashSet<>();
    while (!completa.estaVacia()) {
      afirmar(sacadas.add(completa.sacarSiguiente()), "La baraja no debe repetir cartas al sacar");
    }
    afirmar(sacadas.size() == 54, "Se deben sacar las 54 cartas");
    afirmar(completa.fueCantada(carta(11)), "La baraja debe recordar las cartas cantadas");
    afirmar(completa.obtenerUltimaCantada().isPresent(), "La baraja debe exponer la última carta cantada");
    try {
      completa.sacarSiguiente();
      fallar("Sacar de una baraja vacía debió lanzar una excepción");
    } catch (NoSuchElementException esperada) {
      verificar();
    }

    Baraja ordenada = new Baraja(List.of(carta(4), carta(3), carta(2), carta(1)));
    afirmar(ordenada.sacarSiguiente().obtenerId() == 1, "La baraja debe sacar desde la parte superior");
    afirmar(ordenada.obtenerRestantes() == 3, "La baraja debe descontar la carta sacada");
    afirmar(ordenada.obtenerCantadas().size() == 1, "La baraja debe registrar la carta sacada");
    afirmar(ordenada.obtenerUltimaCantada().orElseThrow().obtenerId() == 1, "La última cantada debe ser la sacada");

    try {
      new Baraja(List.of(carta(1), carta(1)));
      fallar("La baraja debió rechazar cartas repetidas");
    } catch (IllegalArgumentException esperada) {
      verificar();
    }
  }

  private static void verificarGriton() {
    Griton griton = Griton.completo(new Random(5L));
    afirmar(griton.obtenerRestantes() == 54, "El gritón completo debe tener 54 cartas");
    afirmar(griton.puedeGritar(), "El gritón debe poder gritar al iniciar");
    afirmar(!griton.estaVacio(), "El gritón no debe estar vacío al iniciar");
    afirmar(griton.obtenerUltimaCarta().isEmpty(), "El gritón no debe tener última carta al iniciar");

    Set<Carta> gritadas = new HashSet<>();
    while (griton.puedeGritar()) {
      Carta carta = griton.gritarSiguiente();
      afirmar(gritadas.add(carta), "El gritón no debe repetir cartas");
      afirmar(griton.fueGritada(carta), "El gritón debe recordar las cartas gritadas");
      afirmar(griton.obtenerUltimaCarta().orElseThrow().equals(carta), "La última carta debe ser la gritada");
    }

    afirmar(gritadas.size() == 54, "El gritón debe gritar las 54 cartas");
    afirmar(griton.obtenerCartasGritadas().size() == 54, "El gritón debe conservar el historial");
    afirmar(griton.estaVacio(), "El gritón debe quedar vacío");
    afirmar(!griton.puedeGritar(), "El gritón vacío no debe poder gritar");
    try {
      griton.gritarSiguiente();
      fallar("Gritar sin cartas debió lanzar una excepción");
    } catch (NoSuchElementException esperada) {
      verificar();
    }

    Griton ordenado = new Griton(
      new Baraja(List.of(carta(4), carta(3), carta(2), carta(1))),
      new Random(1L)
    );
    afirmar(ordenado.gritarSiguiente().obtenerId() == 1, "El gritón debe respetar el orden del mazo al no barajear");
    afirmar(ordenado.gritarSiguiente().obtenerId() == 2, "El gritón debe seguir el orden del mazo");
    afirmar(ordenado.obtenerRestantes() == 2, "El gritón debe descontar las cartas gritadas");
    afirmar(ordenado.obtenerUltimaCarta().orElseThrow().obtenerId() == 2, "La última carta debe coincidir");
    int restantes = ordenado.obtenerRestantes();
    ordenado.barajear();
    afirmar(ordenado.obtenerRestantes() == restantes, "Barajear no debe perder las cartas restantes");

    Griton primero = Griton.completo(new Random(77L));
    Griton segundo = Griton.completo(new Random(77L));
    afirmar(primero.obtenerCartasRestantes().equals(segundo.obtenerCartasRestantes()), "El gritón debe ser determinista con la misma semilla");
  }

  private static void verificarPartida() {
    Random generador = new Random(42L);
    Partida partida = Partida.crear(List.of("Ana", "Luis"), TipoVictoria.CHORRO, generador);
    afirmar(partida.obtenerEstado() == EstadoPartida.CREADA, "La partida debe iniciar creada");
    afirmar(partida.obtenerJugadores().size() == 2, "La partida debe tener dos jugadores");
    afirmar(partida.obtenerCartasRestantes().size() == 54, "La partida debe iniciar con 54 cartas");
    afirmar(partida.obtenerCartasCantadas().isEmpty(), "La partida no debe tener cartas cantadas al iniciar");
    afirmar(partida.obtenerUltimaCarta().isEmpty(), "No debe existir última carta al iniciar");
    afirmar(!partida.puedeCantar(), "No se puede cantar antes de iniciar");

    try {
      partida.cantarSiguiente();
      fallar("No se puede cantar antes de iniciar");
    } catch (IllegalStateException esperada) {
      verificar();
    }

    partida.iniciar();
    afirmar(partida.estaEnCurso(), "La partida debe estar en curso");
    afirmar(partida.puedeCantar(), "La partida debe permitir cantar");

    Carta primera = partida.cantarSiguiente();
    afirmar(partida.obtenerCartasCantadas().size() == 1, "Debe registrarse la carta cantada");
    afirmar(partida.obtenerUltimaCarta().orElseThrow().equals(primera), "La última carta debe ser la cantada");
    afirmar(partida.obtenerCartasRestantes().size() == 53, "El mazo debe quedar con 53 cartas");
    afirmar(!partida.obtenerCartasRestantes().contains(primera), "La carta cantada no debe seguir en el mazo");

    Carta noCantada = CatalogoCartas.obtenerPorId(primera.obtenerId() == 54 ? 1 : primera.obtenerId() + 1);
    try {
      partida.marcar(partida.obtenerJugadores().get(0), noCantada);
      fallar("No se puede marcar una carta que no se ha cantado");
    } catch (IllegalArgumentException esperada) {
      verificar();
    }

    for (Jugador jugador : partida.obtenerJugadores()) {
      try {
        partida.marcar(jugador, primera);
      } catch (IllegalArgumentException esperada) {
        verificar();
      }
    }

    Jugador externo = new Jugador("Externo", tableroDesde(39));
    try {
      partida.marcar(externo, primera);
      fallar("Un jugador externo no puede marcar en la partida");
    } catch (IllegalStateException esperada) {
      verificar();
    }

    partida.pausar();
    afirmar(partida.obtenerEstado() == EstadoPartida.PAUSADA, "La partida debe quedar pausada");
    afirmar(!partida.puedeCantar(), "Una partida pausada no puede cantar");
    try {
      partida.cantarSiguiente();
      fallar("Una partida pausada no puede cantar");
    } catch (IllegalStateException esperada) {
      verificar();
    }
    partida.reanudar();
    afirmar(partida.estaEnCurso(), "La partida debe reanudarse");

    Jugador ana = new Jugador("Ana", tableroDesde(1));
    Jugador luis = new Jugador("Luis", tableroDesde(17));
    List<Carta> mazo = new ArrayList<>();
    for (int id = Tablero.TOTAL_CARTAS; id >= 1; id--) {
      mazo.add(carta(id));
    }
    Partida victoria = new Partida(
      "partida-victoria",
      List.of(ana, luis),
      new Griton(new Baraja(mazo), new Random(1L)),
      TipoVictoria.TABLA_LLENA
    );
    victoria.iniciar();
    for (int id = 1; id <= Tablero.TOTAL_CARTAS; id++) {
      Carta cantada = victoria.cantarSiguiente();
      afirmar(cantada.obtenerId() == id, "Las cartas deben cantarse en el orden esperado");
      afirmar(victoria.marcar(ana, cantada), "Ana debe marcar la carta cantada");
      try {
        victoria.marcar(luis, cantada);
        fallar("Luis no puede marcar una carta que no tiene");
      } catch (IllegalArgumentException esperada) {
        verificar();
      }
    }
    afirmar(victoria.obtenerJugadoresConLoteria().contains(ana), "Ana debe tener el tablero lleno");
    afirmar(!victoria.obtenerJugadoresConLoteria().contains(luis), "Luis no debe tener el tablero lleno");
    afirmar(victoria.gritarLoteria(ana), "Ana debe ganar al gritar lotería");
    afirmar(victoria.estaFinalizada(), "La partida debe finalizar al ganar");
    afirmar(victoria.obtenerGanadores().contains(ana), "Ana debe quedar registrada como ganadora");
    try {
      victoria.gritarLoteria(luis);
      fallar("No se puede gritar lotería después de finalizar");
    } catch (IllegalStateException esperada) {
      verificar();
    }
    try {
      victoria.marcar(ana, carta(1));
      fallar("No se puede marcar después de finalizar");
    } catch (IllegalStateException esperada) {
      verificar();
    }
    try {
      victoria.pausar();
      fallar("No se puede pausar una partida finalizada");
    } catch (IllegalStateException esperada) {
      verificar();
    }
    try {
      victoria.iniciar();
      fallar("No se puede reiniciar una partida finalizada");
    } catch (IllegalStateException esperada) {
      verificar();
    }
    afirmar(victoria.obtenerGanadores().size() == 1, "Debe existir un solo ganador");

    Partida sinCartas = new Partida(
      "partida-sin-cartas",
      List.of(new Jugador("Ana", tableroDesde(1))),
      new Griton(new Baraja(List.of(carta(1))), new Random(1L)),
      TipoVictoria.CHORRO
    );
    sinCartas.iniciar();
    sinCartas.cantarSiguiente();
    afirmar(!sinCartas.puedeCantar(), "No se puede cantar sin cartas");
    try {
      sinCartas.cantarSiguiente();
      fallar("Cantar sin cartas debió lanzar una excepción");
    } catch (NoSuchElementException esperada) {
      verificar();
      afirmar(sinCartas.estaEnCurso(), "La excepción no debe cambiar el estado de la partida");
    }
    afirmar(!sinCartas.estaFinalizada(), "La partida no debe finalizar como efecto de la excepción");
    sinCartas.finalizar();
    afirmar(sinCartas.estaFinalizada(), "El llamador debe poder finalizar la partida agotada");

    try {
      Partida.crear(List.of("Ana", "ana"), new Random(1L));
      fallar("No se deben permitir nombres repetidos");
    } catch (IllegalArgumentException esperada) {
      verificar();
    }
  }

  private static void verificarReglas() {
    verificarGritoDentroDeVentana();
    verificarPrimerGritoGana();
    verificarPuntos();
    verificarGanadorPorPuntos();
  }

  private static void verificarGritoDentroDeVentana() {
    Jugador ana = new Jugador("Ana", tableroDesde(1));
    Jugador luis = new Jugador("Luis", tableroDesde(17));
    List<Carta> mazo = new ArrayList<>();
    for (int id = 18; id >= 1; id--) {
      mazo.add(carta(id));
    }
    Partida partida = new Partida(
      "grito-ventana",
      List.of(ana, luis),
      new Griton(new Baraja(mazo), new Random(1L)),
      TipoVictoria.TABLA_LLENA
    );
    partida.iniciar();
    for (int id = 1; id <= Tablero.TOTAL_CARTAS; id++) {
      partida.marcar(ana, partida.cantarSiguiente());
    }
    afirmar(ana.obtenerTablero().tieneTableroLleno(), "El tablero de Ana debe estar lleno");
    afirmar(partida.gritarLoteria(ana), "El grito en la carta que llena el tablero debe valer");

    Partida vencida = new Partida(
      "grito-vencido",
      List.of(new Jugador("Ana", tableroDesde(1))),
      new Griton(new Baraja(mazo), new Random(1L)),
      TipoVictoria.TABLA_LLENA
    );
    vencida.iniciar();
    for (int id = 1; id <= Tablero.TOTAL_CARTAS; id++) {
      vencida.marcar(vencida.obtenerJugadores().get(0), vencida.cantarSiguiente());
    }
    vencida.cantarSiguiente();
    afirmar(!vencida.gritarLoteria(vencida.obtenerJugadores().get(0)),
      "El grito después de cantar la siguiente carta no debe valer");
    afirmar(vencida.estaEnCurso(), "Un grito vencido no debe finalizar la partida");
  }

  private static void verificarPrimerGritoGana() {
    Jugador ana = new Jugador("Ana", tableroDesde(1));
    Jugador luis = new Jugador("Luis", tableroDesde(1));
    List<Carta> mazo = new ArrayList<>();
    for (int id = Tablero.TOTAL_CARTAS; id >= 1; id--) {
      mazo.add(carta(id));
    }
    Partida partida = new Partida(
      "primer-grito",
      List.of(ana, luis),
      new Griton(new Baraja(mazo), new Random(1L)),
      TipoVictoria.TABLA_LLENA
    );
    partida.iniciar();
    for (int id = 1; id <= Tablero.TOTAL_CARTAS; id++) {
      Carta cantada = partida.cantarSiguiente();
      partida.marcar(ana, cantada);
      partida.marcar(luis, cantada);
    }
    afirmar(partida.gritarLoteria(luis), "Luis debe ganar al gritar primero");
    afirmar(partida.obtenerGanadores().equals(List.of(luis)), "Solo el primero que grita debe ganar");
  }

  private static void verificarPuntos() {
    Jugador ana = new Jugador("Ana", tableroDesde(1));
    Partida partida = new Partida(
      "puntos",
      List.of(ana),
      new Griton(new Baraja(List.of(carta(4), carta(3), carta(2), carta(1))), new Random(1L)),
      TipoVictoria.CHORRO
    );
    partida.iniciar();
    for (int id = 1; id <= 4; id++) {
      partida.marcar(ana, partida.cantarSiguiente());
    }
    afirmar(ana.obtenerPuntos() == 4, "Cada casilla marcada debe sumar un punto");
    afirmar(ana.puntuarPatron(TipoVictoria.CHORRO), "El chorro completo debe sumar puntos");
    afirmar(ana.obtenerPuntos() == 9, "Un patrón completo debe sumar cinco puntos");
    afirmar(!ana.puntuarPatron(TipoVictoria.CHORRO), "El mismo patrón no debe puntuarse dos veces");
    afirmar(!ana.puntuarPatron(TipoVictoria.CENTRO), "Un patrón incompleto no debe sumar puntos");
    afirmar(ana.obtenerPuntos() == 9, "Los patrones inválidos no deben alterar el puntaje");
  }

  private static void verificarGanadorPorPuntos() {
    Jugador ana = new Jugador("Ana", tableroDesde(1));
    Jugador luis = new Jugador("Luis", tableroDesde(17));
    Partida partida = new Partida(
      "ganador-puntos",
      List.of(ana, luis),
      new Griton(new Baraja(List.of(carta(2), carta(1))), new Random(1L)),
      TipoVictoria.TABLA_LLENA
    );
    partida.iniciar();
    partida.marcar(ana, partida.cantarSiguiente());
    partida.marcar(ana, partida.cantarSiguiente());
    partida.finalizar();
    afirmar(ana.obtenerPuntos() == 2, "Ana debe tener dos puntos");
    afirmar(luis.obtenerPuntos() == 0, "Luis debe tener cero puntos");
    afirmar(partida.obtenerGanadores().contains(ana), "Ana debe ganar por puntos");

    Jugador carlos = new Jugador("Carlos", tableroDesde(1));
    Jugador diana = new Jugador("Diana", tableroDesde(1));
    Partida empatada = new Partida(
      "empate-puntos",
      List.of(carlos, diana),
      new Griton(new Baraja(List.of(carta(4), carta(3), carta(2), carta(1))), new Random(1L)),
      TipoVictoria.TABLA_LLENA
    );
    empatada.iniciar();
    Carta primera = empatada.cantarSiguiente();
    empatada.marcar(carlos, primera);
    empatada.marcar(diana, primera);
    empatada.finalizar();
    afirmar(carlos.obtenerPuntos() == diana.obtenerPuntos(), "Los jugadores deben empatar");
    afirmar(empatada.obtenerGanadores().isEmpty(), "Un empate no debe tener ganador");
  }

  private static void verificarEncapsulamiento() {
    afirmarNoPublico(Tablero.class, "marcar", Carta.class);
    afirmarNoPublico(Tablero.class, "desmarcar", Carta.class);
    afirmarNoPublico(Tablero.class, "reiniciar");
    afirmarNoPublico(Casilla.class, "marcar");
    afirmarNoPublico(Casilla.class, "desmarcar");
    afirmarNoPublico(Casilla.class, "reiniciar");
    afirmarNoPublico(Jugador.class, "marcar", Carta.class);
  }

  private static void verificarDeterminismo() {
    Partida primera = Partida.crear(List.of("Ana", "Luis"), TipoVictoria.CHORRO, new Random(123L));
    Partida segunda = Partida.crear(List.of("Ana", "Luis"), TipoVictoria.CHORRO, new Random(123L));
    for (int i = 0; i < primera.obtenerJugadores().size(); i++) {
      List<Carta> tableroPrimera = primera.obtenerJugadores().get(i).obtenerTablero().obtenerCartas();
      List<Carta> tableroSegunda = segunda.obtenerJugadores().get(i).obtenerTablero().obtenerCartas();
      afirmar(tableroPrimera.equals(tableroSegunda), "Los tableros deben ser deterministas con la misma semilla");
    }
    afirmar(primera.obtenerCartasRestantes().equals(segunda.obtenerCartasRestantes()), "El mazo debe ser determinista con la misma semilla");
  }

  private static void marcar(Tablero tablero, int... ids) {
    for (int id : ids) {
      afirmar(marcarTablero(tablero, carta(id)), "La carta " + id + " debe pertenecer al tablero");
    }
  }

  private static Tablero tableroDesde(int inicio) {
    List<Carta> cartas = new ArrayList<>();
    for (int id = inicio; id < inicio + 16; id++) {
      cartas.add(carta(id));
    }
    return new Tablero(cartas);
  }

  private static Carta carta(int id) {
    return CatalogoCartas.obtenerPorId(id);
  }

  private static boolean marcarTablero(Tablero tablero, Carta carta) {
    return (Boolean) invocar(Tablero.class, tablero, "marcar", new Class<?>[]{Carta.class}, carta);
  }

  private static boolean desmarcarTablero(Tablero tablero, Carta carta) {
    return (Boolean) invocar(Tablero.class, tablero, "desmarcar", new Class<?>[]{Carta.class}, carta);
  }

  private static void reiniciarTablero(Tablero tablero) {
    invocar(Tablero.class, tablero, "reiniciar", new Class<?>[0]);
  }

  private static boolean marcarCasilla(Casilla casilla) {
    return (Boolean) invocar(Casilla.class, casilla, "marcar", new Class<?>[0]);
  }

  private static boolean desmarcarCasilla(Casilla casilla) {
    return (Boolean) invocar(Casilla.class, casilla, "desmarcar", new Class<?>[0]);
  }

  private static Object invocar(Class<?> tipo, Object objetivo, String nombre, Class<?>[] parametros, Object... argumentos) {
    try {
      Method metodo = tipo.getDeclaredMethod(nombre, parametros);
      metodo.setAccessible(true);
      return metodo.invoke(objetivo, argumentos);
    } catch (ReflectiveOperationException excepcion) {
      throw new AssertionError("No se pudo invocar " + tipo.getSimpleName() + "." + nombre, excepcion);
    }
  }

  private static void afirmarNoPublico(Class<?> tipo, String metodo, Class<?>... parametros) {
    try {
      int modificadores = tipo.getDeclaredMethod(metodo, parametros).getModifiers();
      afirmar(!Modifier.isPublic(modificadores), tipo.getSimpleName() + "." + metodo + " no debe ser público");
    } catch (NoSuchMethodException excepcion) {
      fallar("No existe el método " + tipo.getSimpleName() + "." + metodo);
    }
  }

  private static void afirmar(boolean condicion, String mensaje) {
    verificar();
    if (!condicion) {
      throw new AssertionError(mensaje);
    }
  }

  private static void verificar() {
    verificaciones++;
  }

  private static void fallar(String mensaje) {
    throw new AssertionError(mensaje);
  }
}
