package ch.eth.scu.importer.at.datamover;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardCopyOption.*;

import javax.swing.JOptionPane;

import ch.eth.scu.importer.common.properties.AppProperties;

import java.security.SecureRandom;
import java.util.Properties;
import java.math.BigInteger;

/**
 * Moves the user folder from the data folder to datamover incoming folder.
 * @author Aaron Ponti
 *
 */
public class ATDataMover {

	// Source and target directories
	File sourceDir;
	File targetDir;
	
	// Secure random number generator
	private SecureRandom random;
	
	/**
	 * Constructor
	 * @param userName openBIS user name 
	 */
	public ATDataMover(String userName) {

		// Initialize random number generator
		random = new SecureRandom();

		// Get the folder from the properties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		if (appProperties == null ) {
			JOptionPane.showMessageDialog(null,
					"Could not read application settings!\n" +
			"Please contact your administrator. The application\n" +
			"will now exit!",
			"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		// Get the user data dir
		sourceDir = new File(appProperties.getProperty("UserDataDir") +
				File.separator + userName);
		if (!sourceDir.isDirectory()) {
			throw new IllegalArgumentException("sourceDir must be a directory!");
		}
		
		// Get the datamover incoming dir
		targetDir = new File(appProperties.getProperty("DatamoverIncomingDir"));
		if (!targetDir.isDirectory()) {
			throw new IllegalArgumentException("sourceDir must be a directory!");
		}
		
	}

	// Move sourceDir to targetDir/{uniqueID}/
	public void move() {
		
		// Get a unique ID
		String uniqueID = new BigInteger(130, random).toString(32);

		// Create a subfolder in targetDir with the uniqueID as name
		File subFolder = new File(targetDir + File.separator + uniqueID);
		if (!createDir(subFolder)) {
			JOptionPane.showMessageDialog(null,
					"Failed creating " + subFolder + "!\n" +
			"Please contact your administrator. The application\n" +
			"will now exit!",
			"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);

		}
		
		// Full target path
		File fullTarget = new File(subFolder + File.separator + 
				sourceDir.getName());
		
		// Move
		try {
			Files.move(sourceDir.toPath(), fullTarget.toPath(), ATOMIC_MOVE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Failed copying " + sourceDir + " to " + 
			fullTarget + "!\n" +
			"Please contact your administrator. The application\n" +
			"will now exit!",
			"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		// Re-create user folder
		if (!createDir(sourceDir)) {
			JOptionPane.showMessageDialog(null,
					"Failed re-creating user folder!\n" +
			"Please contact your administrator. The application\n" +
			"will now exit!",
			"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);

		}
	}

	/**
	 * Create specified directory
	 * @param dir full directory path
	 * @return true if the directory could be created successfully (or
	 * existed already), false otherwise
	 */
	private boolean createDir(File dir) {
		
		// Does the directory exist already?
		if (dir.exists()) {
			return true;
		}
		
		// Try creating it
		boolean success = true;
		try {
			if (!dir.mkdirs()) {
				success = false;
			}
		} catch (Exception e) {
			success = false;
		}
		
		return success;
	}

}
