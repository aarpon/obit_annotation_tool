package ch.eth.scu.importer.processor.model;

import java.util.Map;

public class DatasetDescriptor extends AbstractDescriptor{

	/**
	 * openBIS identifiers
	 */
	protected String openBISExperimentID = "";
	protected String openBISSampleID = "";

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Experiment";
	}
	
	/**
	 * Get the openBIS experiment identifier
	 * @return the openBIS experiment identifier
	 */
	public String getOpenBISExperimentIdentifier() {
		return openBISExperimentID;
	}
	
	/**
	 * Set the openBIS experiment identifier 
	 * @param openBISExperimentID The openBIS experiment identifier
	 */
	public void setOpenBISExperimentIdentifier(String openBISExperimentID) {
		this.openBISExperimentID = openBISExperimentID;
	}
	
	/**
	 * Get the openBIS sample identifier
	 * @return the openBIS sample identifier
	 */
	public String getOpenBISSampleIdentifier() {
		return openBISSampleID;
	}
	
	/**
	 * Set the openBIS sample identifier 
	 * @param openBISSampleID The openBIS sample identifier
	 */
	public void setOpenBISSampleIdentifier(String openBISSampleID) {
		this.openBISSampleID = openBISSampleID;
	}

	/**
	 * Return the entity openBIS attributes.
	 * @return a string-string map with attribute name : value pairs.
	 */
	public Map<String, String> getOpenBISAttributes() { 
		
		// Remark: to register a dataset, the identifier is not needed
		
		// Fill in the map
		openBISAttributes.put("openBISCode",
				getOpenBISCode());
		openBISAttributes.put("openBISSampleIdentifier",
				getOpenBISSampleIdentifier());
		openBISAttributes.put("openBISExperimentIdentifier",
				getOpenBISExperimentIdentifier());

		// Return
		return openBISAttributes;
	}
}
