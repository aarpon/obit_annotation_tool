ECHO OFF

REM Change to working directory
SET WDIR=%~dp0
CD %WDIR%

REM Create needed directories
IF NOT EXIST .\win32 MKDIR .\win32
IF NOT EXIST .\win32\lib MKDIR .\win32\lib
IF NOT EXIST .\win32\log MKDIR .\win32\log

REM Copy all jar packages (common to all tools)
COPY /Y .\AnnotationToolExe\*.jar .\win32\lib\

REM Copy necessary files to generate AnnotationTool.exe for 32-bit Windows
COPY .\AnnotationToolIni\AnnotationTool32bit.ini .\win32\AnnotationTool.ini
COPY .\AnnotationToolIni\icon.ico .\win32\
COPY .\winrun4j\WinRun4J.exe .\win32\AnnotationTool.exe

REM Generate 32-bit Windows executable
.\winrun4j\RCEDIT.exe /I .\win32\AnnotationTool.exe .\win32\icon.ico

REM Copy necessary files to generate AnnotationToolAdmin.exe for 32-bit Windows
COPY .\AnnotationToolAdminIni\AnnotationToolAdmin32bit.ini .\win32\AnnotationToolAdmin.ini
COPY .\AnnotationToolAdminIni\icon_admin.ico .\win32\
COPY .\winrun4j\WinRun4J.exe .\win32\AnnotationToolAdmin.exe

REM Generate 32-bit Windows executable
.\winrun4j\RCEDIT.exe /I .\win32\AnnotationToolAdmin.exe .\win32\icon_admin.ico

REM Copy necessary files to generate AnnotationToolUpdater.exe for 32-bit Windows
COPY .\AnnotationToolUpdaterIni\AnnotationToolUpdater32bit.ini .\win32\AnnotationToolUpdater.ini
COPY .\AnnotationToolUpdaterIni\icon_updater.ico .\win32\
COPY .\winrun4j\WinRun4J.exe .\win32\AnnotationToolUpdater.exe

REM Generate 32-bit Windows executable
.\winrun4j\RCEDIT.exe /I .\win32\AnnotationToolUpdater.exe .\win32\icon_updater.ico

REM Delete the icon and ini files since they were embedded in the executables
DEL /Q .\win32\*.ico

REM Clean up
DEL /Q .\AnnotationToolExe\*.exe
DEL /Q .\AnnotationToolExe\*.jar
DEL /Q .\AnnotationToolExe\*.ini
DEL /Q .\AnnotationToolAdminExe\*.exe
DEL /Q .\AnnotationToolAdminExe\*.jar
DEL /Q .\AnnotationToolAdminExe\*.ini
DEL /Q .\AnnotationToolUpdaterExe\*.exe
DEL /Q .\AnnotationToolUpdaterExe\*.jar
DEL /Q .\AnnotationToolUpdaterExe\*.ini
