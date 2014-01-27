package ch.eth.scu.importer.common.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to store Annotation Tool settings for one specific openBIS server
 * @author Aaron Ponti
 */
public class AppSettings {

	// String-string map of setting name - values
	protected Map<String, String> settings = new HashMap<String, String>();

	/**
	 * Constructor
	 */
	public AppSettings() {
		ArrayList<String> names = getSettingsNames();
		for (String name : names) {
			setSettingValue(name, defaultValueForSetting(name));
		}
	}

	/**
	 * Constructor
	 * @param openBISURL: URL of the openBIS server  
	 */
	public AppSettings(String openBISURL) {
		ArrayList<String> names = getSettingsNames();
		for (String name : names) {
			if (name.equals("OpenBISURL")) {
				setSettingValue("OpenBISURL", openBISURL);
			} else {
				setSettingValue(name, defaultValueForSetting(name));
			}
		}
	}

	/**
	 * Get the openBIS URL
	 * @return the openBIS URL
	 */
	public String getOpenBISURL() {
		if (settings.containsKey("OpenBISURL")) {
			return settings.get("OpenBISURL");
		} else {
			return AppSettings.defaultValueForSetting("OpenBISURL");
		}
	}

	/**
	 * Sets the openBIS URL
	 * @param openBISURL: openBIS URL
	 */
	public void setOpenBISURL(String openBISURL) {
		setSettingValue("OpenBISURL", openBISURL);
	}

	/**
	 * Store a setting
	 * @param name: setting name
	 * @param value: setting value
	 */
	public void setSettingValue(String name, String value) {
		settings.put(name, value);
	}
	
	/**
	 * Get a setting
	 * @param name: setting name
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
	 * Returns the possible values for a given setting
	 * @param name Name of the setting
	 * @return list of values (Strings)
	 */
	static public ArrayList<String> possibleValuesForSetting(String name) {
		
		ArrayList<String> options = new ArrayList<String>();
		
		if (name.equals("OpenBISURL")) {
			options.add("https://localhost:8443/openbis");
		} else if (name.equals("AcceptSelfSignedCertificates")) {
			options.add("no");
			options.add("yes");
		} else if (name.equals("AcquisitionStation")) {
			options.add("BD LSRFortessa cell analyzer");
			options.add("BD FACSAria III cell sorter");
			options.add("Generic light microscope");
		} else if (name.equals("UserDataDir")) {
			options.add("");			
		} else if (name.equals("DatamoverIncomingDir")) {
			options.add("");
		} else {
			throw (new IllegalArgumentException());
		}
		
		return options;
	}
	
	/**
	 * Return default option for a given setting 
	 * @return name String with the default value for a given setting name
	 */		
	public static String defaultValueForSetting(String name) {
	
		// Get all values for the given setting
		ArrayList<String> options = possibleValuesForSetting(name);
		
		// The first one is the default. The options ArrayList is guaranteed
		// to have at least one element.
		return options.get(0);
	}

	/**
	 * Return all known settings 
	 * @return an ArrayList<String> with all known setting names
	 */
	public static ArrayList<String> getSettingsNames() {
		
		ArrayList<String> names = new ArrayList<String>();
		
		names.add("OpenBISURL");
		names.add("AcceptSelfSignedCertificates");
		names.add("AcquisitionStation");
		names.add("UserDataDir");
		names.add("DatamoverIncomingDir");
		
		return names;
	}
	
	/**
	 * Check whether all settings are set
	 * @return true if all settings are set, false otherwise.
	 */
	public boolean allSet() {
		ArrayList<String> names = getSettingsNames();
		for (String name : names) {
			if (getSettingValue(name).equals("")) {
				return false;
			}
		}
		return true;
	}
}
