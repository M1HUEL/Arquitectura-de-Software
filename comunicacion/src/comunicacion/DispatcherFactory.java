package comunicacion;

public final class DispatcherFactory {

  private DispatcherFactory() {
  }

  public static Dispatcher crear() {
    return new Dispatcher(new ClienteTCP());
  }

  public static Dispatcher crear(ObservadorDispatcher... observadores) {
    return new Dispatcher(observadores);
  }
}
