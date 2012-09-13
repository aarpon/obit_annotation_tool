package ch.eth.scu.importer.processor.model;

import java.util.Map;

public class SampleDescriptor extends AbstractDescriptor{

	/**
	 * openBIS identifiers
	 */
	protected String openBISContainerSampleID = "";
	protected String openBISExperimentID = "";


	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Experiment";
	}

	/**
	 * Get the openBIS space identifier (computed from the project identifier)
	 * @return the openBIS space identifier
	 */
	public String getOpenBISSpaceIdentifier() {

		if (openBISExperimentID.length() < 2 ) {
			System.err.println(
					"SampleDescriptor: The openBIS experiment " +
					"Identifier was not set!");
			return "INVALID";
		}

		// Find the first occurrence of / which is NOT at the beginning
		int indx = openBISExperimentID.indexOf("/", 1);

		if (indx == -1) {
			System.err.println(
					"SampleDescriptor: Malformed openBIS " +
					"experiment identifier!");
			return "INVALID";
		}

		return (openBISExperimentID.substring(0, indx));
	}

	/**
	 * Return the openBIS identifier of the entity. This is used to get or
	 * create the openBIS entity associated to this Descriptor. It is computed
	 * from the space identifier and the code.
	 */
	public String getOpenBISIdentifier() {
		return getOpenBISSpaceIdentifier() + "/" + getOpenBISCode();
	}

	/**
	 * Get the openBIS sample container identifier
	 * @return the openBIS sample container identifier
	 */
	public String getOpenBISContainerSampleIdentifier() {
		return openBISContainerSampleID;
	}

	/**
	 * Set the openBIS identifier of the container sample, or "" if there is
	 * no container. 
	 * @param openBISContainerSampleID
	 */
	public void setOpenBISContainerSampleIdentifier(
			String openBISContainerSampleID) {
		this.openBISContainerSampleID = openBISContainerSampleID;
	}

	/**
	 * Get the openBIS experiment identifier
	 * @return the openBIS experiment identifier
	 */
	public String getOpenBISExperimentIdentifier() {
		return openBISExperimentID;
	}

	/**
	 * Set the openBIS identifier of the experiment 
	 * @param openBISExperimentID The openBIS experiment identifier.
	 */
	public void setOpenBISExperimentIdentifier(String openBISExperimentID) {
		this.openBISExperimentID = openBISExperimentID;
	}	

	/**
	 * Return the entity openBIS attributes.
	 * @return a string-string map with attribute name : value pairs.
	 */
	public Map<String, String> getOpenBISAttributes() { 

		// Fill in the map
		openBISAttributes.put("openBISCode",
				getOpenBISCode());
		openBISAttributes.put("openBISContainerSampleIdentifier",
				getOpenBISContainerSampleIdentifier());
		openBISAttributes.put("openBISExperimentIdentifier",
				getOpenBISExperimentIdentifier());
		openBISAttributes.put("openBISIdentifier",
				getOpenBISIdentifier());
		openBISAttributes.put("openBISSpaceIdentifier",
				getOpenBISSpaceIdentifier());

		// Return
		return openBISAttributes;
	}
}
