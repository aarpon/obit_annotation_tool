package ch.eth.scu.importer.common.properties;

import java.util.Observable;
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
public class DropboxProperties extends Observable {

	private File dropboxIncomingFile;

	private Properties dropboxProperties;
	
	public DropboxProperties() {
	
		// Read the application properties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		
		// TODO Check that the folder exists!
		
		// Store the folder
		this.dropboxIncomingFile = new File(
				appProperties.getProperty("DatamoverIncomingDir") +
				File.separator + "dropbox.cfg");
		
		// Create an empty Properties object
		dropboxProperties = new Properties();
	
	}
	
	/**
	 * Read the dropbox properties file
	 * @return a Properties object
	 */
	public Properties readPropertiesFromFile() {
		
		// Open file
		FileInputStream in;
		try {
			in = new FileInputStream(dropboxIncomingFile);
			try {
				dropboxProperties = new Properties();
				dropboxProperties.load(in);
				in.close();
			} catch (IOException e) {
				System.err.println("Could not read from dropbox properties file.");
				return dropboxProperties;
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not read from dropbox properties file.");
			return dropboxProperties;
		}
		
		// Return the Properties object
		return dropboxProperties;
	}

	/**
	 * Add a String-String key-value pair to the properties
	 */
	public void add(String key, String value) {
		// Set the property
		dropboxProperties.setProperty(key, value);
		
		// Notify observers that a key was set 
		setChanged();
		notifyObservers(key);
	}

	/**
	 * Add a String-String key-value pair to the properties
	 * @return String containing the value, or null if the key-value pair does
	 * not exist.
	 */
	public String get(String key) {
		return dropboxProperties.getProperty(key);
	}
	
	/**
	 * Write the stored properties to the dropbox properties file
	 * @return true if writing the file was successful, false otherwise.
	 */
	public boolean writePropertiesToFile() {
		
		try {
			FileOutputStream out = new FileOutputStream(dropboxIncomingFile);
			dropboxProperties.store(out, "Dropbox properties");
			out.close();
		} catch (IOException e) {
			System.err.println("Could not write to dropbox properties file.");
			return false;
		}
		
		// Return success
		return true;
	}	
}
