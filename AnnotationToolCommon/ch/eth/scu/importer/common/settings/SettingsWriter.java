package ch.eth.scu.importer.common.settings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.eth.scu.importer.common.version.VersionInfo;


/**
 * Writes properties to XML file
 * @author Aaron Ponti
 *
 */
public class SettingsWriter {

	protected DocumentBuilder builder;
	protected Document document = null;
	
	/**
	 * Constructor
	 * @param properties: map of properties for each server
	 */
	public SettingsWriter(HashMap<String, HashMap<String, String>> properties) {

		// Build the XML document
		try {
			builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			document = builder.newDocument();

			// Create root element
			Element root = document.createElement("AnnotationTool_Properties");
			root.setAttribute("version",
					Integer.toString(VersionInfo.propertiesVersion));

			// Get all properties for all servers and store them in an XML document
			for (Map.Entry<String, HashMap<String, String>> entry :
				properties.entrySet() ) {
				
				// Get current server URL
				String openBISURL = entry.getKey();
				
				// Get its properties
			    HashMap<String, String> currentProperties = entry.getValue();

				// Create the experiment
			    Element element = document.createElement(openBISURL); 

				// Append all properties as attributes
				for (Map.Entry<String, String> curr : currentProperties.entrySet() ) {
					
					// Get the property name and value
					String propertyName = curr.getKey();
					String propertyValue = curr.getValue();
					
					// Store them as attributes of the server element
					element.setAttribute(propertyName, propertyValue);
					
				}

				// Append the server element to the document
				root.appendChild(element);

			}
			
			// Add the whole tree to the document (by adding the root node)
			document.appendChild(root);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Save the XML document to file
	 * @param filename File name with full path for the XML file
	 * @return true if saving was successful, false otherwise
	 */
	public boolean saveToFile(String filename) {

		try {
			Transformer t = 
					TransformerFactory.newInstance().newTransformer();
			OutputStream outputStream = new FileOutputStream(filename);
			t.transform(new DOMSource(document), 
					new StreamResult(outputStream));
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (TransformerException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
  
}

