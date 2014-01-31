package ch.eth.scu.importer.microscopy.gui.editors.data.model;

import ch.eth.scu.importer.at.gui.editors.data.model.AbstractMetadataMapper;
import ch.eth.scu.importer.at.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.eth.scu.importer.microscopy.processors.MicroscopyProcessor.Experiment;

/**
 * Collects all relevant metadata to allow the registration of an
 * experiment from a Nikon microscope (ND2 file format).
 * @author Aaron Ponti
 *
 */
public class MicroscopyMetadata extends AbstractMetadataMapper {

	public ExperimentNode experimentNode;
	public OpenBISProjectNode openBISProjectNode;
	
	/** 
	 * Constructor
	 */
	public MicroscopyMetadata(ExperimentNode experimentNode, 
			OpenBISProjectNode openBISProjectNode) {
	
		// Assign folder and openBIS project nodes
		this.experimentNode = experimentNode;
		this.openBISProjectNode = openBISProjectNode;
		
	}

	/**
	 * Get the experiment descriptor
	 * @return the experiment descriptor
	 */
	public Experiment getExperiment() {
		return (Experiment) 
				this.experimentNode.getUserObject();
	}

	/**
	 * Get the experiment name
	 * @return the experiment name
	 */
	public String getExperimentName() {
		return this.experimentNode.toString();
	}
	
	/**
	 * Get the project name
	 * @return the experiment name
	 */
	public String getProjectName() {
		return openBISProjectNode.toString();
	}

	/**
	 * Get the openBIS Experiment Identifier
	 * @return the openBIS Experiment identifier
	 */
	public String getOpenBISExerimentIdentifier() {
		String openBISProjectID = openBISProjectNode.getIdentifier();
		String name = getExperimentName().replaceAll(" ", "_");
		return (openBISProjectID + "/" + name).toUpperCase();
	}
	
	/**
	 * Get the openBIS Space Identifier
	 * @return the openBIS space identifier
	 */
	public String getOpenBISSpaceIdentifier() {
		String openBISProjectID = openBISProjectNode.getIdentifier();
		
		// Find the first occurrence of / which is NOT at the beginning
		int indx = openBISProjectID.indexOf("/", 1);
		
		if (indx == -1) {
			// This should not happen, sice the identifier came
			// from openBIS in the first place.
			System.err.println(
					"Malformed openBIS project identifier!");
			return "INVALID";
		}
		
		return (openBISProjectID.substring(0, indx).toUpperCase());
	}

}
