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
import org.xml.sax.SAXException;

import ch.ethz.scu.obit.common.utils.QueryOS;
import ch.ethz.scu.obit.common.version.VersionInfo;

/**
 * Commodity class to manage the AnnotationTool user application properties
 *
 * This class is only visible within its package.
 *
 * @author Aaron Ponti
 */
class UserSettingsManager {

	protected int favoriteConfigurationSettingsIndex = 0;
	protected ArrayList<UserSettings> listUserSettings = null;
	protected int currentConfigurationSettingsIndex;
	protected String errorMessage = "";

	// Public interface

	/**
	 * Default Constructor.
	 *
	 * When using this constructor, settings must be explicitly loaded.
	 *
	 * @param configurations List of configurations.
	 *
	 * @see UserSettingsManager#load()
	 *
	 */
	public UserSettingsManager(ArrayList<String> configurations) {

		// Try to load and validate, otherwise initialize
		if (!load()) {
			initialize(configurations);
		} else {
			validate(configurations);
		}

		// Save
		save();
	}

	/**
	 * Initialize application settings with default values.
	 *
	 * @param configurations List of configuration names.
	 */
	private void initialize(ArrayList<String> configurations) {
		listUserSettings = new ArrayList<UserSettings>();
		for (int i = 0; i < configurations.size(); i++) {
			listUserSettings.add(new UserSettings(configurations.get(i)));
		}
		favoriteConfigurationSettingsIndex = 0;
		currentConfigurationSettingsIndex = 0;
	}

	/**
	 * Validate the servers stored in the User settings with those stored in the
	 * Application settings.
	 *
	 * @param configurations List of configuration names.
	 */
	private void validate(ArrayList<String> configurations) {

		if (listUserSettings.size() != configurations.size()) {
			initialize(configurations);
			return;
		}

		int numServers = listUserSettings.size();

		for (int i = 0; i < numServers; i++) {

			UserSettings current = listUserSettings.get(i);

			boolean found = false;

			for (int j = 0; j < numServers; j++) {
				if (current.getSettingValue("ConfigurationName").equals(configurations.get(j))) {
					found = true;
					break;
				}
			}
			if (!found) {
				initialize(configurations);
			}
		}
	}

	/**
	 * Set the current active settings by configuration name
	 *
	 * @param configurationName Configuration name
	 * @return true if the settings for the specified configuration name could be
	 *         set, false otherwise.
	 */
	public boolean setActiveConfiguration(String configurationName) {
		for (int i = 0; i < listUserSettings.size(); i++) {
			if (listUserSettings.get(i).getConfigurationName().equals(configurationName)) {
				currentConfigurationSettingsIndex = i;
				return true;
			}
		}
		return false;
	}

//	/**
//	 * Get the openBIS URL of currently active server.
//	 *
//	 * @return the URL of the currently active openBIS server.
//	 */
//	public String getActiveServer() {
//		return listUserSettings.get(currentServerSettingsIndex).getOpenBISURL();
//	}

//	/**
//	 * Get the favorite configuration.
//	 *
//	 * @return the name of the favorite configuration.
//	 */
//	public String getFavoriteServer() {
//		return listUserSettings.get(favoriteConfigurationSettingsIndex).getOpenBISURL();
//	}

	/**
	 * Set and persist the favorite configuration. The favorite configuration MUST
	 * be one of the configurations passed in the UserSettingsManager class
	 * constructor.
	 *
	 * @param configurationName Configuration name
	 * @return true if the favorite configuration could be stored, false otherwise.
	 */
	public boolean storeFavoriteConfiguration(String configurationName) {
		for (int i = 0; i < listUserSettings.size(); i++) {
			if (listUserSettings.get(i).getConfigurationName().equals(configurationName)) {
				favoriteConfigurationSettingsIndex = i;
				if (!save()) {
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

//	/**
//	 * Set without persisting the favorite server. The favorite server MUST be one
//	 * of the servers passed in the UserSettingsManager class constructor.
//	 *
//	 * @param openBISURL openBIS URL
//	 * @return true if the favorite server could be set, false otherwise.
//	 */
//	private boolean setFavoriteServer(String openBISURL) {
//		for (int i = 0; i < listUserSettings.size(); i++) {
//			if (listUserSettings.get(i).getOpenBISURL().equals(openBISURL)) {
//				favoriteConfigurationSettingsIndex = i;
//				return true;
//			}
//		}
//		return false;
//	}

	/**
	 * Set without persisting the favorite configuration. The favorite configuration
	 * MUST be one of the configurations passed in the UserSettingsManager class
	 * constructor.
	 *
	 * @param configurationName Configuration name
	 * @return true if the favorite configuration could be set, false otherwise.
	 */
	private boolean setFavoriteConfiguration(String configurationName) {
		for (int i = 0; i < listUserSettings.size(); i++) {
			if (listUserSettings.get(i).getConfigurationName().equals(configurationName)) {
				favoriteConfigurationSettingsIndex = i;
				return true;
			}
		}
		return false;
	}

	/**
	 * Set and persist the default project for the active server.
	 *
	 * Please notice that the validation of the passed project identifier must be
	 * done externally. The UserSettingsManager and the UserSettings classes do not
	 * attempt to check the passed identifier against openBIS.
	 *
	 * This is a User Setting.
	 *
	 * @param project openBIS identifier of the project.
	 * @return true if the default project could be stored and persisted, false
	 *         otherwise.
	 */
	public boolean storeDefaultProject(String project) {

		// Set the default project
		listUserSettings.get(currentConfigurationSettingsIndex).setSettingValue("DefaultOpenBISProject", project);

		// Store
		return save();
	}

	/**
	 * Get the default project for the activer server.
	 *
	 * This is a User Setting.
	 *
	 * @return openBIS identifier of the project.
	 */
	public String getDefaultProject() {
		return listUserSettings.get(currentConfigurationSettingsIndex).getSettingValue("DefaultOpenBISProject");
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
	 * Try reading settings from file. If loading fails, current settings are left
	 * untouched.
	 *
	 * @return true if the settings were loaded correctly, false otherwise.
	 */
	private boolean load() {

		// Make sure the Properties file exists
		if (!settingsFileExists()) {
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

		// Do we have an obsolete user settings?
		if (rootNode.getNodeName().equals("AnnotationTool_Properties")) {
			// This is an old-style user settings.
			return false;
		}

		// Is version < 2?
		if (Integer.parseInt(rootNode.getAttribute("version")) < 2) {
			// This is a pre-version 2 user settings.
			return false;
		}

		// Now process all children
		NodeList allConfigurationsNode = rootNode.getChildNodes();

		// Get the favorite server
		NamedNodeMap attrs = allConfigurationsNode.item(0).getAttributes();
		String favoriteConfiguration = attrs.item(0).getNodeValue();

		// Allocate space to store the loaded settings
		ArrayList<UserSettings> loadedListSettings = new ArrayList<UserSettings>();

		// Get all configurations
		NodeList configurationNode = allConfigurationsNode.item(0).getChildNodes();
		for (int i = 0; i < configurationNode.getLength(); i++) {
			Node configuration = configurationNode.item(i);

			// Create a new AppSettings object
			UserSettings settings = new UserSettings();

			// Get and add the attributes
			NamedNodeMap configurationAttrs = configuration.getAttributes();

			for (int j = 0; j < configurationAttrs.getLength(); j++) {

				// Get attribute name and value
				String name = configurationAttrs.item(j).getNodeName();
				String value = configurationAttrs.item(j).getNodeValue();

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
		setFavoriteConfiguration(favoriteConfiguration);
		setActiveConfiguration(favoriteConfiguration);

		// Return success
		return true;
	}
//
//	/**
//	 * Try writing settings to file. If writing fails, use getLastErrorMessage() to
//	 * get the details.
//	 *
//	 * This function might require write access to a restricted system folder. It
//	 * should be used only in code run with admin privileges.
//	 *
//	 * @return true if the properties were saved successfully, false otherwise
//	 */
//	private boolean save() {
//
//		// Check that the settings are set
//		if (listUserSettings == null || listUserSettings.size() == 0) {
//			errorMessage = "No settings to save!";
//			return false;
//		}
//
//		DocumentBuilder builder;
//		Document document = null;
//
//		// Build the XML document
//		try {
//			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//			document = builder.newDocument();
//
//			// Create root element
//			Element root = document.createElement("AnnotationTool_User_Settings");
//			root.setAttribute("version", Integer.toString(VersionInfo.userSettingsVersion));
//
//			// Create servers element
//			Element configurations = document.createElement("configurations");
//			configurations.setAttribute("FavoriteConfiguration",
//					listUserSettings.get(favoriteConfigurationSettingsIndex).getConfigurationName());
//
//			// Get all user properties for all servers and store them in an XML document
//			for (UserSettings userSettings : listUserSettings) {
//
//				// Get its properties
//				Map<String, String> currentProperties = userSettings.getAllSettings();
//
//				// Create the configuration
//				Element element = document.createElement("configuration");
//
//				// Append all properties as attributes
//				for (Map.Entry<String, String> curr : currentProperties.entrySet()) {
//
//					// Get the property name and value
//					String propertyName = curr.getKey();
//					String propertyValue = curr.getValue();
//
//					// Store them as attributes of the server element
//					element.setAttribute(propertyName, propertyValue);
//
//				}
//
//				// Append the server element to the document
//				configurations.appendChild(element);
//
//			}
//
//			// Append the servers node
//			root.appendChild(configurations);
//
//			// Add the whole tree to the document (by adding the root node)
//			document.appendChild(root);
//
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//			return false;
//		} catch (DOMException e) {
//			e.printStackTrace();
//			return false;
//		}
//
//		// Make sure the directory exists
//		if (!createApplicationSettingsDir()) {
//			return false;
//		}
//
//		// Now try to write to disk
//		try {
//			Transformer t = TransformerFactory.newInstance().newTransformer();
//			OutputStream outputStream = new FileOutputStream(getSettingsFileName());
//			t.transform(new DOMSource(document), new StreamResult(outputStream));
//			outputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		} catch (TransformerException e) {
//			e.printStackTrace();
//			return false;
//		}
//
//		return true;
//	}

	/**
	 * Try writing settings to file. If writing fails, use getLastErrorMessage() to
	 * get the details.
	 *
	 * This function might require write access to a restricted system folder. It
	 * should be used only in code run with admin privileges.
	 *
	 * @return true if the properties were saved successfully, false otherwise
	 */
	public boolean save() {

		DocumentBuilder builder;
		Document document = null;

		// Build the XML document
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.newDocument();

			// Create root element
			Element root = document.createElement("AnnotationTool_User_Settings");
			root.setAttribute("version", Integer.toString(VersionInfo.userSettingsVersion));

			// Create a configurations element
			Element configurationsElement = document.createElement("configurations");
			configurationsElement.setAttribute("FavoriteConfiguration",
					listUserSettings.get(favoriteConfigurationSettingsIndex).getConfigurationName());

			// Append the configurations element to the document
			root.appendChild(configurationsElement);

			// Get all properties for all servers and store them in an XML document
			for (UserSettings userSettings : listUserSettings) {

				// Get its properties
				Map<String, String> currentProperties = userSettings.getAllSettings();

				// Create the experiment
				Element element = document.createElement("configuration");

				// Append all properties as attributes
				for (Map.Entry<String, String> curr : currentProperties.entrySet()) {

					// Get the property name and value
					String propertyName = curr.getKey();
					String propertyValue = curr.getValue();

					// Store them as attributes of the server element
					element.setAttribute(propertyName, propertyValue);

				}

				// Append the configuration element to the document
				configurationsElement.appendChild(element);

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
		if (!createApplicationSettingsDir()) {
			return false;
		}

		// Now try to write to disk
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "no");
			OutputStream outputStream = new FileOutputStream(getSettingsFileName());
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
	 * @return true if the user data directory could be created successfully, false
	 *         otherwise.
	 */
	private boolean createApplicationSettingsDir() {

		// Get the application directory
		File scuFolder = getSettingsPropertiesDir();

		// Does the folder exist already?
		if (scuFolder.exists()) {
			return true;
		}

		// Try creating it
		if (!scuFolder.mkdirs()) {
			errorMessage = "Could not create settings directory.";
			return false;
		}
		return true;

	}

	/**
	 * Returns the application data directory
	 *
	 * @return path to the folder where the application properties will be stored
	 * @throws UnsupportedOperationException if the operating system is not one of
	 *                                       Mac OS X or Windows 7.
	 */
	static private File getSettingsPropertiesDir() throws UnsupportedOperationException {

		// Get the common application data folder
		File userDataDir = QueryOS.getOSSpecificUserDataFolder();

		// Append the sub-path common to all platform
		return new File(userDataDir + File.separator + "obit" + File.separator + "AnnotationTool");
	}

	/**
	 * Returns the properties file name with full path
	 *
	 * @return name with full path of the properties files
	 */
	static private File getSettingsFileName() {
		return new File(getSettingsPropertiesDir() + File.separator + "user_settings.xml");
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

	/**
	 * Return the current favorite configuration.
	 *
	 * @return index of current favorite configuration.
	 */
	public String getFavoriteConfigurationName() {
		return listUserSettings.get(favoriteConfigurationSettingsIndex).getSettingValue("ConfigurationName");
	}
}
