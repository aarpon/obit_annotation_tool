package ch.ethz.scu.obit.common.settings;

import java.net.UnknownHostException;
import java.util.ArrayList;

import ch.ethz.scu.obit.common.utils.QueryOS;

/**
 * Class to store Annotation Tool settings for one specific openBIS server
 *
 * This class is only visible within its package.
 *
 * @author Aaron Ponti
 */
public class AppSettings extends AbstractSettings {

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
	 * Alternative constructor
	 */
	public AppSettings(String configurationName) {
		this();
		setSettingValue("ConfigurationName", configurationName);
	}

	/**
	 * Get the configuration name
	 *
	 * @return the configuration name
	 */
	@Override
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
	@Override
	public void setConfigurationName(String configurationName) {
		setSettingValue("ConfigurationName", configurationName);
	}

	/**
	 * Returns the possible values for a given setting
	 *
	 * @param name Name of the setting
	 * @return list of values (Strings)
	 */
	public static ArrayList<String> possibleValuesForSetting(String name) {

		ArrayList<String> options = new ArrayList<String>();

		if (name.equals("OpenBISURL")) {
			options.add("https://localhost:8443/openbis");
		} else if (name.equals("AcceptSelfSignedCertificates")) {
			options.add("no");
			options.add("yes");
		} else if (name.equals("AcquisitionStation")) {
			options.add("Flow cytometry");
			options.add("Microscopy");
		} else if (name.equals("HumanFriendlyHostName")) {
			try {
				options.add(QueryOS.getHostName());
			} catch (UnknownHostException e) {
				options.add("Unknown");
			}
		} else if (name.equals("UserDataDir")) {
			options.add("");
		} else if (name.equals("DatamoverIncomingDir")) {
			options.add("");
		} else if (name.equals("CreateMarkerFile")) {
			options.add("no");
			options.add("yes");
		} else if (name.equals("ConfigurationName")) {
			options.add("Default");
		} else {
			throw (new IllegalArgumentException());
		}

		return options;
	}

	/**
	 * Returns the description for the specified acquisition station
	 *
	 * @param name Name of the acquisition station
	 * @return description of acquisition station
	 */
	public static String getAcqStationDescription(String name) {

		if (name.equals("Flow cytometry")) {
			return "Cell analyzers and sorters " + "(FCS 3.0/3.1)";
		} else if (name.equals("Microscopy")) {
			return "Light microscopes (Open Microscopy Environment Bio-Formats compatible)";
		} else {
			throw (new IllegalArgumentException("Unknown acquisition station."));
		}
	}

	/**
	 * Return default option for a given setting
	 *
	 * @param name String with the default value for a given setting name
	 * @return settings value
	 */
	public static String defaultValueForSetting(String name) {

		// Get all values for the given setting
		ArrayList<String> options = AppSettings.possibleValuesForSetting(name);

		// The first one is the default. The options ArrayList is guaranteed
		// to have at least one element.
		return options.get(0);
	}

	/**
	 * Return all known settings
	 *
	 * @return an {@code ArrayList<String>} with all known setting names
	 */
	public static ArrayList<String> getSettingsNames() {

		ArrayList<String> names = new ArrayList<String>();

		names.add("ConfigurationName");
		names.add("OpenBISURL");
		names.add("AcceptSelfSignedCertificates");
		names.add("AcquisitionStation");
		names.add("HumanFriendlyHostName");
		names.add("UserDataDir");
		names.add("DatamoverIncomingDir");
		names.add("CreateMarkerFile");

		return names;
	}

	/**
	 * Check whether all settings are set
	 *
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

	/**
	 * Returns true if a certain setting can only occur once in a collection of
	 * settings
	 *
	 * @param name Name of the setting
	 * @return true if the setting must be unique, false otherwise
	 *
	 * @see AppSettingsManager
	 */
	static public boolean mustBeUnique(String name) {

		if (name.equals("OpenBISURL")) {
			return false;
		} else if (name.equals("AcceptSelfSignedCertificates")) {
			return false;
		} else if (name.equals("AcquisitionStation")) {
			return false;
		} else if (name.equals("HumanFriendlyHostName")) {
			return false;
		} else if (name.equals("UserDataDir")) {
			return false;
		} else if (name.equals("DatamoverIncomingDir")) {
			return false;
		} else if (name.equals("CreateMarkerFile")) {
			return false;
		} else if (name.equals("ConfigurationName")) {
			return true;
		} else {
			throw (new IllegalArgumentException());
		}
	}
}
