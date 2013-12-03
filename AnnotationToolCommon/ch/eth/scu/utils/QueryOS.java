package ch.eth.scu.utils;

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

}
