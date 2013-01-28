package ch.eth.scu.importer.processor.model;

import java.util.Map;

public class ExperimentDescriptor extends AbstractDescriptor{

	/**
	 * openBIS project identifier
	 */
	protected String openBISProjectID = "";
	
	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Experiment";
	}

	/**
	 * Return the openBIS identifier of the entity. This is used to get or
	 * create the openBIS entity associated to this Descriptor.
	 */
	public String getOpenBISIdentifier() {
		if (openBISProjectID.isEmpty()) {
			System.err.println("ExperimentDescriptor: the openBIS project " +
					"identifier was not set!");
		}
		return openBISProjectID + "/" + getOpenBISCode();
	}
	
	/**
	 * Get the openBIS space identifier (computed from the project identifier)
	 * @return the openBIS space identifier
	 */
	public String getOpenBISSpaceIdentifier() {
		
		if (openBISProjectID.length() < 2 ) {
			System.err.println(
					"ExperimentDescriptor: The openBIS project" +
					" Identifier was not set!");
			return "INVALID";
		}
		
		// Find the first occurrence of / which is NOT at the beginning
		int indx = openBISProjectID.indexOf("/", 1);
		
		if (indx == -1) {
			System.err.println(
					"ExperimentDescriptor: Malformed openBIS " +
					"project identifier!");
			return "INVALID";
		}
		
		return (openBISProjectID.substring(0, indx));
	}

	/**
	 * Get the openBIS project identifier
	 * @return the openBIS project identifier
	 */
	public String getOpenBISProjectIdentifier() {
		return openBISProjectID;
	}
	
	/**
	 * Set the openBIS project identifier 
	 * @param projectID The openBIS project identifier
	 */
	public void setOpenBISProjectIdentifier(String projectID) {
		this.openBISProjectID = projectID;
	}

	/**
	 * Return the entity openBIS attributes.
	 * @return a string-string map with attribute name : value pairs.
	 */
	public Map<String, String> getOpenBISAttributes() { 
		
		// Fill in the map
		openBISAttributes.put("openBISIdentifier",
				getOpenBISIdentifier());

		// Return
		return openBISAttributes;
	}
	
}
