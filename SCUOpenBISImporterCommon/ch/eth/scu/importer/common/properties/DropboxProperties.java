package ch.eth.scu.importer.common.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * DropBoxProperties reads and writes properties to be used in the dropbox script
 * @author Aaron Ponti
 */
public class DropboxProperties {

	private Properties dropboxProperties;
	
	public DropboxProperties() {
	
		// Create an empty Properties object
		dropboxProperties = new Properties();
	
	}
	
	/**
	 * Read the properties from a specified file
	 * @return a Properties object
	 */
	public Properties readPropertiesFromFile(File file) {
		
		// Open file
		FileInputStream in;
		try {
			in = new FileInputStream(file);
			try {
				dropboxProperties = new Properties();
				dropboxProperties.load(in);
				in.close();
			} catch (IOException e) {
				System.err.println("Could not read from specified file.");
				return dropboxProperties;
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not read from specified file.");
			return dropboxProperties;
		}
		
		// Return the Properties object
		return dropboxProperties;
	}

	/**
	 * Add a String-String key-value pair to the properties
	 */
	public void add(String key, String value) {
		dropboxProperties.setProperty(key, value);
	}
	
	/**
	 * Write the stored properties to a specified file
	 * @return true if writing the file was successful, false otherwise.
	 */
	public boolean writePropertiesToFile(File file) {
		
		try {
			FileOutputStream out = new FileOutputStream(file);
			dropboxProperties.store(out, "Dropbox properties");
			out.close();
		} catch (IOException e) {
			System.err.println("Could not write to specified file.");
			return false;
		}
		
		// Return success
		return true;
	}	
}
