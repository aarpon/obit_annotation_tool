@ECHO OFF

REM Define some variables
REM Make sure to pick a JDK version 6!

SET CURRENT_DIR=%CD%
SET TEMP_FOLDER=%CURRENT_DIR%\tmp
SET IN=%CURRENT_DIR%\..\..\bin\ch\ethz\scu\obit
SET TEMP_OUT=%TEMP_FOLDER%\ch\ethz\scu\obit
SET OUT=%CURRENT_DIR%\build
SET JDK_BIN="C:\Program Files\Java\jdk1.6.0_45\bin"

ECHO * * * Compiling code
%JDK_BIN%\javac -cp ..\..\lib\dss_client\loci_tools.jar:..\..\lib\dss_client\ij.jar ..\..\AnnotationTool\ch\ethz\scu\obit\readers\AbstractReader.java ..\..\AnnotationToolMicroscopy\ch\ethz\scu\obit\microscopy\readers\MicroscopyReader.java

ECHO * * * Moving compiled classes
MOVE /Y ..\..\AnnotationTool\ch\ethz\scu\obit\readers\AbstractReader.class %IN%\readers\
MOVE /Y ..\..\AnnotationToolMicroscopy\ch\ethz\scu\obit\microscopy\readers\MicroscopyReader.class %IN%\microscopy\readers\

ECHO * * * Packaging JAR archive for microscopy dropbox...

REM Start by creating the needed directories
MKDIR %OUT%
MKDIR %TEMP_OUT%\readers\
MKDIR %TEMP_OUT%\microscopy\readers\

REM Copy the classes
XCOPY /Y %IN%\readers\AbstractReader.class %TEMP_OUT%\readers\
XCOPY /Y %IN%\microscopy\readers\BaseBioFormatsReader.class %TEMP_OUT%\microscopy\readers\
XCOPY /Y %IN%\microscopy\readers\MicroscopyReader.class %TEMP_OUT%\microscopy\readers\

REM Package the jar file
CD %TEMP_FOLDER%
%JDK_BIN%\jar cvf MicroscopyReader.jar ch
CD %CURRENT_DIR%

ECHO * * * Moving JAR to the build folder
MOVE /Y %TEMP_FOLDER%\MicroscopyReader.jar %OUT%\

ECHO * * * Cleaning
RMDIR /S /Q %TEMP_FOLDER%

ECHO Done.
