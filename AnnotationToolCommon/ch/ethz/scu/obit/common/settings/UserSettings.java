package ch.ethz.scu.obit.common.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to store user-specific settings for the Annotation Tool
 * 
 * This class is only visible within its package.
 * 
 * @author Aaron Ponti
 */
class UserSettings {

	// String-string map of setting name - values
	protected Map<String, String> settings = new HashMap<String, String>();

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
	 * @param openBISURL openBIS server URL.
	 */
	public UserSettings(String openBISURL) {
		ArrayList<String> names = getSettingsNames();
		for (String name : names) {
			if (name.equals("OpenBISURL")) {
				setSettingValue(name, openBISURL);				
			} else {
				setSettingValue(name, defaultValueForSetting(name));
			}
		}
	}

	/**
	 * Return the openBIS URL for current settings
	 * @return String the openBIS URL. 
	 */
	public String getOpenBISURL() {
		return getSettingValue("OpenBISURL");
	}

	/**
	 * Store a setting
	 * @param name setting name
	 * @param value setting value
	 */
	public void setSettingValue(String name, String value) {
		settings.put(name, value);
	}
	
	/**
	 * Get a setting
	 * @param name setting name
	 * @return value of the setting.
	 */
	public String getSettingValue(String name) {
		return settings.get(name);
	}
	
	/**
	 * Return all settings
	 * @return all settings in a name-value String-String map
	 */
	public Map<String, String> getAllSettings() {
		return settings;
	}

	/**
	 * Return all known settings 
	 * @return an ArrayList<String> with all known setting names
	 */
	public static ArrayList<String> getSettingsNames() {
		
		ArrayList<String> names = new ArrayList<String>();
		
		names.add("OpenBISURL");
		names.add("DefaultOpenBISProject");
		
		return names;
	}

	/**
	 * Return default option for a given setting 
	 * @param name Name of the setting.
	 * @return name String with the default value for a given setting name
	 */		
	public static String defaultValueForSetting(String name) {
	
		// User settings do not currently have defaults
		return "";
	}
}
