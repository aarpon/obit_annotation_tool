@ECHO OFF

REM Core technology versions
SET FLOW_VERSION=3
SET MICROSCOPY_VERSION=3

REM OBIT LIBS PATH
SET OBIT_LIBS_PATH=%CD%\..

REM SET THE PATH OF THE CORE-TECHNOLOGIES
SET FLOW_PATH=%OBIT_LIBS_PATH%\..\..\..\..\server\obit_flow_core_technology
SET MICROSCOPY_PATH=%OBIT_LIBS_PATH%\..\..\..\..\server\obit_microscopy_core_technology

REM CHECK THAT THE FOLDERS ARE BUILT CORRECTLY
IF NOT EXIST "%FLOW_PATH%\" (
    echo Directory %FLOW_PATH% does not exist!
    exit /b  
)

IF NOT EXIST "%MICROSCOPYPATH%\" (
    echo Directory %MICROSCOPY_PATH% does not exist!
    exit /b 
)

REM ---------
REM FCSREADER
REM ---------

ECHO * * * Copying FCSReader to flow core technology...
SET FCSREADER_FLOW_PATH=%FLOW_PATH%\core-plugins\flow\%FLOW_VERSION%\dss\reporting-plugins\retrieve_fcs_events\lib\
IF NOT EXIST "%FCSREADER_FLOW_PATH%\" (
    echo Could not file FCSReader_*.jar in %FCSREADER_FLOW_PATH%!
    exit /b  
)
CD "%FCSREADER_FLOW_PATH%\"
DEL FCSReader_*.jar
COPY %OBIT_LIBS_PATH%\FCSReader\build\FCSReader_*.jar .

REM -------
REM LRCACHE
REM -------

ECHO * * * Copying LRCache to flow core technology...
SET LRCACHE_FLOW_PATH=%FLOW_PATH%\core-plugins\flow\%FLOW_VERSION%\dss\reporting-plugins\export_flow_datasets\lib\
IF NOT EXIST "%LRCACHE_FLOW_PATH%\" (
    echo Could not file LRCache_*.jar in %FCSREADER_FLOW_PATH%!
    exit /b  
)
CD "%LRCACHE_FLOW_PATH%\"
DEL LRCache_*.jar
COPY %OBIT_LIBS_PATH%\LRCache\build\LRCache_*.jar .

ECHO * * * Copying LRCache to microscopy core technology...
SET LRCACHE_MICROSCOPY_PATH=%MICROSCOPY_PATH%\core-plugins\microscopy\%MICROSCOPY_VERSION%\dss\reporting-plugins\export_microscopy_datasets\lib\
IF NOT EXIST "%LRCACHE_MICROSCOPY_PATH%\" ( 
    echo Could not file LRCache_*.jar in %FCSREADER_FLOW_PATH%!
    exit /b  
)
CD "%LRCACHE_MICROSCOPY_PATH%\"
DEL LRCache_*.jar
COPY %OBIT_LIBS_PATH%\LRCache\build\LRCache_*.jar .

REM -----------------
REM MICROSCOPY_READER
REM -----------------

ECHO * * * Copying MicroscopyReader to microscopy core technology...
SET MICROSCOPYREADER_MICROSCOPY_PATH=%MICROSCOPY_PATH%\core-plugins\microscopy\%MICROSCOPY_VERSION%\dss\drop-boxes\MicroscopyDropbox\lib\
IF NOT EXIST "%MICROSCOPYREADER_MICROSCOPY_PATH%\" (
    echo Could not file MicroscopyReader_*.jar in %FCSREADER_FLOW_PATH%!
    exit /b  
)
CD "%MICROSCOPYREADER_MICROSCOPY_PATH%\"
DEL MicroscopyReader_*.jar
COPY %OBIT_LIBS_PATH%\MicroscopyReader\build\MicroscopyReader_*.jar .

REM Return to the original path
CD %OBIT_LIBS_PATH%\CopyClientLibsToCoreTechnologies
