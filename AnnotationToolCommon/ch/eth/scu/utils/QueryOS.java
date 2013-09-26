package ch.eth.scu.utils;

public class QueryOS {

	/**
	 * Check if the application is running on Mac OS X
	 * @return true if on Mac OS X, false otherwise
	 */
	static public boolean isMac() {
		String OS = System.getProperty("os.name").toUpperCase();
		if (OS.contains("MAC")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the application is running on Windows
	 * @return true if on Windows, false otherwise
	 */
	static public boolean isWindows() {
		String OS = System.getProperty("os.name").toUpperCase();
		if (OS.contains("WINDOWS")) {
			return true;
		}
		return false;
	}

}
