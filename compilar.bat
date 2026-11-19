@echo off
setlocal EnableDelayedExpansion
set "BASE=%~dp0"
set "BUILD=%TEMP%\loteria-build"

if defined JAVA_HOME (
  set "JAVAC=%JAVA_HOME%\bin\javac.exe"
  set "JAR=%JAVA_HOME%\bin\jar.exe"
) else (
  for /f "tokens=3,*" %%H in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /c:"java.home ="') do set "JAVAHOME=%%H %%I"
  set "JAVAC=!JAVAHOME!\bin\javac.exe"
  set "JAR=!JAVAHOME!\bin\jar.exe"
)

if exist "%BUILD%" rmdir /s /q "%BUILD%"
mkdir "%BUILD%\clases"
if errorlevel 1 exit /b 1

set "LISTA=%BUILD%\fuentes.txt"
type nul > "%LISTA%"
for %%P in (comun comunicacion-api serializador comunicacion broker dominio juego ensamblador) do (
  for /f "delims=" %%F in ('dir /s /b "%BASE%%%P\src\*.java"') do (
    set "RUTA=%%F"
    echo "!RUTA:\=/!" >> "%LISTA%"
  )
)

"%JAVAC%" --release 26 -encoding UTF-8 -d "%BUILD%\clases" "@%LISTA%"
if errorlevel 1 exit /b 1

call :hacerJar comun comun comun.jar
call :hacerJar comunicacion-api comunicacion/api comunicacion-api.jar
call :hacerJar comunicacion comunicacion comunicacion.jar
call :hacerJar serializador serializador serializador.jar
call :hacerJar broker broker broker.jar
call :hacerJar dominio dominio dominio.jar
call :hacerJar juego juego juego.jar
call :hacerJar ensamblador ensamblador ensamblador.jar
if not exist "%BUILD%\ensamblador.jar" exit /b 1

for %%D in (comunicacion-api\dist comunicacion\dist\lib serializador\dist) do mkdir "%BASE%%%D" 2>nul

for %%D in (broker dominio juego ensamblador) do copy /y "%BUILD%\comun.jar" "%BASE%%%D\comun.jar" >nul
for %%D in (comunicacion-api\dist comunicacion comunicacion\dist\lib broker dominio juego ensamblador) do copy /y "%BUILD%\comunicacion-api.jar" "%BASE%%%D\comunicacion-api.jar" >nul
for %%D in (comunicacion\dist broker dominio juego ensamblador) do copy /y "%BUILD%\comunicacion.jar" "%BASE%%%D\comunicacion.jar" >nul
for %%D in (serializador\dist broker dominio juego ensamblador) do copy /y "%BUILD%\serializador.jar" "%BASE%%%D\serializador.jar" >nul
for %%D in (broker ensamblador) do copy /y "%BUILD%\broker.jar" "%BASE%%%D\broker.jar" >nul
for %%D in (dominio ensamblador) do copy /y "%BUILD%\dominio.jar" "%BASE%%%D\dominio.jar" >nul
for %%D in (juego ensamblador) do copy /y "%BUILD%\juego.jar" "%BASE%%%D\juego.jar" >nul
copy /y "%BUILD%\ensamblador.jar" "%BASE%ensamblador\ensamblador.jar" >nul

rmdir /s /q "%BUILD%"
echo Jars compilados y copiados.
endlocal
exit /b 0

:hacerJar
set "PROYECTO=%~1"
set "PAQUETE=%~2"
set "NOMBRE=%~3"
"%JAR%" --create --file "%BUILD%\%NOMBRE%" --manifest "%BASE%%PROYECTO%\manifest.mf" -C "%BUILD%\clases" %PAQUETE%
exit /b %ERRORLEVEL%
