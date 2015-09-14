package ch.ethz.scu.obit.common.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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

import ch.ethz.scu.obit.common.utils.QueryOS;
import ch.ethz.scu.obit.common.version.VersionInfo;

/**
 * Commodity class to manage the AnnotationTool user application properties
 * 
 * This class is only visible within its package.

 * @author Aaron Ponti
 */
class UserSettingsManager {

	protected int favoriteServerSettingsIndex = 0;
	protected ArrayList<UserSettings> listUserSettings = null;
	protected int currentServerSettingsIndex;
	protected String errorMessage = "";

	// Public interface

	/**
	 * Default Constructor. 
	 * 
	 * When using this constructor, settings must be explicitly loaded.
	 * 
	 * @see UserSettingsManager#load()
	 * 
	 */
	public UserSettingsManager(ArrayList<String> configuredServers) {

		// Try to load and validate, otherwise initialize
		if (!load()) {
			initialize(configuredServers);
		} else {
			validate(configuredServers);
		}
		
		// Save
		save();
	}
	
	/**
	 * Initialize application settings with default values.
	 */
	private void initialize(ArrayList<String> configuredServers) {
		listUserSettings = new ArrayList<UserSettings>();
		for (int i = 0; i < configuredServers.size(); i++) {
			listUserSettings.add(new UserSettings(configuredServers.get(i)));
		}
		favoriteServerSettingsIndex = 0;
		currentServerSettingsIndex = 0;
	}
	
	/**
	 * Validate the servers stored in the User settings with those stored in the Application settings.
	 * @param configuredServers List of openBIS server URLs.
	 */
	private void validate(ArrayList<String> configuredServers) {
		
		if (listUserSettings.size() != configuredServers.size()) {
			initialize(configuredServers);
			return;
		}
		
		int numServers = listUserSettings.size();
		
		for (int i = 0; i < numServers; i++) {
			
			UserSettings current = listUserSettings.get(i);
			
			boolean found = false;
			
			for (int j = 0; j < numServers; j++) {
				if (current.getOpenBISURL().equals(configuredServers.get(j))) {
					found = true;
					break;
				}
			}
			if (! found) {
				initialize(configuredServers);
			}
		}
	}

	/**
	 * Set the current active settings by openBIS URL
	 * @param openBISURL openBIS URL
	 * @return true if the settings for the specified openBIS URL could be set,
	 * false otherwise.
	 */
	public boolean setActiveServer(String openBISURL) {
		for (int i = 0; i < listUserSettings.size(); i++) {
			if (listUserSettings.get(i).getOpenBISURL().equals(openBISURL)) {
				currentServerSettingsIndex = i;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Set the current active settings by openBIS URL
	 * @param openBISURL openBIS URL
	 * @return true if the settings for the specified openBIS URL could be set,
	 * false otherwise.
	 */
	public String getActiveServer() {
		return listUserSettings.get(currentServerSettingsIndex).getOpenBISURL();
	}

	/**
	 * Get the favorite server.
	 * @return the URL of the favorite server.
	 */
	public String getFavoriteServer() {
		return listUserSettings.get(favoriteServerSettingsIndex).getOpenBISURL();
	}

	/**
	 * Set the favorite server. The favorite server MUST be one of the servers
	 * passed in the UserSettingsManager class constructor.
	 * @param openBISURL openBIS URL
	 * @return true if the favorite server could be set, false otherwise.
	 */
	public boolean setFavoriteServer(String openBISURL) {
		for (int i = 0; i < listUserSettings.size(); i++) {
			if (listUserSettings.get(i).getOpenBISURL().equals(openBISURL)) {
				favoriteServerSettingsIndex = i;
				if (! save()) {
					errorMessage = "Could not store settings to disk.";
					return false;
				} else {
					errorMessage = "";
					return true;
				}
			}
		}
		errorMessage = "Invalid openBIS URL.";
		return false;
	}

	/**
	 * Set the default project for the active server.
	 * 
	 * Please notice that the validation of the passed project identifier must 
	 * be done externally. The UserSettingsManager and the UserSettings classes
	 * do not attempt to check the passed identifier against openBIS. 
	 * 
	 * This is a User Setting.
	 *  
	 * @param project openBIS identifier of the project.
	 */
	public void setDefaultProject(String project) {
		listUserSettings.get(currentServerSettingsIndex)
		.setSettingValue("DefaultOpenBISProject", project);
	}

	/**
	 * Get the default project for the activer server.
	 * 
	 * This is a User Setting.
	 *  
	 * @return openBIS identifier of the project.
	 */
	public String getDefaultProject() {
		return listUserSettings.get(currentServerSettingsIndex)
				.getSettingValue("DefaultOpenBISProject");
	}

	/**
	 * Returns the properties file name with full path
	 * 
	 * @return name with full path of the properties files
	 */
	public ArrayList<String> getSettingsNames() {
		return UserSettings.getSettingsNames();
	}

	/**
	 * Return the value for the requested setting and currently active server.
	 * 
	 * @param name setting name
	 * @return the value of the attribute for current setting.
	 */
	public String getSettingValue(String name) {
		UserSettings userSettings = listUserSettings.get(currentServerSettingsIndex);
		return userSettings.getSettingValue(name);
	}

	/**
	 * Set the value of a specific setting and currently active server.
	 * @param name Name of the setting
	 * @param value Value of the setting
	 */
	public void setSettingValue(String name, String value) {
		listUserSettings.get(currentServerSettingsIndex).setSettingValue(name, value);
	}

	/**
	 * Try reading settings from file. If loading fails, current settings are
	 * left untouched.
	 * 
	 * @return true if the settings were loaded correctly, false otherwise.
	 */
	private boolean load() {

		// Make sure the Properties file exists
		if (! settingsFileExists()) {
			errorMessage = "Settings file does not exist.";
			return false;
		}

		// Read and parse the XML settings file
		Document doc = readXMLFile();
		if (doc == null) {
			// Error message already set in readXMLFile()
			return false;
		}

		// Get the root node
		Element rootNode = doc.getDocumentElement();

		// Do we have old-style user settings?
		if (rootNode.getNodeName().equals("AnnotationTool_Properties")) {
		
			// This is an old-style user settings. 
			return false;
		}

		// Now process all children
		NodeList serversNode = rootNode.getChildNodes();
		
		// Get the favorite server
		NamedNodeMap attrs = serversNode.item(0).getAttributes();
		String favoriteOpenBISURL = attrs.item(0).getNodeValue();

		// Allocate space to store the loaded settings
		ArrayList<UserSettings> loadedListSettings = new ArrayList<UserSettings>();

		// Get all servers
		NodeList serverNode = serversNode.item(0).getChildNodes();
		for (int i = 0; i < serverNode.getLength(); i ++) {
			Node server = serverNode.item(i);
			
			// Create a new AppSettings object
			UserSettings settings = new UserSettings();
			
			// Get and add the attributes
			NamedNodeMap serverAttrs = server.getAttributes();
			
			for (int j = 0; j < serverAttrs.getLength(); j++) {

				// Get attribute name and value
				String name = serverAttrs.item(j).getNodeName();
				String value = serverAttrs.item(j).getNodeValue();

				// Store it
				settings.setSettingValue(name, value);
			}
			
			// Append user settings for current server
			loadedListSettings.add(settings);
			
		}

		// Reset error message
		errorMessage = "";

		// Now store the loaded settings
		listUserSettings = loadedListSettings;
		
		// Set the favorite one
		setActiveServer(favoriteOpenBISURL);
		
		// Return success
		return true;
	}

	/**
	 * Try writing settings to file. If writing fails, use getLastErrorMessage()
	 * to get the details.
	 * 
	 * This function might require write access to a restricted system folder.
	 * It should be used only in code run with admin privileges.
	 * 
	 * @return true if the properties were saved successfully, false otherwise
	 */
	private boolean save() {

		// Check that the settings are set
		if (listUserSettings == null || listUserSettings.size() == 0) {
			errorMessage = "No settings to save!";
			return false;
		}

		DocumentBuilder builder;
		Document document = null;

		// Build the XML document
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.newDocument();

			// Create root element
			Element root = document.createElement("AnnotationTool_User_Settings");
			root.setAttribute("version",
					Integer.toString(VersionInfo.userSettingsVersion));

			// Create servers element
			Element servers = document.createElement("servers");
			servers.setAttribute("favoriteServerURL", 
					listUserSettings.get(favoriteServerSettingsIndex).getOpenBISURL());
			
			// Get all user properties for all servers and store them in an XML document
			for (UserSettings userSettings : listUserSettings) {
				
				// Get its properties
			    Map<String, String> currentProperties = 
			    		userSettings.getAllSettings();

				// Create the experiment
			    Element element = document.createElement("server"); 
			    element.setAttribute("OpenBISURL", userSettings.getOpenBISURL());

				// Append all properties as attributes
				for (Map.Entry<String, String> curr : currentProperties.entrySet() ) {
					
					// Get the property name and value
					String propertyName = curr.getKey();
					String propertyValue = curr.getValue();
					
					// Store them as attributes of the server element
					element.setAttribute(propertyName, propertyValue);
					
				}

				// Append the server element to the document
				servers.appendChild(element);

			}
			
			// Append the servers node
			root.appendChild(servers);
			
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
		if (!createApplicationSettingsDir()) {
			return false;
		}

		// Now try to write to disk
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			OutputStream outputStream = new FileOutputStream(
					getSettingsFileName());
			t.transform(new DOMSource(document), new StreamResult(outputStream));
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
	 * 
	 * @return last error message.
	 */
	public String getLastErrorMessage() {
		return errorMessage;
	}

	/**
	 * Check whether the properties file already exists
	 * 
	 * @return true if the properties file already exists, false otherwise.
	 */
	static public boolean settingsFileExists() {
		return getSettingsFileName().exists();
	}

	/**
	 * Create the user data directory
	 * 
	 * @return true if the user data directory could be created
	 *         successfully, false otherwise.
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
	 * 
	 * @return path to the folder where the application properties will be
	 *         stored
	 * @throws UnsupportedOperationException
	 *             if the operating system is not one of Mac OS X or Windows 7.
	 */
	static private File getSettingsPropertiesDir()
			throws UnsupportedOperationException {

		// Get the common application data folder
		File userDataDir = QueryOS.getOSSpecificUserDataFolder();
		
		// Append the sub-path common to all platform
        return new File(userDataDir +
                File.separator + "obit" + File.separator + "AnnotationTool");
	}

	/**
	 * Returns the properties file name with full path
	 * 
	 * @return name with full path of the properties files
	 */
	static private File getSettingsFileName() {
		return new File(getSettingsPropertiesDir() + File.separator
				+ "user_settings.xml");
	}

	/**
	 * Read and parse the settings XML file and returns the Document
	 * 
	 * @return parsed Document
	 */
	private Document readXMLFile() {

		// Does the file exist?
		if (!getSettingsFileName().exists()) {
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
