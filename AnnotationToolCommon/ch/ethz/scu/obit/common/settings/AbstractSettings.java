package ch.ethz.scu.obit.common.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSettings {

	// String-string map of setting name - values
	protected Map<String, String> settings = new HashMap<String, String>();

	/**
	 * Constructor
	 */
	protected AbstractSettings() {
		// The extending classes must use their own implementations
		// of getSettingsNames() and defaultValueForSetting().
		ArrayList<String> names = getSettingsNames();
		for (String name : names) {
			setSettingValue(name, defaultValueForSetting(name));
		}
	}

	/**
	 * Alternative constructor
	 */
	protected AbstractSettings(String configurationName) {
		this();
		setSettingValue("ConfigurationName", configurationName);
	}

	/**
	 * Return all settings
	 *
	 * @return all settings in a name-value String-String map
	 */
	public Map<String, String> getAllSettings() {
		return settings;
	}

	/**
	 * Store a setting
	 *
	 * @param name  setting name
	 * @param value setting value
	 */
	public void setSettingValue(String name, String value) {
		settings.put(name, value);
	}

	/**
	 * Get the configuration name
	 *
	 * @return the configuration name
	 */
	public String getConfigurationName() {
		if (settings.containsKey("ConfigurationName")) {
			return settings.get("ConfigurationName");
		} else {
			return defaultValueForSetting("ConfigurationName");
		}
	}

	/**
	 * Sets the configuration name
	 *
	 * @param configurationName Configuration name
	 */
	public void setConfigurationName(String configurationName) {
		setSettingValue("ConfigurationName", configurationName);
	}

	/**
	 * Get a setting
	 *
	 * @param name setting name
	 * @return value of the setting.
	 */
	public String getSettingValue(String name) {
		return settings.get(name);
	}

	/**
	 * Return all known settings.
	 *
	 * @return an {@code ArrayList<String>} with all known setting names
	 */
	public static ArrayList<String> getSettingsNames() {
		return new ArrayList<String>();
	}

	/**
	 * Return default option for a given setting
	 *
	 * @param name Name of the setting.
	 * @return name String with the default value for a given setting name
	 */
	public static String defaultValueForSetting(String name) {
		return "";
	}
}
