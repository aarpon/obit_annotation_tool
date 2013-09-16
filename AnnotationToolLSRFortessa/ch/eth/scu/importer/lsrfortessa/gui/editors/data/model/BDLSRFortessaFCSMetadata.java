package ch.eth.scu.importer.lsrfortessa.gui.editors.data.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.eth.scu.importer.at.gui.editors.data.model.AbstractMetadataMapper;
import ch.eth.scu.importer.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.eth.scu.importer.lsrfortessa.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor.Experiment;
import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor.Tray;

/**
 * Collects all relevant metadata to allow the registration of an
 * experiment from the BD LSR FORTESSA flow cytometer into OpenBIS.
 * @author Aaron Ponti
 *
 */
public class BDLSRFortessaFCSMetadata extends AbstractMetadataMapper {

	public ExperimentNode expNode;
	public OpenBISProjectNode openBISProjectNode;
	public ArrayList<String> supportedTrayGeometries;
	
	/** 
	 * Constructor
	 */
	public BDLSRFortessaFCSMetadata(ExperimentNode expNode, 
			OpenBISProjectNode openBISProjectNode) {
	
		// Assign folder and openBIS project nodes
		this.expNode = expNode;
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
		return expNode.getParent().toString();
	}
	
	/**
	 * Get the experiment descriptor
	 * @return the experiment descriptor
	 */
	public Experiment getExperiment() {
		return (Experiment) expNode.getUserObject();
	}

	/**
	 * Get the experiment name
	 * @return the experiment name
	 */
	public String getExperimentName() {
		return expNode.toString();
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
		if (this.expNode == null) {
			return emptySet;
		}
		
		// Return the trays
		return ((Experiment) this.expNode.getUserObject()).trays;
	}
}
