package ch.eth.scu.importer.common.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
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

	protected ArrayList<AppSettings> listAppSettings = null;
	protected int currentSettingsIndex;
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
	public AppSettingsManager() {
		
		// Try to load, otherwise initialize
		if (!load()) {
			initialize();
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
	 * Add a new server
	 */
	public void add() {
   		listAppSettings.add(new AppSettings());
   		currentSettingsIndex = listAppSettings.size() - 1;
	}

	/**
	 * Add a new server
	 */
	public void add(String openBISURL) {
		add();
		setSettingValue("OpenBISURL", openBISURL);
	}

	/**
     * Remove the Setting with given index
     * @param index of the AppSettings in the listAppSettings array.
     */
	public void remove(int index) throws ArrayIndexOutOfBoundsException {
    	
		// We do not allow to remove all settings
		if (listAppSettings.size() <= 1) {
			return;
		}

    	if (index < 0 && index > (listAppSettings.size() - 1)) {
    		throw new ArrayIndexOutOfBoundsException();
    	}
    	
   		listAppSettings.remove(index);
   		index--;
   		if (index < 0) {
   			index = 0;
   		}
   		currentSettingsIndex = index;
    	
    }

	/**
     * Move the Setting with given index down one position
     * @param index of the AppSettings in the listAppSettings array.
     */
	public void moveDown(int index) {
    	
		// We cannot push down the last one
		if (index == (listAppSettings.size())) {
			return;
		}
		
		Collections.swap(listAppSettings, index, index + 1);
		currentSettingsIndex = index + 1;
    	
    }

	/**
     * Move the Setting with given index up one position
     * @param index of the AppSettings in the listAppSettings array.
     */
	public void moveUp(int index) {
    	
		// We cannot push down the first one
		if (index == 0) {
			return;
		}
		
		Collections.swap(listAppSettings, index, index - 1);
		currentSettingsIndex = index - 1;

    }
	
	/**
	 * Return all configured openBIS servers
	 * @return list of server URLs
	 */
	public ArrayList<String> getAllServers() {
	
		ArrayList<String> servers = new ArrayList<String>();
		for (AppSettings current : listAppSettings) {
			servers.add(current.getOpenBISURL());
		}
		
		return servers;
	}
	
	/**
	 * Return the index of currently active setting
	 * @return index of currently active setting.
	 */
	public int getCurrentIndex() {
		return currentSettingsIndex;
	}

	/**
	 * Return the index of currently active setting
	 * @return index of currently active setting.
	 */
	public boolean setCurrent(int newCurrent) throws ArrayIndexOutOfBoundsException {
		if (newCurrent < 0 || newCurrent > (listAppSettings.size() - 1)) {
			throw new ArrayIndexOutOfBoundsException();
		}
		currentSettingsIndex = newCurrent;
		return true;
	}

	/**
	 * Return the value of the setting for current server.
	 * @param name Attribute name
	 * @return the value of the attribute for current setting.
	 */
	public String getSettingValue(String name) {
		return listAppSettings.get(currentSettingsIndex).getSettingValue(name);
	}
	
	/**
	 * Set the value of a specific setting
	 * @param name Name of the setting
	 * @param value Value of the setting
	 */
	public void setSettingValue(String name, String value) {
		listAppSettings.get(currentSettingsIndex).setSettingValue(name, value);
	}

	/**
	 * Checks whether a setting with the specified name must be unique
	 * @param name name of the setting
	 * @return true if the setting must be unique, false otherwise.
	 */
	public boolean settingMustBeUnique(String name) {
		return AppSettings.mustBeUnique(name);
	}

	/**
	 * Checks whether a combination setting name - value already exists
	 * @param name name of the setting
	 * @param value value of the setting
	 * @return true if the setting combination already exists, false otherwise
	 */
	public boolean doesSettingExist(String name, String value) {
		for (AppSettings setting: listAppSettings) {
			if (setting.getSettingValue(name).equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the URL of current openBIS server
	 * @return the URL of current openBIS server.
	 */
	public String getServer() {
		return listAppSettings.get(currentSettingsIndex).getOpenBISURL();
	}

	/**
	 * Set the URL of current openBIS server
	 * @param openBISURL URL for current openBIS server
	 */
	public void setServer(String openBISURL) {
		listAppSettings.get(currentSettingsIndex).setOpenBISURL(openBISURL);
	}
	
	/**
	 * Check whether all Properties in the file are set. 
	 * @return true if all Properties in the file are set,
	 * false otherwise.
	 */
	public boolean allSet() {
		for (AppSettings appSettings : listAppSettings) {
			if (! appSettings.allSet()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the setting for current openBIS server
	 * @param openBISURL openBIS server URL
	 * @return AppSettings object
	 */
	public AppSettings getSettingsForServer(String openBISURL) {
	
		// Get the settings
		for (AppSettings appSettings : listAppSettings) {
			if (appSettings.getOpenBISURL().equals(openBISURL)) {
				return appSettings;
			}
		}
		return null;
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
		ArrayList<AppSettings> loadedAppSettings = new ArrayList<AppSettings>();
	
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
		NodeList openBISURLNodes = rootNode.getChildNodes();
		for (int i = 0; i < openBISURLNodes.getLength(); i ++) {
			Node openBISURL = openBISURLNodes.item(i);
			
			// Create a new AppSettings object
			AppSettings setting = new AppSettings();
			
			// Get and add the attributes
			NamedNodeMap attrs = openBISURL.getAttributes();
			
			for (int j = 0; j < attrs.getLength(); j++) {
				
				// Get attribute name and value
				String name = attrs.item(j).getNodeName();
				String value = attrs.item(j).getNodeValue();
				
				// Store it
				setting.setSettingValue(name, value);
			}
	
			// Now add the AppSettings object
			loadedAppSettings.add(setting);
		}
		
		// Now store the loaded settings
		listAppSettings = loadedAppSettings;
		currentSettingsIndex = 0;
		isFileRead = true;
	
		// Check that the file version is current
		if (fileVersion != VersionInfo.propertiesVersion) {
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
			Element root = document.createElement("AnnotationTool_Properties");
			root.setAttribute("version",
					Integer.toString(VersionInfo.propertiesVersion));

			// Get all properties for all servers and store them in an XML document
			for (AppSettings appSettings : listAppSettings) {
				
				// Get its properties
			    Map<String, String> currentProperties = 
			    		appSettings.getAllSettings();

				// Create the experiment
			    Element element = document.createElement("server"); 

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
	public String getLastErrorMessage() {
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
	public ArrayList<String> possibleValuesForSetting(String name) {
		return AppSettings.possibleValuesForSetting(name);
	}

	/**
	 * Return default option for a given property 
	 * @return an String with the default value for a given property name
	 */		
	public String defaultValueForSetting(String name) {
		return AppSettings.defaultValueForSetting(name);
	}

	/**
	 * Returns the description for the specified acquisition station
	 * @param name Name of the acquisition station
	 * @return description of acquisition station
	 */
	public String getAcqStationDescription(String name) {
		return AppSettings.getAcqStationDescription(name);
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
		
		// Create it if not there
	    return scuFolder.exists() || scuFolder.mkdirs();
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
				File.separator + "settings.xml");	
	}

	/**
	 * Initialize application settings with default values.
	 */
	private void initialize() {
		listAppSettings = new ArrayList<AppSettings>();
		listAppSettings.add(new AppSettings());
		currentSettingsIndex = 0;
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
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(getSettingsFileName());
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

