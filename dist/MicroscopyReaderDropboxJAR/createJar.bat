@ECHO OFF
REM Make sure to compile with compliance level 1.6

REM Define some variables
REM Eclipse build folder
REM SET BIN=bin
REM IntelliJ build folder
SET BIN=out\production\obit_annotation_tool
SET CURRENT_DIR=%CD%
SET TEMP_FOLDER=%CURRENT_DIR%\tmp
SET IN=%CURRENT_DIR%\..\..\%BIN%\ch\ethz\scu\obit
SET TEMP_OUT=%TEMP_FOLDER%\ch\ethz\scu\obit
SET OUT=%CURRENT_DIR%\build

ECHO Packaging JAR archive for microscopy dropbox...

REM Start by creating the needed directories
MKDIR %OUT%
MKDIR %TEMP_OUT%\readers\
MKDIR %TEMP_OUT%\microscopy\readers\

REM Copy the classes
XCOPY %IN%\readers\AbstractReader.class %TEMP_OUT%\readers\
XCOPY %IN%\microscopy\readers\MicroscopyReader.class %TEMP_OUT%\microscopy\readers\

REM Package the jar file
CD %TEMP_FOLDER%
jar cvf MicroscopyReader.jar ch
CD %CURRENT_DIR%

REM Move it to the build folder
MOVE /Y %TEMP_FOLDER%\MicroscopyReader.jar %OUT%\

REM Clean
RMDIR /S /Q %TEMP_FOLDER%

ECHO Done.