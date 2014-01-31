ECHO OFF

REM Change to working directory
CD C:\Users\pontia\Documents\openBISImporterToolset\eclipse\scu_annotation_tool\dist

REM Create needed directories
IF NOT EXIST .\win64 MKDIR .\win64
IF NOT EXIST .\win64\lib MKDIR .\win64\lib
IF NOT EXIST .\win64\log MKDIR .\win64\log

REM Copy all jar packages (common to both tools)
COPY /Y .\AnnotationToolExe\*.jar .\win64\lib\

REM Copy necessary files to generate AnnotationTool.exe for 64-bit Windows
COPY .\AnnotationToolIni\AnnotationTool64bit.ini .\win64\AnnotationTool.ini
COPY .\AnnotationToolIni\icon.ico .\win64\
COPY .\winrun4j\WinRun4J64.exe .\win64\AnnotationTool.exe

REM Generate 64-bit Windows executable
.\winrun4j\RCEDIT64.exe /I .\win64\AnnotationTool.exe .\win64\icon.ico

REM Copy necessary files to generate AnnotationToolAdmin.exe for 64-bit Windows
COPY .\AnnotationToolAdminIni\AnnotationToolAdmin64bit.ini .\win64\AnnotationToolAdmin.ini
COPY .\AnnotationToolAdminIni\icon_admin.ico .\win64\
COPY .\winrun4j\WinRun4J64.exe .\win64\AnnotationToolAdmin.exe

REM Generate 64-bit Windows executable
.\winrun4j\RCEDIT64.exe /I .\win64\AnnotationToolAdmin.exe .\win64\icon_admin.ico

REM Delete the icon files since they were embedded in the executables
DEL .\win64\*.ico