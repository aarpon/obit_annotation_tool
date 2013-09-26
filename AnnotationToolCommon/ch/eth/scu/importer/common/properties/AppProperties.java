package ch.eth.scu.importer.common.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import ch.eth.scu.utils.QueryOS;

/**
 * Commodity class to manage the AnnotationTool application properties
 * @author Aaron Ponti
 */
public class AppProperties {

	static private final String propertiesVersion = "0.1";

	// Public interface

	/**
	 * Check whether the properties file already exists
	 * @return 	true if the properties file already exists, false
	 * otherwise.
	 */	
	static public boolean propertiesFileExists() {
		return getPropertiesFileName().exists();
	}

	/**
	 * Check whether the Properties file is at current version
	 * @param appProperties Properties object
	 * @return true if the Properties file is at current version,
	 * false otherwise.
	 * 
	 * Please note that this function will return true if the file does
	 * not exist, since a new AppProperties object will be created and
	 * tested for its version. 
	 * 
	 * @see propertiesFileExists
	 */
	static public boolean isPropertiesFileVersionCurrent(Properties appProperties) {
		if (appProperties == null) { return false; }
		return appProperties.getProperty("PropertyFileVersion")
				.equals(propertiesVersion);
	}

	/**
	 * Check whether all Properties in the file are set. 
	 * @return true if all Properties in the file are set,
	 * false otherwise.
	 */
	static public boolean areAllPropertiesSet(Properties appProperties) {
		if (appProperties == null) { return false; }
		Set<Object> keys = appProperties.keySet(); 
		for (Object k : keys) {
			String key = (String) k;
			if (appProperties.getProperty(key).equals("")) {
				return false; 
			}
		}
		return true;
	}
	
	/**
	 * Read the properties from disk 
	 * @return a Properties object or null if it could not be loaded
	 */
	static public Properties readPropertiesFromFile() {

		// Make sure the Properties file exists
		if (!AppProperties.propertiesFileExists()) {
			return null;
		}

		// Instantiate Properties object
		Properties appProperties = new Properties();

		// Open file
		FileInputStream in;
		try {
			in = new FileInputStream(getPropertiesFileName());
			try {
				appProperties.load(in);
				in.close();
			} catch (IOException e) {
				System.err.println("Could not read from file. " +
						getPropertiesFileName() + ". " +
						"Returning default properties.");
				return null;
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not read from file. " +
					getPropertiesFileName()  + ". " +
					"Returning default properties.");
			return null;
		}

		// Return the Properties object
		return appProperties;
	}
	
	/**
	 * Write the properties to disk 
	 * @return true if the properties were saved successfully, false otherwise
	 * 
	 * This function might require write access to a restricted system
	 * folder. It should be used only in code run with admin privileges.
	 */
	static public boolean writePropertiesToFile(String openBISURL, 
			String acqStation, String userDataDir, String incomingDir) {

		// Make sure the Properties file exists
		if (!AppProperties.propertiesFileExists()) {
			if (AppProperties.initializePropertiesFile() == null) {
				System.err.println(
						"Could not initialize properties file " +
						getPropertiesFileName() + ".");				
			}
		}
		
		// Set properties
		Properties applicationProperties = getDefaultProperties();
		applicationProperties.setProperty(
				"PropertyFileVersion", propertiesVersion );
		applicationProperties.setProperty(
				"OpenBISURL", openBISURL);
		applicationProperties.setProperty(
				"AcquisitionStation", acqStation);
		applicationProperties.setProperty(
				"UserDataDir", userDataDir);	
		applicationProperties.setProperty(
				"DatamoverIncomingDir", incomingDir);		

		// Save to file
		try {
			FileOutputStream out = new FileOutputStream(getPropertiesFileName());
			applicationProperties.store(out, 
					"AnnotationTool Properties Set");
			out.close();
		} catch (IOException e) {
			System.err.println("Could not write to file " +
					getPropertiesFileName() + ".");				
			return false;
		}
		
		// Return success
		return true;
	}
		
	// Private methods
	
	/**
	 * Returns the application data directory
	 * @return 	path to the folder where the application properties will
	 * 			be stored 
	 * @throws 	UnsupportedOperationException if the operating system is not one 
	 * 			of Mac OS X or Windows 7.
	 */
	static private File getApplicationPropertiesDir() 
			throws UnsupportedOperationException {

		// Initialize the applicationDir variable
		File applicationDataDir;
		
		// Build the path as a function of the operating system
		if (QueryOS.isMac()) {
			applicationDataDir = new File(
					"/Users/Shared/Library/Application Support/");			
		} else if (QueryOS.isWindows()) {
			applicationDataDir = new File(
					"C:/Users/All Users/Application Data");
		} else {
			throw new UnsupportedOperationException(
					"Operating system not supported.");
		}
		
		// Append the sub-path common to all platform

        return new File( applicationDataDir +
                File.separator + "scu" + File.separator + "obit");
	}
	
	/**
	 * Returns the properties file name with full path
	 * @return 	name with full path of the properties files
	 */
	static private File getPropertiesFileName() {

		return new File( getApplicationPropertiesDir() + 
				File.separator + "properties.cfg" );	
	}

	/**
	 * Create the application data directory
	 * @return 	true if the application data directory could be created
	 * successfully, false otherwise.
	 * 
	 * This function might require write access to a restricted system
	 * folder. It should be used only in code run with admin privileges.
	 */
	static private boolean createApplicationPropertiesDir() {
		
		// Get the application directory
		File scuFolder = getApplicationPropertiesDir();
		
		// Create it if not there
        return scuFolder.exists() || scuFolder.mkdirs();
    }
	
	/**
	 * Initialize the properties file (if it does not exist)
	 * @return 	the generated Properties object or null if it could not
	 * be saved to disk.
	 * 
	 * This function might require write access to a restricted system
	 * folder. It should be used only in code run with admin privileges.

	 */	
	static public Properties initializePropertiesFile() {
		
		// Make sure the properties file exists
		if (!AppProperties.propertiesFileExists()) {
			if (!AppProperties.createApplicationPropertiesDir()) {
				return null;
			}
		}
		
		// Get default properties to store
		Properties applicationProperties = getDefaultProperties();
		
		// Save them to file
		try {
			FileOutputStream out = new FileOutputStream(getPropertiesFileName());
			applicationProperties.store(out, "AnnotationTool Properties Set");
			out.close();
		} catch (IOException e) {
			System.err.println("Could not write to file " +
					getPropertiesFileName() + ".");	
			return null;
		}

		// Return success
		return applicationProperties;
	}
	
	/**
	 * Return default properties 
	 * @return 	Properties with default values.
	 */		
	private static Properties getDefaultProperties() {

		// Create and return default properties
		Properties applicationProperties = new Properties();
		applicationProperties.setProperty(
				"PropertyFileVersion", propertiesVersion );
		applicationProperties.setProperty(
				"OpenBISURL", 
				DefaultProperties.defaultValueForProperty("OpenBISURL"));		
		applicationProperties.setProperty(
				"AcquisitionStation",
				DefaultProperties.defaultValueForProperty("AcquisitionStation"));
		applicationProperties.setProperty(
				"UserDataDir",
				DefaultProperties.defaultValueForProperty("UserDataDir"));
		applicationProperties.setProperty(
				"DatamoverIncomingDir",
				DefaultProperties.defaultValueForProperty("DatamoverIncomingDir"));
		return applicationProperties;
	}

}

