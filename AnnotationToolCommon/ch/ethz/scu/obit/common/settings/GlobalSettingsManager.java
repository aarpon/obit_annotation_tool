package ch.ethz.scu.obit.common.settings;

import java.util.ArrayList;

/**
 * Global settings for the Annotation Tool, combining both Application and User settings.
 * @author Aaron Ponti
 *
 */
public class GlobalSettingsManager {

	protected String currentOpenBISURL;
	protected AppSettingsManager appManager = null;
	protected UserSettingsManager userManager = null;
	
	/**
	 * Constructor
	 */
	public GlobalSettingsManager() {
		
		// Load applications settings
		appManager = new AppSettingsManager();
		
		// Load user settings
		userManager = new UserSettingsManager(appManager.getAllServers());
		
		// If the user has a favorite server, set if as active
		appManager.setActiveServer(userManager.getFavoriteOpenBISServer());

	}
	
	/**
	 * Set the active server.
	 * @param openBISURL Server URL.
	 */
	public void setActiveServer(String openBISURL) {
		appManager.setActiveServer(openBISURL);
		userManager.setActiveServer(openBISURL);
	}
	
	/**
	 * Get the URL of the active server.
	 * @return
	 */
	public String getActiveServer() {
		return appManager.getActiveServer();
	}
	
	
	/**
	 * Get all server URLs.
	 * @return List of all server URLs.
	 */
	public ArrayList<String> getAllServers() {
		return appManager.getAllServers();
	}
		
	/**
	 * Returns whether the client should accept self-signed certificates from the server.
	 * @return "yes" if the client should except and accept self-signed certificates, or "no" otherwise.
	 */
	public String acceptSelfSignedCertificates() {
		return appManager.getSettingValue("AcceptSelfSignedCertificates");
	}
	
	/**
	 * Get user data root directory (without user subfolder).
	 * @return user data root directory. 
	 */
	public String getUserDataRootDir() {
		return appManager.getSettingValue("UserDataDir");
	}

	/**
	 * Get Datamover incoming directory.
	 * @return Datamover incoming directory. 
	 */
	public String getDatamoverIncomingDir() {
		return appManager.getSettingValue("DatamoverIncomingDir");
	}

	/**
	 * Return the acquisition station.
	 * @return the acquisition station.
	 */
	public String getAcquisitionStation() {
		return appManager.getSettingValue("AcquisitionStation");
	}

	/**
	 * Checks that the configuration files are valid and ready to use.
	 * @return
	 * @throws Exception 
	 */
	static public boolean isConfigurationValid() throws Exception {
		
		// Check whether the application has been set up already.
		// If not, we inform the user and quit.
		if (!AppSettingsManager.settingsFileExists()) {
			throw (new Exception(
					"The application has not ben configured yet.\n" +
					"Please ask an administrator to do it for you.\n\n" +
					"The application will close now.")
			);
		}
		
		// Read the application settings
		AppSettingsManager manager = new AppSettingsManager();

		// Check
		if (!manager.isFileRead()) {
			throw (new Exception(
				    "The application settings could not be read.\n" +
			        "Please ask an administrator to re-configure the " +
			        "application.\n\n" +
			        "The application will close now.")
			);
		}		
		
		if (!manager.isFileCurrent()) {
			throw (new Exception(
					"The application settings are obsolete.\n" +
					"Please ask an administrator to re-configure the " +
					"application.\n\n" +
					"The application will close now.")
			);
		}		

		if (!manager.isFileValid()) {
			throw (new Exception(
					"The application settings are not valid.\n" +
					"Please ask an administrator to reconfigure the application.\n\n" +
					"The application will close now.")
			);
		}

		return true;
	}

}
