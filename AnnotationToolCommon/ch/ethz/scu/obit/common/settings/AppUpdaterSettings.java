package ch.ethz.scu.obit.common.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to store Annotation Tool Updater settings
 * 
 * This class is visible outside of its package but is used exclusively
 * by the AnnotationToolUpdater. 
 * 
 * @author Aaron Ponti
 */
class AppUpdaterSettings {

    protected String errorMessage = "";
    
	// String-string map of setting name - values
	protected Map<String, String> settings = new HashMap<String, String>();

	/**
	 * Constructor
	 */
	public AppUpdaterSettings() {
		ArrayList<String> names = getSettingsNames();
		for (String name : names) {
			setSettingValue(name, defaultValueForSetting(name));
		}
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
	 * @return value for the requested name
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
		
		if (name.equals("useProxyServer")) {
			options.add("0");
			options.add("1");
		} else if (name.equals("proxyServerName")) {
			options.add("");
		} else if (name.equals("proxyServerPort")) {
			options.add("");
		} else {
			throw (new IllegalArgumentException());
		}
		
		return options;
	}

	/**
	 * Return default option for a given setting 
	 * @param name String with the default value for a given setting name
	 * @return setings value 
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
		
		names.add("useProxyServer");
		names.add("proxyServerName");
		names.add("proxyServerPort");
		return names;
	}
	
	public String getLastErrorMessage() {
	    return errorMessage;
	}

	/**
	 * Check whether all settings are set and valid. If not, the error message
	 * can be retrieved with getLastErrorMessage();
	 * @return true if all settings are set and valid, false otherwise.
	 */
	public boolean allSet() {
	    
	    String useProxyServer = getSettingValue("useProxyServer");
	    if ( !(useProxyServer.equals("0") || useProxyServer.equals("1"))) {
	        errorMessage = "Bad value for use proxy server!";
	        return false;
	    }

        String proxyServerName = getSettingValue("proxyServerName");
        if (proxyServerName.contains(" ")) {
            errorMessage = "No blank spaces are allowed in the proxy server name!";
            return false;            
        }
        setSettingValue("proxyServerName", proxyServerName);
        if (useProxyServer.equals("1") && proxyServerName.equals("")) {
            errorMessage = "Please specify a valid proxy server!";
            return false;
        }
        
        String proxyServerPort = getSettingValue("proxyServerPort");
        int port;
        if (! proxyServerPort.equals("")) {
            try {
                port = Integer.parseInt(proxyServerPort);
            } catch(NumberFormatException e) {
                errorMessage = "Please specify a valid proxy port!";
                return false;
            }
            if (port < 0) {
                errorMessage = "Please specify a valid proxy port!";
                return false;
            }
        }

        return true;
	}

}
