ECHO OFF

REM Change to working directory
CD C:\Users\pontia\Documents\openBISImporterToolset\eclipse\scu_annotation_tool\dist

REM Create needed directories
IF NOT EXIST .\win32 MKDIR .\win32
IF NOT EXIST .\win32\lib MKDIR .\win32\lib
IF NOT EXIST .\win32\log MKDIR .\win32\log

REM Copy all jar packages (common to both tools)
COPY /Y .\AnnotationToolExe\*.jar .\win32\lib\

REM Copy necessary files to generate AnnotationTool.exe for 32-bit Windows
COPY .\AnnotationToolIni\AnnotationTool32bit.ini .\win32\AnnotationTool.ini
COPY .\AnnotationToolIni\icon.ico .\win32\
COPY .\winrun4j\WinRun4J.exe .\win32\AnnotationTool.exe

REM Generate 32-bit Windows executable
.\winrun4j\RCEDIT.exe /I .\win32\AnnotationTool.exe .\win32\icon.ico
.\winrun4j\RCEDIT.exe /N .\win32\AnnotationTool.exe .\win32\AnnotationTool.ini

REM Copy necessary files to generate AnnotationToolAdmin.exe for 32-bit Windows
COPY .\AnnotationToolAdminIni\AnnotationToolAdmin32bit.ini .\win32\AnnotationToolAdmin.ini
COPY .\AnnotationToolAdminIni\icon_admin.ico .\win32\
COPY .\winrun4j\WinRun4J.exe .\win32\AnnotationToolAdmin.exe

REM Generate 32-bit Windows executable
.\winrun4j\RCEDIT.exe /I .\win32\AnnotationToolAdmin.exe .\win32\icon_admin.ico
.\winrun4j\RCEDIT.exe /N .\win32\AnnotationToolAdmin.exe .\win32\AnnotationToolAdmin.ini

REM Delete the icon and ini files since they were embedded in the executables
DEL /Q .\win32\*.ico
DEL /Q .\win32\*.ini

REM Clean up
DEL /Q .\AnnotationToolExe\*.exe
DEL /Q .\AnnotationToolExe\*.jar
DEL /Q .\AnnotationToolAdminExe\*.exe
DEL /Q .\AnnotationToolAdminExe\*.jar
