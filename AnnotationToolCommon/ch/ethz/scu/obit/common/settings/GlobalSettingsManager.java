package ch.ethz.scu.obit.common.settings;

import java.util.ArrayList;

/**
 * Global settings for the Annotation Tool, combining both Application and User
 * settings.
 *
 * This is the favorite entry point for all settings management in the
 * Annotation Tool, since the GlobalSettingsManager takes care of properly
 * managing and synchronizing both Application and User Settings.
 *
 * The GlobalSettingsManager offers read-only access to the applications
 * settings and read-write access to the user settings. There is no explicit
 * save() method for user settings, that are persisted automatically on
 * modification.
 *
 * The UserSettingsManager and UserSettings classes are only visible within
 * their containing package, to avoid exposing user settings management
 * capabilities independently of the Application and Global Settings.
 *
 * In contrast, the AppSettingsManager class can be directly accessed from
 * external packages. This is meant to facilitate the interaction with the
 * application settings by the Annotation Tool Admin app, that only manages them
 * and ignores the User settings.
 *
 * Please notice, that direct access to the application settings through the
 * AppSettingsManager allows to both read applications settings from disk and
 * write them back modified. It is recommended not to use this direct access
 * from the Annotation Tool, since by modifying the application settings without
 * synchronizing with the user settings you can create an inconsistent state.
 * Use the GlobalSettingsManager class instead.
 *
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
		userManager = new UserSettingsManager(appManager.getAllConfigurationNames());

		// If the user has a favorite server, set if as active
		appManager.setActiveConfiguration(userManager.getFavoriteConfigurationName());

	}

	/**
	 * Set the active server.
	 *
	 * @param openBISURL Server URL.
	 */
	public void setActiveConfiguration(String configurationName) {
		appManager.setActiveConfiguration(configurationName);
		userManager.setActiveConfiguration(configurationName);
	}

	/**
	 * Get the name of the active configuration.
	 *
	 * @return name of the active configuration.
	 */
	public AppSettings getActiveConfiguration() {
		return appManager.getActiveConfiguration();
	}

	/**
	 * Get all configurations
	 *
	 * @return List of all configurations.
	 */
	public ArrayList<AppSettings> getAllConfigurations() {
		return appManager.getAllConfigurations();
	}

	/**
	 * Get all configuration names
	 *
	 * @return List of all configuration names.
	 */
	public ArrayList<String> getAllConfigurationNames() {
		return appManager.getAllConfigurationNames();
	}

	/**
	 * Returns whether the client should accept self-signed certificates from the
	 * server.
	 *
	 * This is an Application Setting.
	 *
	 * @return "yes" if the client should except and accept self-signed
	 *         certificates, or "no" otherwise.
	 */
	public String acceptSelfSignedCertificates() {
		return appManager.getSettingValue("AcceptSelfSignedCertificates");
	}

	/**
	 * Get user data root directory (without user subfolder).
	 *
	 * This is an Application Setting.
	 *
	 * @return user data root directory.
	 */
	public String getUserDataRootDir() {
		return appManager.getSettingValue("UserDataDir");
	}

	/**
	 * Get openBIS server URL.
	 *
	 * This is an Application Setting.
	 *
	 * @return openBIS server URL.
	 */
	public String getOpenBISURL() {
		return appManager.getSettingValue("OpenBISURL");
	}

	/**
	 * Get Datamover incoming directory.
	 *
	 * This is an Application Setting.
	 *
	 * @return Datamover incoming directory.
	 */
	public String getDatamoverIncomingDir() {
		return appManager.getSettingValue("DatamoverIncomingDir");
	}

	/**
	 * Return the acquisition station.
	 *
	 * This is an Application Setting.
	 *
	 * @return the acquisition station.
	 */
	public String getAcquisitionStation() {
		return appManager.getSettingValue("AcquisitionStation");
	}

	/**
	 * Return the acquisition station human-friendly name.
	 *
	 * This is an Application Setting.
	 *
	 * @return the acquisition station.
	 */
	public String getAcquisitionStationFriendlyHostName() {
		return appManager.getSettingValue("HumanFriendlyHostName");
	}

	/**
	 * Returns whether the marker file to trigger registration shold be created
	 * after moving the data to the Datamover incoming folder.
	 *
	 * This is an Application Setting.
	 *
	 * @return "yes" if the marker should be created, or "no" otherwise.
	 */
	public String createMarkerFileInDatamoverIncomingFolder() {
		return appManager.getSettingValue("CreateMarkerFile");
	}

	/**
	 * Set the user's favorite configuration..
	 *
	 * The configuration must be one of those defined by the Annotation Tool Admin
	 * application and stored in the application settings.
	 *
	 * This is a User Setting.
	 *
	 * @param configurationName name of the favorite configuration.
	 * @return true if the favorite configuration could be set successfully, false
	 *         otherwise.
	 */
	public boolean setFavoriteConfiguration(String configurationName) {
		return userManager.storeFavoriteConfiguration(configurationName);
	}

	/**
	 * Get the user's favorite configuration.
	 *
	 * This is a User Setting.
	 *
	 * @return the URL of the server to use.
	 */
	public String getFavoriteConfigurationName() {
		return userManager.getFavoriteConfigurationName();
	}

	/**
	 * Set the default project for the active server.
	 *
	 * Please notice that the validation of the passed project identifier must be
	 * done externally. The GlobalSettingsManager, UserSettingsManager and
	 * UserSettings classes do not attempt to check the passed identifier against
	 * openBIS.
	 *
	 * This is a User Setting.
	 *
	 * @param project openBIS identifier of the project.
	 * @return true if the project could be set successfully, false otherwise.
	 */
	public boolean setDefaultProject(String project) {
		return userManager.storeDefaultProject(project);
	}

	/**
	 * Get the default project for the activer server.
	 *
	 * This is a User Setting.
	 *
	 * @return openBIS identifier of the project.
	 */
	public String getDefaultProject() {
		return userManager.getDefaultProject();
	}

	/**
	 * Checks that the configuration files are valid and ready to use.
	 *
	 * @return true if the configuration is valid, false otherwise.
	 * @throws Exception if the configuration is not valid for some reason. The
	 *                   exception message contains the actual explanation and is
	 *                   meant to be shown to the user (e.g. in a dialog).
	 */
	static public boolean isConfigurationValid() throws Exception {

		// Check whether the application has been set up already.
		// If not, we inform the user and quit.
		if (!AppSettingsManager.settingsFileExists()) {
			throw (new Exception("The application has not ben configured yet.\n"
					+ "Please ask an administrator to do it for you.\n\n" + "The application will close now."));
		}

		// Read the application settings
		AppSettingsManager manager = new AppSettingsManager();

		// Check
		if (!manager.isFileRead()) {
			throw (new Exception(
					"The application settings could not be read.\n" + "Please ask an administrator to re-configure the "
							+ "application.\n\n" + "The application will close now."));
		}

		if (!manager.isFileCurrent()) {
			throw (new Exception(
					"The application settings are obsolete.\n" + "Please ask an administrator to re-configure the "
							+ "application.\n\n" + "The application will close now."));
		}

		if (!manager.isFileValid()) {
			throw (new Exception("The application settings are not valid.\n"
					+ "Please ask an administrator to reconfigure the application.\n\n"
					+ "The application will close now."));
		}

		return true;
	}

}
