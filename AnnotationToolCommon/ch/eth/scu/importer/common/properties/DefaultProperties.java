package ch.eth.scu.importer.common.properties;

import java.util.ArrayList;

/**
 * Default properties defines all properties with possible and default values
 * @author Aaron Ponti
 */
public class DefaultProperties {

	/**
	 * Return all known properties 
	 * @return an ArrayList<String> with all known property names
	 */
	public static ArrayList<String> propertyNames() {
		
		ArrayList<String> names = new ArrayList<String>();
		
		names.add("OpenBISURL");
		names.add("AcceptSelfSignedCertificates");
		names.add("AcquisitionStation");
		names.add("UserDataDir");
		names.add("DatamoverIncomingDir");
		
		return names;
	}
	
	/**
	 * Return all options for a given property.
	 * 
	 * This function is guaranteed to return an ArrayList<String> with at least
	 * one element.
	 * 
	 * @return an ArrayList<String> with all options for a given property name
	 */		
	public static ArrayList<String> possibleValuesForProperty(String property) {

		ArrayList<String> options = new ArrayList<String>();
		
		if (property.equals("OpenBISURL")) {
			options.add("https://localhost:8443/openbis");
		} else if (property.equals("AcceptSelfSignedCertificates")) {
			options.add("no");
			options.add("yes");
		} else if (property.equals("AcquisitionStation")) {
			options.add("BD LSRFortessa cell analyzer");
			options.add("BD FACSAria III cell sorter");
			options.add("Generic light microscope");
		} else if (property.equals("UserDataDir")) {
			options.add("");			
		} else if (property.equals("DatamoverIncomingDir")) {
			options.add("");
		} else {
			throw (new IllegalArgumentException());
		}
		
		return options;
	}

	/**
	 * Return default option for a given property 
	 * @return an String with the default value for a given property name
	 */		
	public static String defaultValueForProperty(String property) {

		// Get all values for the given property
		ArrayList<String> options = possibleValuesForProperty(property);
		
		// The first one is the default. The options ArrayList is guaranteed
		// to have at least one element.
		return options.get(0);
	}

}
