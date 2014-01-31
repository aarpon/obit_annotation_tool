package ch.eth.scu.utils;

import java.io.File;

public class QueryOS {

	/**
	 * Check if the application is running on Mac OS X
	 * @return true if on Mac OS X, false otherwise
	 */
	static public boolean isMac() {
		String OS = System.getProperty("os.name").toUpperCase();
        return OS.contains("MAC");
    }
	
	/**
	 * Check if the application is running on Windows
	 * @return true if on Windows, false otherwise
	 */
	static public boolean isWindows() {
		String OS = System.getProperty("os.name").toUpperCase();
        return OS.contains("WINDOWS");
    }

	/**
	 * Get the application data folder specific for current OS
	 * @return full path to the application data folder
	 */
	static public File getOSSpecificAppDataFolder() {
	
		// Initialize the applicationDir variable
		File applicationDataDir;
				
		// Build the path as a function of the operating system
		if (QueryOS.isMac()) {
			applicationDataDir = new File(
					"/Users/Shared/Library/Application Support/");			
		} else if (QueryOS.isWindows()) {
			applicationDataDir = new File(System.getenv("ALLUSERSPROFILE"));
		} else {
			throw new UnsupportedOperationException(
					"Operating system not supported.");
		}
		
		return applicationDataDir;

	}

	/**
	 * Get the user data folder specific for current OS
	 * @return full path to the user data folder
	 */
	static public File getOSSpecificUserDataFolder() {
		
		// Initialize the userDir variable
		File userDir;
				
		// Build the path as a function of the operating system
		if (QueryOS.isMac()) {
			userDir =  new File("/Users/"
					+ System.getProperty("user.name")
					+ "/Library/Application Support/");		
		} else if (QueryOS.isWindows()) {
			userDir = new File(System.getenv("LOCALAPPDATA"));
		} else {
			throw new UnsupportedOperationException(
					"Operating system not supported.");
		}
		
		return userDir;
	}
	
}
