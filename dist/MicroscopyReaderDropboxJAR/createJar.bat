@ECHO OFF

REM Override the default java compiler and jar command if needed
SET JAVAC=javac
SET JAR=jar

REM Get current directory
SET CURRENT_DIR=%CD%

REM Delete the existing classes if they existing
IF exist %CURRENT_DIR%\build\ch\ ( RMDIR /S /Q %CURRENT_DIR%\build\ch )

REM Classpath
SET CLASSPATH=..\..\lib\bioformats_package-5.0.8.jar

REM Build path
SET BUILD_PATH=%CURRENT_DIR%\build

ECHO Generating MicroscopyReader.jar to use in the microscopy core technology dropbox

ECHO Compiling classes...
%JAVAC% -cp %CLASSPATH% -source 1.7 -target 1.7 @"%CURRENT_DIR%.\files.txt" -d %BUILD_PATH%

ECHO Packaging JAR archive...

REM Change to the build directory
cd %BUILD_PATH%

REM Delete current archive
del MicroscopyReader.jar

REM Create new archive
%JAR% cvf MicroscopyReader.jar ch

REM Delete the generated classes
IF exist %CURRENT_DIR%\build\ch\ ( RMDIR /S /Q %CURRENT_DIR%\build\ch )

REM Change back to original directory
cd %CURRENT_DIR% 

ECHO Generated JAR archive is .\build\MicroscopyReader.jar

