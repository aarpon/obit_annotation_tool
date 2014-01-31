package ch.eth.scu.importer.common.settings;

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

import ch.eth.scu.importer.common.version.VersionInfo;
import ch.eth.scu.utils.QueryOS;

/**
 * Commodity class to manage the AnnotationTool user application properties
 * 
 * @author Aaron Ponti
 */
public class UserSettingsManager {

	protected AppSettings appSettings = null;
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
	public UserSettingsManager() {
		this.appSettings = null;
	}
	
	/**
	 * Alternative Constructor
	 */
	public UserSettingsManager(AppSettings appSettings) {
		this.appSettings = appSettings;
	}

	/**
	 * Returns the properties file name with full path
	 * 
	 * @return name with full path of the properties files
	 */
	public ArrayList<String> getSettingsNames() {
		if (appSettings == null) {
			return new ArrayList<String>();
		}
		return AppSettings.getSettingsNames();
	}

	/**
	 * Return the value of the setting for current server.
	 * 
	 * @param name setting name
	 * @return the value of the attribute for current setting.
	 */
	public String getSettingValue(String name) {
		if (appSettings == null) {
			return "";
		}
		return appSettings.getSettingValue(name);
	}

	/**
	 * Return the URL of current openBIS server
	 * 
	 * @return the URL of current openBIS server.
	 */
	public String getServer() {
		if (appSettings == null) {
			return "";
		}
		return appSettings.getOpenBISURL();
	}

	/**
	 * Try reading settings from file. If loading fails, current settings are
	 * left untouched.
	 * 
	 * @return true if the settings were loaded correctly, false otherwise.
	 */
	public boolean load() {

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

		// Now process all children
		NodeList openBISURLNodes = rootNode.getChildNodes();
		
		Node openBISURL = openBISURLNodes.item(0);

		// Create a new AppSettings object
		appSettings = new AppSettings();

		// Get and add the attributes
		NamedNodeMap attrs = openBISURL.getAttributes();

		for (int j = 0; j < attrs.getLength(); j++) {

			// Get attribute name and value
			String name = attrs.item(j).getNodeName();
			String value = attrs.item(j).getNodeValue();

			// Store it
			appSettings.setSettingValue(name, value);
		}

		// Reset error message
		errorMessage = "";

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
	public boolean save() {

		// Check that the settings are set
		if (appSettings == null) {
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
			Element root = document.createElement("AnnotationTool_Properties");
			root.setAttribute("version",
					Integer.toString(VersionInfo.propertiesVersion));

			// Get its properties
			Map<String, String> currentProperties = appSettings.getAllSettings();

			// Create the experiment
			Element element = document.createElement("server");

			// Append all properties as attributes
			for (Map.Entry<String, String> curr : currentProperties.entrySet()) {

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
	 * Create the application data directory
	 * 
	 * @return true if the application data directory could be created
	 *         successfully, false otherwise.
	 * 
	 *         This function might require write access to a restricted system
	 *         folder. It should be used only in code run with admin privileges.
	 */
	private boolean createApplicationSettingsDir() {

		// Get the application directory
		File scuFolder = getSettingsPropertiesDir();

		// Create it if not there
		return scuFolder.exists() || scuFolder.mkdirs();
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
