package ch.ethz.scu.obit.bdfacsdivafcs.gui.editors.data.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.ethz.scu.obit.at.gui.editors.data.model.AbstractMetadataMapper;
import ch.ethz.scu.obit.at.gui.viewers.data.model.ExperimentNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.BDFACSDIVAFCSProcessor.Experiment;
import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.BDFACSDIVAFCSProcessor.Tray;

/**
 * Collects all relevant metadata to allow the registration of an
 * experiment from the BD LSR FORTESSA flow cytometer into OpenBIS.
 * @author Aaron Ponti
 *
 */
public final class BDFACSDIVAFCSMetadataMapper extends AbstractMetadataMapper {

	/**
	 * Selected experiment node.
	 */
	public ExperimentNode experimentNode;
	
	/**
	 * Selected openBIS project node.
	 */
	public OpenBISProjectNode openBISProjectNode;
	
	/**
	 * Support tray geometries.
	 */
	public ArrayList<String> supportedTrayGeometries;
	
	/** 
	 * Constructor
	 * @param expNode Experiment node (in the data viewr)
	 * @param openBISProjectNode  openBIS project node (in the openBIS viewer)
	 */
	public BDFACSDIVAFCSMetadataMapper(ExperimentNode expNode, 
			OpenBISProjectNode openBISProjectNode) {
	
		// Assign folder and openBIS project nodes
		this.experimentNode = expNode;
		this.openBISProjectNode = openBISProjectNode;
		
		// Set the supported geometries
		this.supportedTrayGeometries = new ArrayList<String>();
		this.supportedTrayGeometries.add("96_WELLS_8X12");
		this.supportedTrayGeometries.add("384_WELLS_16x24");
	}

	/**
	 * Get the folder name
	 * @return the folder name
	 */
	public String getFolderName() {
		return experimentNode.getParent().toString();
	}
	
	/**
	 * Get the experiment descriptor
	 * @return the experiment descriptor
	 */
	public Experiment getExperiment() {
		return (Experiment) experimentNode.getUserObject();
	}

	/**
	 * Get the experiment name
	 * @return the experiment name
	 */
	public String getExperimentName() {
		return experimentNode.toString();
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
		Experiment e = (Experiment) experimentNode.getUserObject();
		return (openBISProjectID + "/" + e.getCode()).toUpperCase();
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
			// This should not happen, since the identifier came
			// from openBIS in the first place.
			System.err.println(
					"Malformed openBIS project identifier!");
			return "INVALID";
		}
		
		return (openBISProjectID.substring(0, indx).toUpperCase());
	}

	/**
	 * Get the plates for the stored Experiment.
	 * @return Plates contained in current Experiment. 
	 */
	public Map<String, Tray> getTraysForExperiment() {
		
		// Empty result set
		Map<String, Tray> emptySet = new LinkedHashMap<String, Tray>();
		
		// Do we have a folder node already?
		if (this.experimentNode == null) {
			return emptySet;
		}
		
		// Return the trays
		return ((Experiment) this.experimentNode.getUserObject()).trays;
	}
}
