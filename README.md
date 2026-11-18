# Lotería

Juego de lotería con arquitectura cliente/servidor sobre un broker de mensajes.
El broker, el dominio y cada cliente se arrancan por separado desde el
ensamblador, que es el punto de entrada único del sistema.

## Requisitos

- Java 26 o superior (probado con JDK 27).
- Windows para usar `loteria.bat`.

## Arranque rápido

Abrir cinco consolas en la raíz del proyecto y ejecutar, en este orden.
En PowerShell hay que anteponer `.\` al nombre del lanzador; en `cmd.exe`
funciona igual con o sin `.\`.

1. Broker:

   ```bat
   .\loteria.bat broker 7000 7001 7002 7003 7004
   ```

2. Dominio (espera 30 segundos por defecto para que se conecten los clientes):

   ```bat
   .\loteria.bat dominio 7001 7000
   ```

3. Clientes (uno por consola, con el nombre y el puerto elegidos):

   ```bat
   .\loteria.bat cliente Miguel 7002
   .\loteria.bat cliente Ana 7003
   .\loteria.bat cliente Luis 7004
   ```

El dominio espera el tiempo indicado y luego inicia la partida con todos los
clientes que se hayan conectado durante la espera. No hay cantidad fija de
jugadores ni oponentes automáticos en este modo. Si la espera termina sin
clientes, el dominio vuelve a esperar.
