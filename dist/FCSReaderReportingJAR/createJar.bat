@ECHO OFF

REM Override the default java compiler and jar command if needed
SET JAVAC=javac
SET JAR=jar

REM Get current directory
SET CURRENT_DIR=%CD%

REM Delete the existing classes if they existing
IF exist %CURRENT_DIR%\build\ch\ ( RMDIR /S /Q %CURRENT_DIR%\build\ch )

REM Build path
SET BUILD_PATH=%CURRENT_DIR%\build

ECHO Generating FCSReader.jar to use in the flow core technology generate_fcs_plots reporting plug-in...

ECHO Compiling classes...
%JAVAC% -source 1.7 -target 1.7 @"%CURRENT_DIR%.\files.txt" -d %BUILD_PATH%

ECHO Packaging JAR archive...

REM Change to the build directory
cd %BUILD_PATH%

REM Delete current archive
del FCSReader.jar

REM Create new archive
%JAR% cvf FCSReader.jar ch

REM Delete the generated classes
IF exist %CURRENT_DIR%\build\ch\ ( RMDIR /S /Q %CURRENT_DIR%\build\ch )

REM Change back to original directory
cd %CURRENT_DIR% 

ECHO Generated JAR archive is .\build\FCSReader.jar
