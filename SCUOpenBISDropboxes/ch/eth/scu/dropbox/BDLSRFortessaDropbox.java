package ch.eth.scu.dropbox;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
		
		// Find the related <incoming>_properties.xml file
		File properties = buildPropertiesFileName(incoming);
		if (properties == null) {
			// TODO Handle the case the properties file could not be generated!
			// This happens if incoming is a file, or if an exception is thrown
			return;
		}
		
		// Make sure the properties file exists
		if (properties.exists() == false) {
			// TODO Handle the case where the properties file does not exist!
			return;
		}

		// Parse the properties XML file
		readPropertiesFile(properties);
		if (doc == null) {
			// TODO Handle the case where the properties file cannot be parsed!
			return;
		}
		
		// Use the information in the XML file to register the data set
		if (!register()) {
			// TODO Handle failed processing
		}
    }
	
	/**
	 * Build properties file name from the name of the incoming directory
	 * @return <incoming>_properties.xml file
	 */
	private File buildPropertiesFileName(File incoming) {
		
		// Incoming must be a directory
		if (! incoming.isDirectory()) {
			return null;
		}
		
		// Get full incoming name 
		String incomingName;
		try {
			incomingName = incoming.getCanonicalPath();
		} catch (IOException e) {
			// TODO Handle the case one cannot get the incoming path!
			return null;
		}
		
		// Since incoming is a directory, we just append _properties.xml
		// to the canonical path
		return new File(incomingName + "_properties.xml");
	}
	
	/**
	 * Read the properties XML file and returns a Document
	 * @param properties <incoming>_properties.xml file 
	 * @return parsed XML Document or null if parsing failed
	 */
	private void readPropertiesFile(File properties) {
		
		DocumentBuilder parser = null;
		
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
			
			// Iterate over current Experiment children
			NodeList expChildren = e.getChildNodes();
			for ( int j = 0; j < expChildren.getLength(); i++ ) {
			
				// Get child: it can be a Specimen or a Tray
				Node c = expChildren.item(j);
				String type = c.getNodeName();
				
				if (type.equals("Specimen")) {
					
				} else if (type.equals("Tray")) {
					ISample Tray = processTray(c, openBISExperiment);
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
		IExperimentUpdatable openBISExperiment = null;
		
		// Get Experiment attributes
		NamedNodeMap attr = e.getAttributes();
		String name = 
				attr.getNamedItem("name").getNodeValue();
		String openBISCode = 
				attr.getNamedItem("openBISCode").getNodeValue();
		String openBISIdentifier =
				attr.getNamedItem("openBISIdentifier").getNodeValue();
		
		// TODO Store the Experiment type in the _properties.xml file
		String expType = "UNKNOWN";
	
		// Get the Experiment
		openBISExperiment = transaction.getExperimentForUpdate(openBISIdentifier);
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
	 * Register a Tray (Plate) based on the Experiment XML node
	 * @param e An XML node corresponding to a Tray (Plate)
	 * @return ISample sample, or null
	 */
	private ISample processTray(Node tray, IExperimentUpdatable exp) {
		
		// Initialize the Tray
		ISample Tray = null;
		
		// Get Tray attributes
		NamedNodeMap attr = tray.getAttributes();
		String name = 
				attr.getNamedItem("name").getNodeValue();
		
		// Make an openBISCode from the name
		String openBISCode = name.replaceAll(" ", "_").toUpperCase();
		
		// Build the openBIS Identifier
		String openBISIdentifier =
				exp.getExperimentIdentifier() + "/" + openBISCode;
		
		// TODO Map tray_type from the Fortessa software to an openBIS 
		// sample type
		String tray_type = 
				attr.getNamedItem("tray_type").getNodeValue();
		
		// TODO This shoud be mapped from the tray_type
		String openBISSampleType = "PLATE";

		// The sample should NOT exist!
		// TODO make sure to handle properly the case where the sample already
		// exists!
		ISample openBISTray = transaction.getSampleForUpdate(openBISIdentifier);
		if(openBISTray == null) {
			openBISTray = transaction.createNewSample(
					openBISIdentifier, openBISSampleType);
		}
		
		// TODO Set all required properties
		openBISTray.setPropertyValue("DESCRIPTION", name);
		//openBISTray.setPropertyValue("$PLATE_GEOMETRY", mapped_tray_type);
		
		return Tray;
	}
	
}
