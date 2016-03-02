@ECHO OFF

REM OBIT LIBS PATH
SET OBIT_LIBS_PATH=%CD%\..

REM SET THE PATH OF THE CORE-TECHNOLOGIES
SET FLOW_PATH=%OBIT_LIBS_PATH%\..\..\..\obit_flow_core_technology
SET MICROSCOPY_PATH=%OBIT_LIBS_PATH%\..\..\..\obit_microscopy_core_technology
    
REM ---------
REM FCSREADER
REM ---------

ECHO * * * Copying FCSReader to flow core technology...
CD %FLOW_PATH%\core-plugins\flow\1\dss\reporting-plugins\retrieve_fcs_events\lib\
DEL FCSReader_*.jar
COPY %OBIT_LIBS_PATH%\FCSReader\build\FCSReader_*.jar .

REM -------
REM LRCACHE
REM -------

ECHO * * * Copying LRCache to flow core technology...
CD %FLOW_PATH%\core-plugins\flow\1\dss\reporting-plugins\export_bdfacsdiva_datasets\lib\
DEL LRCache_*.jar
COPY %OBIT_LIBS_PATH%\LRCache\build\LRCache_*.jar .

ECHO * * * Copying LRCache to microscopy core technology...
CD %MICROSCOPY_PATH%\core-plugins\microscopy\1\dss\reporting-plugins\export_microscopy_datasets\lib\
DEL LRCache_*.jar
COPY %OBIT_LIBS_PATH%\LRCache\build\LRCache_*.jar .

REM -----------------
REM MICROSCOPY_READER
REM -----------------

ECHO * * * Copying MicroscopyReader to microscopy core technology...
CD %MICROSCOPY_PATH%\core-plugins\microscopy\1\dss\drop-boxes\MicroscopyDropbox\lib\
DEL MicroscopyReader_*.jar
COPY %OBIT_LIBS_PATH%\MicroscopyReader\build\MicroscopyReader_*.jar .

CD %OBIT_LIBS_PATH%\CopyClientLibsToCoreTechnologies
