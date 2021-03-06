package ch.ethz.scu.obit.common.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ch.ethz.scu.obit.common.utils.QueryOS;
import ch.ethz.scu.obit.common.version.VersionInfo;

/**
 * Commodity class to manage the AnnotationTool application properties
 * 
 * @author Aaron Ponti
 * 
 * This class is only visible within its package.
 *  
 */
public class AppUpdaterSettingsManager {

	protected AppUpdaterSettings appUpdaterSettings = null;
	protected String errorMessage = "";
	private boolean isFileRead = false;
	private boolean isFileCurrent = false;
	private boolean isFileValid = false;
	private boolean fileExists = false;
	private int fileVersion = -1;

	// Public interface

	/**
	 * Constructor
	 */
	public AppUpdaterSettingsManager() {
		
		// Try to load, otherwise initialize
		if (!load()) {
		    appUpdaterSettings = new AppUpdaterSettings();
		}

	}
	
	/**
	 * Return true if the settings file is valid
	 * @return true if the settings file is valid
	 */
	public boolean isFileValid() {
		return isFileValid;
	}

	/**
	 * Return true if the settings file was found and read
	 * @return true if the settings file was found and read, false otherwise
	 */
	public boolean isFileRead() {
		return isFileRead;
	}

	/**
	 * Return true if the settings file version is current
	 * @return true if the settings file version is current, false otherwise
	 */
	public boolean isFileCurrent() {
		return isFileCurrent;
	}

	/**
	 * Return the value of the setting for current server.
	 * @param name Attribute name
	 * @return the value of the attribute for current setting.
	 */
	public String getSettingValue(String name) {
		return appUpdaterSettings.getSettingValue(name);
	}
	
	/**
	 * Set the value of a specific setting
	 * @param name Name of the setting
	 * @param value Value of the setting
	 */
	public void setSettingValue(String name, String value) {
	    appUpdaterSettings.setSettingValue(name, value);
	}

	/**
	 * Checks whether a combination setting name - value already exists
	 * @param name name of the setting
	 * @param value value of the setting
	 * @return true if the setting combination already exists, false otherwise
	 */
	public boolean doesSettingExist(String name, String value) {
	    return (appUpdaterSettings.getSettingValue(name).equalsIgnoreCase(value));
	}

	/**
	 * Check whether all Properties in the file are set. 
	 * @return true if all Properties in the file are set,
	 * false otherwise.
	 */
	public boolean allSet() {
	    errorMessage = "";
	    boolean success = appUpdaterSettings.allSet();
	    if (!success) {
	        errorMessage = appUpdaterSettings.getLastErrorMessage();
	    }
	    return success;
	}
	
	/**
	 * Try reading settings from file. If loading fails, current settings are 
	 * left untouched.
	 * 
	 * @return true if the settings were loaded correctly, false otherwise.
	 */
	public boolean load() {
	
		// Make sure the Properties file exists
		fileExists = settingsFileExists();
		if (!fileExists) {
			isFileRead = false;
			isFileCurrent = false;
			isFileValid = false;
			fileVersion = -1;
			errorMessage = "Settings file does not exist.";
			return false;
		}
	
	
		// Instantiate new Settings
		appUpdaterSettings = new AppUpdaterSettings();
	
		// Read and parse the XML settings file
		Document doc = readXMLFile();
		if (doc == null) {
			// Error message already set in readXMLFile()
			return false;
		}
		
		// Get the root node
		Element rootNode = doc.getDocumentElement();
		
		// Store the file version
		try {
			fileVersion = Integer.parseInt(rootNode.getAttribute("version"));
		} catch (NumberFormatException n) {
			fileVersion = -1;
		}
	
		// Now process all children
		NodeList firstLevelNodes = rootNode.getChildNodes();
		if (firstLevelNodes.getLength() != 1) {
            errorMessage = "The settings file is not valid!";
            isFileValid = false;
            return false;
		}
		
		Node settings = firstLevelNodes.item(0);
			
		// Get and add the attributes
		NamedNodeMap attrs = settings.getAttributes();
			
		for (int j = 0; j < attrs.getLength(); j++) {
				
			// Get attribute name and value
			String name = attrs.item(j).getNodeName();
			String value = attrs.item(j).getNodeValue();
				
			// Store it
			appUpdaterSettings.setSettingValue(name, value);
		}
		
		isFileRead = true;
	
		// Check that the file version is current.
		if (fileVersion != VersionInfo.applicationUpdaterSettingsVersion) {
			
				errorMessage = "The settings file is obsolete.";
				isFileCurrent = false;
				return false;
		}
		
		// Set file to be current
		isFileCurrent = true;
		
		// Run a validation
		if (! allSet()) {
			errorMessage = "File did not pass validation.";
			isFileValid = false;
			return false;
		}

		// Set the file to be valid
		isFileValid = true;

		// Reset error message
		errorMessage = "";
	
		// Return success
		return true;
	}


	/**
	 * Try writing settings to file. If writing fails, use getLastErrorMessage()
	 * to get the details.
	 * 
	 * This function might require write access to a restricted system
	 * folder. It should be used only in code run with admin privileges.
	 *  
	 * @return true if the properties were saved successfully, false otherwise
	 */
	public boolean save() {

		DocumentBuilder builder;
		Document document = null;

		// Build the XML document
		try {
			builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			document = builder.newDocument();

			// Create root element
			Element root = document.createElement("AnnotationToolUpdater_App_Settings");
			root.setAttribute("version",
					Integer.toString(VersionInfo.applicationUpdaterSettingsVersion));
			
			// Get its properties
		    Map<String, String> currentProperties = appUpdaterSettings.getAllSettings();

			// Create the experiment
			Element element = document.createElement("settings"); 

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

			
			// Add the whole tree to the document (by adding the root node)
			document.appendChild(root);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return false;
		} catch (DOMException e) {
			e.printStackTrace();
			return false;
		}

		// Make sure the directory exists
		if (! createApplicationSettingsDir()) {
			return false;
		}

		// Now try to write to disk
		try {
			Transformer t = 
					TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "no");
			OutputStream outputStream = new FileOutputStream(getSettingsFileName());
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

	// Private methods

	/**
	 * Return last error message.
	 * @return last error message.
	 */
	public String getLastErrorMessage() {
		return errorMessage;
	}

	/**
	 * Return all options for a given property.
	 * 
	 * This function is guaranteed to return an {@code ArrayList<String>} with at least
	 * one element.
	 * @param name Name of the setting.
	 * 
	 * @return an {@code ArrayList<String>} with all options for a given property name
	 */		
	public ArrayList<String> possibleValuesForSetting(String name) {
		return AppSettings.possibleValuesForSetting(name);
	}

	/**
	 * Return default option for a given property 
	 * @param name name of the setting
	 * @return an String with the default value for a given property name
	 */		
	public String defaultValueForSetting(String name) {
		return AppSettings.defaultValueForSetting(name);
	}

	/**
	 * Check whether the properties file already exists
	 * @return 	true if the properties file already exists, false
	 * otherwise.
	 */	
	static public boolean settingsFileExists() {
		return getSettingsFileName().exists();
	}


	/**
	 * Create the application data directory
	 * @return 	true if the application data directory could be created
	 * successfully, false otherwise.
	 * 
	 * This function might require write access to a restricted system
	 * folder. It should be used only in code run with admin privileges.
	 */
	private boolean createApplicationSettingsDir() {
		
		// Get the application directory
		File scuFolder = getSettingsPropertiesDir();
		
		// Does the folder exist already?
		if (scuFolder.exists()) {
			return true;
		}
		
		// Try creating it
	    if (! scuFolder.mkdirs()) {
	    	errorMessage = "Could not create settings directory.";
	    	return false;
	    }
	    return true;
	}

	/**
	 * Returns the application data directory
	 * @return 	path to the folder where the application properties will
	 * 			be stored 
	 * @throws 	UnsupportedOperationException if the operating system is not one 
	 * 			of Mac OS X or Windows 7.
	 */
	static private File getSettingsPropertiesDir() 
			throws UnsupportedOperationException {

		// Get the common application data folder
		File applicationDataDir = QueryOS.getOSSpecificAppDataFolder();
		
		// Append the sub-path common to all platform
        return new File(applicationDataDir +
                File.separator + "obit" + File.separator + "AnnotationTool");
	}
	
	/**
	 * Returns the properties file name with full path
	 * @return 	name with full path of the properties files
	 */
	static private File getSettingsFileName() {
		return new File(getSettingsPropertiesDir() + 
				File.separator + "updater_settings.xml");	
	}

	/**
	 * Read and parse the settings XML file and returns the Document
	 * @return parsed Document
	 */
	private Document readXMLFile() {

		// Does the file exist?
		if (! getSettingsFileName().exists()) {
			errorMessage = "Settings file does not exist.";
			return null;
		}

		// Read and parse the XML settings file
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
						
			// Read the file into a String. We need to do this because we MUST
			// remove any line feeds and blank spaces bewteen XML tags or the
			// DocumentBuilder will fail parsing the XML content.
			// DocumentBuilderFactory.setIgnoringElementContentWhitespace(true)
			// does not seem to help...
			byte[] b = Files.readAllBytes(Paths.get(getSettingsFileName().toURI()));
			String xmlString = new String(b, StandardCharsets.UTF_8); 
			
			// Make sure the String starts with <?xml
			int indx = xmlString.indexOf("<?xml");
			if (indx > 0) {
				xmlString = xmlString.substring(indx);
			}

		    // Remove line endings
		    if (QueryOS.isWindows()) {
		    	xmlString = xmlString.replaceAll("\r\n", ""); 
		    } else {
		    	xmlString = xmlString.replaceAll("\n", ""); 
		    }

		    // Also, make sure that there are no spaces between elements
		    xmlString = xmlString.replaceAll(">\\s*<", "><");
		    		
		    // Create an input source
			InputSource is = new InputSource();
		    is.setCharacterStream(new StringReader(xmlString));

		    // Now parse it
		    doc = dBuilder.parse(is);
		    
		} catch (ParserConfigurationException e) {
			errorMessage = "Error parsing the settings file.";
			return null;
		} catch (SAXException p) {
			errorMessage = "Error parsing the settings file.";
			return null;
		} catch (IOException i) {
			errorMessage = "Error reading the settings file.";
			return null;
		}
		doc.getDocumentElement().normalize();
		
		// Return the document
		return doc;
	}

}

