Steps:
- Run createJAR.bat in the Windows command line to generate build/MicroscopyReader_{datetime}.jar
- Upload the created JAR to the lib subfolder of the "MicroscopyDropbox".
- Delete openbis/servers/datastore_server/lib/autolink-MicroscopyDropbox-MicroscopyReader_{datetime}.jar if it exists
- Restart the DSS
