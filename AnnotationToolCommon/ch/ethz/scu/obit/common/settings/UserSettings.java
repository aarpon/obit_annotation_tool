package ch.ethz.scu.obit.common.settings;

import java.util.ArrayList;

/**
 * Class to store user-specific settings for the Annotation Tool
 *
 * This class is only visible within its package.
 *
 * @author Aaron Ponti
 */
public class UserSettings extends AbstractSettings {

	/**
	 * Constructor
	 */
	public UserSettings() {
		ArrayList<String> names = getSettingsNames();
		for (String name : names) {
			setSettingValue(name, defaultValueForSetting(name));
		}
	}

	/**
	 * Alternative constructor
	 */
	public UserSettings(String configurationName) {
		this();
		setSettingValue("ConfigurationName", configurationName);
	}

	/**
	 * Return all known settings
	 *
	 * @return an {@code ArrayList<String>} with all known setting names
	 */
	public static ArrayList<String> getSettingsNames() {
		ArrayList<String> names = new ArrayList<String>();
		names.add("ConfigurationName");
		names.add("DefaultOpenBISProject");
		return names;
	}

	/**
	 * Return default option for a given setting
	 *
	 * @param name Name of the setting.
	 * @return name String with the default value for a given setting name
	 */
	public static String defaultValueForSetting(String name) {
		// User settings do not currently have defaults
		return "";
	}
}
