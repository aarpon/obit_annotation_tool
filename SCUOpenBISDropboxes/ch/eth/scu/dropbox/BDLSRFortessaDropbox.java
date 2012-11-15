package ch.eth.scu.dropbox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v2.AbstractJavaDataSetRegistrationDropboxV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;

/**
 * Dropbox to register BD LSR Fortessa datasets into openBIS
 * @author Aaron Ponti
 *
 */
public class BDLSRFortessaDropbox extends AbstractJavaDataSetRegistrationDropboxV2 {

	/**
	 * A IDataSetRegistrationTransactionV2 transaction object
	 */
	private IDataSetRegistrationTransactionV2 transaction;
	
	private BufferedWriter out;
	
	final String LOGFILE = 
			"/local0/openbis/sprint/servers/core-plugins/scu/1/dss/drop-boxes/bdlsrfortessa-dropbox/logs/BDLSRLog.txt";

	/**
	 * Parsed XML document
	 */
	private Document doc = null;
	
	@Override
    public void process(IDataSetRegistrationTransactionV2 transaction)
    {
		// Store the transaction reference
		this.transaction = transaction;
		
		// Get the name of the incoming data set folder
		File incoming = transaction.getIncoming();
		
		// TODO DEBUG INFO TO REMOVE
		logToFile("Incoming: " + incoming.getAbsolutePath());
		
		// Incoming is the 'user' folder. Experiments are sub-folders within 
		// incoming. So, we scan incoming for sub-folders and return them.
		File[] subFolders = scanIncomingForSubFolders(incoming);
		
		// Now we process (and ultimately register) all subfolders
		for (File subfolder : subFolders) {
			logToFile("Processing: " + subfolder.getName());
			processSubFolder(subfolder);
		}
    }

	/**
	 * Process the experiment contained in the given subfolder. The processing
	 * is a multi-step that results in the registration in openBIS. 
	 * @param subfolder Full path name of the subfolder to process.
	 */
	private void processSubFolder(File subfolder) {
		
		// Find the related <>_properties.six file
		File properties = getPropertiesFileName(subfolder);
		if (properties == null) {
			// TODO Handle the case the properties file could not be generated!
			// This happens if incoming is a file, or if an exception is thrown
			logToFile("ERROR: Could not find properties file for subfolder " +
					subfolder.getName());
			return;
		}
		
		// Make sure the properties file exists
		if (!properties.exists()) {
			// TODO Handle the case where the properties file does not exist!
			logToFile("ERROR: The properties file for subfolder " +
					subfolder.getName() + " does not exist!");
			return;
		}

		// Parse the properties XML file
		readPropertiesFile(properties);
		if (doc == null) {
			// TODO Handle the case where the properties file cannot be parsed!
			logToFile("ERROR: Could not parse properties file for subfolder " +
					subfolder.getName());
			return;
		}
		
		// Use the information in the XML file to register the data set
		if (!register()) {
			logToFile("ERROR: Could not register data in subfolder " +
					subfolder.getName());
			// TODO Handle failed processing
		}
		
	}	

	/**
	 * Return the properties file name for the given subfolder
	 * @return the <>_properties.six full file name
	 */
	private File getPropertiesFileName(File subfolder) {
		
		// Incoming must be a directory
		if (! subfolder.isDirectory()) {
			logToFile("ERROR: Subfolder " + subfolder + " is not a directory");
			return null;
		}
		
		File[] propertyFiles = subfolder.listFiles(
				new FilenameFilter() {
					public boolean accept(File file, String name) {
						return name.toLowerCase().endsWith("_properties.six");
					}
				});
		if (propertyFiles.length != 1) {
			logToFile("ERROR: Exactly ONE _properties.six file must be in " +
					"an experiment subfolder!" );
			return null;
		}

		// TODO DEBUG INFO TO REMOVE
		logToFile("Properties.six file: " + propertyFiles[0]);

		return propertyFiles[0];
	}

	/**
	 * Read the properties XML file and returns a Document
	 * @param properties <incoming>_properties.six file 
	 * @return parsed XML Document or null if parsing failed
	 */
	private void readPropertiesFile(File properties) {
		
		DocumentBuilder parser;
		
		// Instantiate the factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true); 	// Ignore comments 
		factory.setCoalescing(true); 		// Convert CDATA to Text nodes 
		factory.setNamespaceAware(false); 	// No name spaces
		factory.setValidating(false);		// Don't validate DTD

		// Create and store a parser
		try {
			parser = factory.newDocumentBuilder();
		} catch ( ParserConfigurationException pce) {
			parser = null;
		}

		// Parse the file
		doc = null;
		if (parser != null ) {
			try {
				doc = parser.parse(properties);
				//doc.getDocumentElement().normalize();
			} catch (SAXException se) {
				doc = null;
			} catch (IOException ioe) {
				doc = null;
			}	
		}

	}
	
	/**
	 * Register the data set using the parsed XML Document 
	 * @return true if registration was successful, false otherwise
	 */
	private boolean register() {
		
		// Do we have a Document?
		if (doc == null) {
			return false;
		}
		
		logToFile("Registering...");
		
		// Get the root node
		Node rootNode = doc.getDocumentElement();

		// How iterate over the children
		NodeList children = rootNode.getChildNodes();
		for ( int i = 0; i < children.getLength(); i++ ) {
			
			// Get experiment
			Node e = children.item(i);
			
			// Check that it is indeed an Experiment
			if (! e.getNodeName().equals("Experiment")) {
				return false;
			}
			
			// Process the experiment
			IExperimentUpdatable openBISExperiment = processExperiment(e);

			if (openBISExperiment != null ) {
				logToFile("Experiment processed successfully.");
			} else {
				logToFile("ERROR: Failure processing Experiment!");
			}

			logToFile("Iterating over Experiment children.");

			// Iterate over current Experiment children
			NodeList expChildren = e.getChildNodes();
			for ( int j = 0; j < expChildren.getLength(); j++ ) {
			
				// Get child: it can be a Specimen or a Tray
				Node c = expChildren.item(j);
				String type = c.getNodeName();
				
				if (type.equals("Specimen")) {
					
					logToFile("Processing Specimen.");
					
					// Process the Specimen
					ISample openBISSpecimen = processSpecimen(c, 
							openBISExperiment);
					
					// Iterate over current Specimen children
					NodeList specChildren = c.getChildNodes();
					for ( int m = 0; m < specChildren.getLength(); m++ ) {

						// Get child: it must be a Specimen
						Node s = specChildren.item(m);
						
						// Check that it is indeed a Specimen
						if (! s.getNodeName().equals("Tube")) {
							return false;
						}

						// Process the Specimen
						// (we don't need to get the Tube object back)
						processTube(s, openBISSpecimen);
						
					}
					
				} else if (type.equals("Tray")) {
					
					logToFile("Processing Tray.");
					
					// Process a Tray
					ISample openBISTray = processTray(c, openBISExperiment);
					
					// Iterate over current Tray children
					NodeList trayChildren = c.getChildNodes();
					for ( int k = 0; k < trayChildren.getLength(); k++ ) {
					
						// Get child: it must be a Specimen
						Node s = trayChildren.item(j);
						
						// Check that it is indeed a Specimen
						if (! s.getNodeName().equals("Specimen")) {
							return false;
						}

						// Process the Specimen
						ISample openBISSpecimen =
								processSpecimen(c, openBISTray);
						
						// Iterate over current Specimen children
						NodeList specChildren = s.getChildNodes();
						for ( int l = 0; l < specChildren.getLength(); l++ ) {

							// Get child: it must be a Specimen
							Node t = specChildren.item(j);
							
							// Check that it is indeed a Tube
							if (! t.getNodeName().equals("Tube")) {
								return false;
							}
							
							// Process the Tube
							// (we don't need to get the Tube object back)
							processTube(t, openBISSpecimen);

						}

					}
					
				} else {
					return false;
				}

				
			}
			
		}
		
		return true;
	}
	
	/**
	 * Register an IExperimentUpdatable based on the Experiment XML node
	 * @param e An XML node corresponding to an Experiment
	 * @return IExperimentUpdatable experiment, or null
	 */
	private IExperimentUpdatable processExperiment(Node e) {
		
		// Initialize the experiment
		IExperimentUpdatable openBISExperiment;
		
		// Get Experiment attributes
		NamedNodeMap attr = e.getAttributes();
		String name = 
				attr.getNamedItem("name").getNodeValue();
		String openBISIdentifier =
				attr.getNamedItem("openBISIdentifier").getNodeValue();
		
		logToFile("Get/create experiment: " + openBISIdentifier );
		
		// TODO Store the Experiment type in the _properties.six file
		String expType = "UNKNOWN";
		
		// Get the Experiment
		openBISExperiment = 
				transaction.getExperimentForUpdate(openBISIdentifier);
		if (openBISExperiment == null) {
			// Create the Experiment
			openBISExperiment = transaction.createNewExperiment(
					openBISIdentifier, expType);
		}
		
		// TODO Set all required properties
		openBISExperiment.setPropertyValue("DESCRIPTION", name);
	
		return openBISExperiment;
	}

	/**
	 * Register a Tray (Plate) based on the Tray XML node
	 * and an IExperimentUpdatable object
	 * @param tray An XML node corresponding to a Tray (Plate)
	 * @param exp  An IExperimentUpdatable object
	 * @return ISample sample, or null
	 */
	private ISample processTray(Node tray, IExperimentUpdatable exp) {
		
		// Initialize the Tray
		ISample openBISTray;
		
		// Get Tray attributes
		NamedNodeMap attr = tray.getAttributes();
		
		// Get the name
		String name = 
				attr.getNamedItem("name").getNodeValue();
		
		logToFile("Processing Tray " + name);
		
		// Make an openBISCode from the name
		String openBISCode = name.replaceAll(" ", "_").toUpperCase();
		
		// Build the openBIS Identifier
		String openBISIdentifier =
				exp.getExperimentIdentifier() + "/" + openBISCode;
		
		logToFile("Tray has identifier: " + openBISIdentifier);
		
		// TODO Map tray_type from the Fortessa software to an openBIS 
		// sample type
		String tray_type;
		Node trayTypeNode = attr.getNamedItem("tray_type");
		if (trayTypeNode == null ) {
			tray_type = "UNKNOWN";
		} else {
			tray_type = trayTypeNode.getNodeValue();
		}
		String trayGeometry = "96 Wells, 8x12";
		
		logToFile("Tray has type: " + tray_type);
		
		// TODO This should be mapped from the tray_type
		String openBISSampleType = "PLATE";

		logToFile("Creating new Tray with identifier: " + openBISIdentifier + 
				" and sample type: " + openBISSampleType);
		
		// The sample should NOT exist!
		// TODO make sure to handle properly the case where the sample already
		// exists!
		openBISTray = transaction.getSampleForUpdate(openBISIdentifier);
		if(openBISTray == null) {
			openBISTray = transaction.createNewSample(
					openBISIdentifier, openBISSampleType);
		}
		
		logToFile("Created/Obtained new Tray with identifier: " + 
				openBISIdentifier + " and sample type: " + openBISSampleType);

		// TODO Set all required properties
		openBISTray.setPropertyValue("DESCRIPTION", name);
		openBISTray.setPropertyValue("$PLATE_GEOMETRY", trayGeometry);
		
		return openBISTray;
	}

	/**
	 * Register a Specimen (as a child of an Experiment) based on the Specimen 
	 * XML node and an IExperimentUpdatable object
	 * @param specimen An XML node corresponding to a Specimen
	 * @param exp  An IExperimentUpdatable object
	 * @return ISample sample, or null
	 */
	private ISample processSpecimen(Node specimen, IExperimentUpdatable exp) {
	
		// Initialize the Specimen
		ISample openBISSpecimen;

		// Get Tray attributes
		NamedNodeMap attr = specimen.getAttributes();
		
		// Get the name
		String name = 
				attr.getNamedItem("name").getNodeValue();
		
		// Make an openBISCode from the name
		String openBISCode = name.replaceAll(" ", "_").toUpperCase();
		
		// Build the openBIS Identifier
		String openBISIdentifier =
				exp.getExperimentIdentifier() + "/" + openBISCode;

		// TODO This should be stored in the _properties.six file
		String openBISSpecimenType = "UNKNOWN";
		
		// The sample should NOT exist!
		// TODO make sure to handle properly the case where the sample already
		// exists!
		openBISSpecimen = transaction.getSampleForUpdate(openBISIdentifier);
		if(openBISSpecimen == null) {
			openBISSpecimen = transaction.createNewSample(
					openBISIdentifier, openBISSpecimenType);
		}
		
		// TODO Set all required properties
		openBISSpecimen.setPropertyValue("DESCRIPTION", name);

		return openBISSpecimen;
	}

	/**
	 * Register a Specimen (as a child of a Tray) based on the Specimen 
	 * XML node and an ISample object
	 * @param specimen An XML node corresponding to a Specimen
	 * @param tray  An ISample object
	 * @return ISample sample, or null
	 */
	private ISample processSpecimen(Node specimen, ISample tray) {
	
		// Initialize the Specimen
		ISample openBISSpecimen;

		// Get Tray attributes
		NamedNodeMap attr = specimen.getAttributes();
		
		// Get the name
		String name = 
				attr.getNamedItem("name").getNodeValue();
		
		// Make an openBISCode from the name
		String openBISCode = name.replaceAll(" ", "_").toUpperCase();
		
		// Build the openBIS Identifier
		String openBISIdentifier =
				tray.getSampleIdentifier() + "/" + openBISCode;

		// TODO This should be stored in the _properties.six file
		String openBISSpecimenType = "UNKNOWN";
		
		// The sample should NOT exist!
		// TODO make sure to handle properly the case where the sample already
		// exists!
		openBISSpecimen = transaction.getSampleForUpdate(openBISIdentifier);
		if(openBISSpecimen == null) {
			openBISSpecimen = transaction.createNewSample(
					openBISIdentifier, openBISSpecimenType);
		}
		
		// TODO Set all required properties
		openBISSpecimen.setPropertyValue("DESCRIPTION", name);
		
		// Set the tray as a container
		openBISSpecimen.setContainer(tray);
		
		return openBISSpecimen;
	}
	
	/**
	 * Register a Tube (as a child of a Specimen) based on the Tube 
	 * XML node and an ISample object
	 * 
	 * The associated fcs file is attached as a IDataset
	 * 
	 * @param tube An XML node corresponding to a Tube
	 * @param specimen  An ISample object
	 * @return ISample sample, or null
	 */
	private ISample processTube(Node tube, ISample specimen) {
	
		ISample openBISTube;
		
		// Get Tray attributes
		NamedNodeMap attr = tube.getAttributes();
		
		// Get the name
		String name = 
				attr.getNamedItem("name").getNodeValue();
		
		// Make an openBISCode from the name
		String openBISCode = name.replaceAll(" ", "_").toUpperCase();
		
		// Build the openBIS Identifier
		String openBISIdentifier =
				specimen.getSampleIdentifier() + "/" + openBISCode;

		// TODO This should be stored in the _properties.six file
		String openBISTubeType = "UNKNOWN";

		// The sample should NOT exist!
		// TODO make sure to handle properly the case where the sample already
		// exists!
		openBISTube = transaction.getSampleForUpdate(openBISIdentifier);
		if(openBISTube == null) {
			openBISTube = transaction.createNewSample(
					openBISIdentifier, openBISTubeType);
		}
		
		// TODO Set all required properties
		openBISTube.setPropertyValue("DESCRIPTION", name);
		
		// Set the specimen as a container
		openBISTube.setContainer(specimen);
		
		// Get the dataset name and build the dataset code 
		// (name without extension)
		String dataFilename = 
				attr.getNamedItem("dataFilename").getNodeValue();
		String datasetCode;
		int indx = dataFilename.lastIndexOf('.');
		if (indx == -1) {
			datasetCode = new String(dataFilename);
		} else {
			datasetCode = new String(dataFilename.substring(0, indx));
		}
		datasetCode = openBISIdentifier + "/" + datasetCode.toUpperCase();
				
		// TODO Set the correct dataset type
		String datasetType = "UNKNOWN";
		
		// Create a new dataset
		IDataSet dataset = transaction.createNewDataSet(
				datasetType, datasetCode);
		
		// Assign the dataset to the sample
		dataset.setSample(openBISTube);

		// Assign the file to the dataset
		transaction.moveFile(
				transaction.getIncoming().getAbsolutePath() +
				File.separator + dataFilename, dataset);

		// TODO Assign the file to experiment as well!
		
		return openBISTube;
	}
	
	/**
	 * Logs the message to file
	 * @param message String to be logged to file
	 */
	private void logToFile(String message) {
		try {
			out = new BufferedWriter(new FileWriter(LOGFILE, true));
			out.write((new Date()).toString() + ": " + message + "\n");
			out.close();
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * The incoming folder is the user folder. The experiment folders are 
	 * subfolders of incoming. This function returns all direct subfolders of
	 * incoming
	 * @param incoming Full path to the incoming folder
	 * @return Array of subfolders
	 */
	private File[] scanIncomingForSubFolders(File incoming) {
		File[] subFolders = incoming.listFiles(
				new FileFilter() {
					public boolean accept(File file) {
						return file.isDirectory();
					}
				});	
		return subFolders;
	}

}
