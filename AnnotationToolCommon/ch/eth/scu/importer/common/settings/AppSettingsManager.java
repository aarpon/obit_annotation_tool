package ch.eth.scu.importer.common.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.eth.scu.importer.common.version.VersionInfo;
import ch.eth.scu.utils.QueryOS;

/**
 * Commodity class to manage the AnnotationTool application properties
 * @author Aaron Ponti
 */
public class AppSettingsManager {

	static protected String errorMessage = "";

	// Public interface

	/**
	 * Check whether the properties file already exists
	 * @return 	true if the properties file already exists, false
	 * otherwise.
	 */	
	static public boolean settingsFileExists() {
		return getSettingsFileName().exists();
	}

	/**
	 * Initialize application settings with default values.
	 * @return ArrayList of AppSetting containing one element.
	 */
	static public ArrayList<AppSetting> initializeSettings() {
		ArrayList<AppSetting> appSettings = new ArrayList<AppSetting>();
		appSettings.add(new AppSetting());
		return appSettings;
    }

	/**
	 * Check whether the Properties file is at current version
	 * @param appProperties Properties object
	 * @return true if the Properties file is at current version,
	 * false otherwise.
	 * 
	 * Please note that this function will return true if the file does
	 * not exist, since a new AppSettingsManager object will be created and
	 * tested for its version. 
	 * 
	 */
	static public boolean isSettingsFileVersionCurrent() {
		int fileVersion = getSettingsFileVersion();
		return fileVersion == VersionInfo.propertiesVersion;
    }

	/**
	 * Check whether all Properties in the file are set. 
	 * @return true if all Properties in the file are set,
	 * false otherwise.
	 */
	static public boolean areAllPropertiesSet(ArrayList<AppSetting> appSettings) {
		for (AppSetting appSetting : appSettings) {
			if (! appSetting.allSet()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Read the properties from disk 
	 * @return a Properties object or null if it could not be loaded
	 */
	static public ArrayList<AppSetting> readSettingsFromFile() {

		// Make sure the Properties file exists
		if (!AppSettingsManager.settingsFileExists()) {
			return null;
		}

		// Instantiate Settings
		ArrayList<AppSetting> appSettings = new ArrayList<AppSetting>();

		// Read and parse the XML settings file
		Document doc = readXMLFile();
		
		// Get the root node
		Element rootNode = doc.getDocumentElement();
		
		// Now process all children
		NodeList openBISURLNodes = rootNode.getChildNodes();
		for (int i = 0; i < openBISURLNodes.getLength(); i ++) {
			Node openBISURL = openBISURLNodes.item(i);
			
			// Create a new AppSetting object
			AppSetting setting = new AppSetting(openBISURL.getNodeName());
			
			// Get and add the attributes
			NamedNodeMap attrs = openBISURL.getAttributes();
			
			for (int j = 0; j < attrs.getLength(); j++) {
				
				// Get attribute name and value
				String name = attrs.item(j).getNodeName();
				String value = attrs.item(j).getNodeValue();
				
				// Store it
				setting.setSetting(name, value);
			}

			// Now add the AppSetting object
			appSettings.add(setting);
		}
		
		// Return the settings
		return appSettings;
		
	}
	
	/**
	 * Returns the setting for current openBIS server
	 * @param openBISURL: openBIS server URL
	 * @return AppSetting object
	 */
	public static AppSetting getSettingsForServer(String openBISURL) {
	
		// Get the settings
		ArrayList<AppSetting> appSettings = readSettingsFromFile();
		for (AppSetting appSetting : appSettings) {
			if (appSetting.getOpenBISURL().equals(openBISURL)) {
				return appSetting;
			}
		}
		return null;
	}
	
	/**
	 * Write the properties to disk 
	 * @return true if the properties were saved successfully, false otherwise
	 * 
	 * This function might require write access to a restricted system
	 * folder. It should be used only in code run with admin privileges.
	 */
	static public boolean writeSettingsToFile(ArrayList<AppSetting> appSettings) {

		DocumentBuilder builder;
		Document document = null;

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
			for (AppSetting appSetting : appSettings) {
				
				// Get current server URL
				String openBISURL = appSetting.getOpenBISURL();
				
				// Get its properties
			    Map<String, String> currentProperties = 
			    		appSetting.getAllSettings();

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
	public static String getLastErrorMessage() {
		return errorMessage;
	}

	/**
	 * Return all options for a given property.
	 * 
	 * This function is guaranteed to return an ArrayList<String> with at least
	 * one element.
	 * 
	 * @return an ArrayList<String> with all options for a given property name
	 */		
	public static ArrayList<String> possibleValuesForSetting(String property) {
		return AppSetting.possibleValuesForSetting(property);
	}

	/**
	 * Return default option for a given property 
	 * @return an String with the default value for a given property name
	 */		
	public static String defaultValueForSetting(String name) {
		return AppSetting.defaultValueForSetting(name);
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

		// Initialize the applicationDir variable
		File applicationDataDir;
		
		// Build the path as a function of the operating system
		if (QueryOS.isMac()) {
			applicationDataDir = new File(
					"/Users/Shared/Library/Application Support/");			
		} else if (QueryOS.isWindows()) {
			applicationDataDir = new File(
					"C:/Users/All Users/");
		} else {
			throw new UnsupportedOperationException(
					"Operating system not supported.");
		}
		
		// Append the sub-path common to all platform

        return new File(applicationDataDir +
                File.separator + "oBIT" + File.separator + "AnnotationTool");
	}
	
	/**
	 * Returns the properties file name with full path
	 * @return 	name with full path of the properties files
	 */
	static private File getSettingsFileName() {
		return new File( getSettingsPropertiesDir() + 
				File.separator + "settings.cfg" );	
	}

	/**
	 * Create the application data directory
	 * @return 	true if the application data directory could be created
	 * successfully, false otherwise.
	 * 
	 * This function might require write access to a restricted system
	 * folder. It should be used only in code run with admin privileges.
	 */
	static private boolean createApplicationSettingsDir() {
		
		// Get the application directory
		File scuFolder = getSettingsPropertiesDir();
		
		// Create it if not there
        return scuFolder.exists() || scuFolder.mkdirs();
    }

	/**
	 * Read and parse the settings XML file and returns the Document
	 * @return parsed Document
	 */
	private static Document readXMLFile() {

		// Does the file exist?
		if (! getSettingsFileName().exists()) {
			return null;
		}

		// Read and parse the XML settings file
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(getSettingsFileName());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException p) {
			p.printStackTrace();
		} catch (IOException i) {
			i.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		
		// Return the document
		return doc;
	}
	
	/**
	 * Get the version of the settings XML file. It parses the file first.
	 * @return the settings file number or -1 if the file does not exist.
	 */
	private static int getSettingsFileVersion() {
		Document doc = readXMLFile();
		if (doc == null) {
			return -1;
		}
		Element root = doc.getDocumentElement();
		int version = -1;
		try {
			version = Integer.parseInt(root.getAttribute("version")); 
		} catch (NumberFormatException n) {
			return -1;
		}
		return version;
	}	

	/**
	 * Get the version of the settings XML file
	 * @param doc Parsed XML document
	 */
	private static int getSettingsFileVersion(Document doc) {
		Element root = doc.getDocumentElement();
		return Integer.parseInt(root.getAttribute("version"));	
	}
}

