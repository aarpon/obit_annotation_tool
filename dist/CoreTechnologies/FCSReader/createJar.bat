@ECHO OFF

REM Override the default java compiler and jar command if needed
SET JAVAC=javac
SET JAR=jar

REM Get current directory
SET CURRENT_DIR=%CD%

REM Delete the existing classes if already existing
IF exist %CURRENT_DIR%\build\ch\ ( RMDIR /S /Q %CURRENT_DIR%\build\ch )

REM Build path
SET BUILD_PATH=%CURRENT_DIR%\build

ECHO Generating FCSReader jar to use in the flow core technology retrieve_fcs_events reporting plug-in...

ECHO Compiling classes...
%JAVAC% -source 1.8 -target 1.8 @"%CURRENT_DIR%.\files.txt" -d %BUILD_PATH%

ECHO Packaging JAR archive...

REM Change to the build directory
cd %BUILD_PATH%

REM Delete current archive
del FCSReader*.jar

REM Archive name
FOR /f "tokens=2 delims==" %%a IN ('wmic OS Get localdatetime /value') DO SET "dt=%%a"
SET "YY=%dt:~2,2%" & SET "YYYY=%dt:~0,4%" & SET "MM=%dt:~4,2%" & SET "DD=%dt:~6,2%"
SET "HH=%dt:~8,2%" & SET "Min=%dt:~10,2%" & SET "Sec=%dt:~12,2%"
SET "fullstamp=%YYYY%%MM%%DD%%HH%%Min%%Sec%"
SET ARCHIVENAME=FCSReader_%fullstamp%.jar

REM Create new archive
%JAR% cvf %ARCHIVENAME% ch

REM Delete the generated classes
IF exist %CURRENT_DIR%\build\ch\ ( RMDIR /S /Q %CURRENT_DIR%\build\ch )

REM Change back to original directory
cd %CURRENT_DIR% 

ECHO Generated JAR archive is .\build\%ARCHIVENAME%