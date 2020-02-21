ECHO OFF

REM Change to working directory
SET WDIR=%~dp0
CD %WDIR%

REM Create needed directories
IF NOT EXIST .\win32 MKDIR .\win32
IF NOT EXIST .\win32\obit_annotation_tool MKDIR .\win32\obit_annotation_tool
IF NOT EXIST .\win32\obit_annotation_tool\lib MKDIR .\win32\obit_annotation_tool\lib
IF NOT EXIST .\win32\obit_annotation_tool\log MKDIR .\win32\obit_annotation_tool\log

REM Copy all jar packages (common to all tools)
COPY /Y .\AnnotationToolExe\*.jar .\win32\obit_annotation_tool\lib\

REM Copy necessary files to generate AnnotationTool.exe for 32-bit Windows
COPY .\AnnotationToolIni\AnnotationTool64bit.ini .\win32\obit_annotation_tool\AnnotationTool.ini
COPY .\AnnotationToolIni\icon.ico .\win32\obit_annotation_tool\
COPY ..\winrun4j\WinRun4J.exe .\win32\obit_annotation_tool\AnnotationTool.exe

REM Generate 32-bit Windows executable and embed icon and ini file
..\winrun4j\RCEDIT.exe /I .\win32\obit_annotation_tool\AnnotationTool.exe .\win32\obit_annotation_tool\icon.ico

REM Copy necessary files to generate AnnotationToolAdmin.exe for 32-bit Windows
COPY .\AnnotationToolAdminIni\AnnotationToolAdmin64bit.ini .\win32\obit_annotation_tool\AnnotationToolAdmin.ini
COPY .\AnnotationToolAdminIni\icon_admin.ico .\win32\obit_annotation_tool\
COPY ..\winrun4j\WinRun4J.exe .\win32\obit_annotation_tool\AnnotationToolAdmin.exe

REM Generate 32-bit Windows executable
..\winrun4j\RCEDIT.exe /I .\win32\obit_annotation_tool\AnnotationToolAdmin.exe .\win32\obit_annotation_tool\icon_admin.ico

REM Copy necessary files to generate AnnotationToolUpdater.exe for 32-bit Windows
COPY .\AnnotationToolUpdaterIni\AnnotationToolUpdater64bit.ini .\win32\obit_annotation_tool\AnnotationToolUpdater.ini
COPY .\AnnotationToolUpdaterIni\icon_updater.ico .\win32\obit_annotation_tool\
COPY ..\winrun4j\WinRun4J.exe .\win32\obit_annotation_tool\AnnotationToolUpdater.exe

REM Generate 32-bit Windows executable
..\winrun4j\RCEDIT.exe /I .\win32\obit_annotation_tool\AnnotationToolUpdater.exe .\win32\obit_annotation_tool\icon_updater.ico

REM Copy the manifest to prevent Windows from asking the admin password
REM (since the file name contains 'update')
COPY .\AnnotationToolUpdaterIni\AnnotationToolUpdater.exe.manifest .\win32\obit_annotation_tool\

REM To make sure that the manifest file is considered, update the last
REM modification time of the executable.
@COPY /B .\win32\obit_annotation_tool\AnnotationToolUpdater.exe+,, .\win32\obit_annotation_tool\AnnotationToolUpdater.exe

REM Delete the icon and ini files since they were embedded in the executables
DEL /Q .\win32\obit_annotation_tool\*.ico

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
