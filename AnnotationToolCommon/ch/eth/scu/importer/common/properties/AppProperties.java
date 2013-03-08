package ch.eth.scu.importer.common.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Commodity class to manage the AnnotationTool application properties
 * @author Aaron Ponti
 */
public class AppProperties {

	static private final String propertiesVersion = "0.0";

	// Public interface

	/**
	 * Check whether the properties file already exists
	 * @return 	true if the properties file already exists, false otherwise.
	 */	
	static public boolean propertiesFileExists() {
		return getPropertiesFileName().exists();
	}

	/**
	 * Read the properties from disk 
	 * @return a Properties object
	 */
	static public Properties readPropertiesFromFile() {

		// Initialize default Properties
		Properties appProperties = getDefaultProperties();
		
		// Make sure the Properties file exists
		if (!AppProperties.propertiesFileExists()) {
			if (!AppProperties.initializePropertiesFile()) {
				System.err.println("Could not initialize properties file " +
						getPropertiesFileName() + ".");
				return appProperties;
			}
		}
		
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
				return appProperties;
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not read from file. " +
					getPropertiesFileName()  + ". " +
					"Returning default properties.");
			return appProperties;
		}
		
		// Return the Properties object
		return appProperties;
	}
	
	/**
	 * Write the properties to disk 
	 * @return true if the properties were saved successfully, false otherwise
	 */
	static public boolean writePropertiesToFile(String openBISURL, 
			String acqStation, String incomingDir) {

		// Make sure the Properties file exists
		if (!AppProperties.propertiesFileExists()) {
			if (!AppProperties.initializePropertiesFile()) {
				System.err.println("Could not initialize properties file " +
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
				"DatamoverIncomingDir", incomingDir);		

		// Save to file
		try {
			FileOutputStream out = new FileOutputStream(getPropertiesFileName());
			applicationProperties.store(out, 
					"AnnotationTool Default Properties Set");
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
		String OS = System.getProperty("os.name").toUpperCase();
		if (OS.contains("MAC")) {
			applicationDataDir = new File(
					"/Users/Shared/Library/Application Support/");			
		} else if (OS.contains("WINDOWS")) {
			applicationDataDir = new File(
					"C:/Users/All Users/Application Data");
		} else {
			throw new UnsupportedOperationException(
					"Operating system not supported.");
		}
		
		// Append the sub-path common to all platform

        return new File( applicationDataDir +
                File.separator + "scu" + File.separator + "scuimporter");
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
	 */
	static private boolean createApplicationPropertiesDir() {
		
		// Get the application directory
		File scuFolder = getApplicationPropertiesDir();
		
		// Create it if not there
        return scuFolder.exists() || scuFolder.mkdirs();
    }
	
	/**
	 * Initialize the properties file (if it does not exist)
	 * @return 	true if the properties file was generated successfully, 
	 * false otherwise.
	 */	
	static private boolean initializePropertiesFile() {
		
		// Make sure the properties file exists
		if (!AppProperties.propertiesFileExists()) {
			if (!AppProperties.createApplicationPropertiesDir()) {
				return false;
			}
		}
		
		// Get default properties to store
		Properties applicationProperties = getDefaultProperties();
		
		// Save them to file
		try {
			FileOutputStream out = new FileOutputStream(getPropertiesFileName());
			applicationProperties.store(out, "Default Properties Set");
			out.close();
		} catch (IOException e) {
			System.err.println("Could not write to file " + 
					getPropertiesFileName() + ".");
			return false;
		}

		// Return success
		return true;
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
				"DatamoverIncomingDir",
				DefaultProperties.defaultValueForProperty("DatamoverIncomingDir"));
		return applicationProperties;
	}

}

