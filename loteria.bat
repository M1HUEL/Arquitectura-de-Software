@echo off
setlocal

rem Lanzador del proyecto. Uso:
rem   loteria.bat broker [puertoBroker] [puertoDominio] [puertoCliente...]
rem   loteria.bat dominio [puertoDominio] [puertoBroker] [segundosEspera] [intervaloMs]
rem   loteria.bat cliente <nombre> <puertoCliente> [puertoBroker] [hostBroker]

set "BASE=%~dp0"
set "CP=%BASE%ensamblador\ensamblador.jar;%BASE%ensamblador\comunicacion-api.jar;%BASE%ensamblador\comunicacion.jar;%BASE%ensamblador\serializador.jar;%BASE%ensamblador\comun.jar;%BASE%ensamblador\broker.jar;%BASE%ensamblador\dominio.jar;%BASE%ensamblador\juego.jar"

if defined JAVA_HOME (
  set "JAVA=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA=java"
)

"%JAVA%" -cp "%CP%" ensamblador.Main %*
endlocal
