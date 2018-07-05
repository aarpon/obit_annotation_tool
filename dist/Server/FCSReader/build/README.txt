Steps:
- Run createJAR.bat in the Windows command line to generate build/FCSReader.jar
- Upload the created JAR to the lib subfolder of the "retrieve_fcs_events" flow reporting plug-in.
- Delete openbis/servers/datastore_server/lib/autolink-retrieve_fcs_events-FCSReader.jar if it exists
- Restart the DSS
