package ch.eth.scu.dropbox;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v2.AbstractJavaDataSetRegistrationDropboxV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;

/**
 * Dropbox to register BD LSR Fortessa datasets into openBIS
 * @author Aaron Ponti
 *
 */
public class BDLSRFortessaDropbox extends AbstractJavaDataSetRegistrationDropboxV2 {

	@Override
    public void process(IDataSetRegistrationTransactionV2 transaction)
    {
		// Get the name of the incoming dataset folder
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
		Document propertiesDoc = readPropertiesFile(properties);
		if (propertiesDoc == null) {
			// TODO Handle the case where the properties file cannot be parsed!
			return;
		}
		
		// TODO Use the information in the XML file to register the dataset
		// ...
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
	private Document readPropertiesFile(File properties) {
		
		DocumentBuilder parser = null;
		Document doc = null;

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
		
		// Return the parsed Document
		return doc;
	}
}
