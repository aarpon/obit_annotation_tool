package ch.eth.scu.importer.ui_elements.lsrfortessa.gui.editors.data.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.eth.scu.importer.at.gui.editors.data.model.AbstractMetadataMapper;
import ch.eth.scu.importer.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.eth.scu.importer.processors.lsrfortessa.BDFACSDIVAFCSProcessor.Experiment;
import ch.eth.scu.importer.processors.lsrfortessa.BDFACSDIVAFCSProcessor.Tray;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.FolderNode;

/**
 * Collects all relevant metadata to allow the registration of an
 * experiment from the BD LSR FORTESSA flow cytometer into OpenBIS.
 * @author Aaron Ponti
 *
 */
public class BDLSRFortessaFCSMetadata extends AbstractMetadataMapper {

	public FolderNode folderNode;
	public OpenBISProjectNode openBISProjectNode;
	public String description;
	public ArrayList<String> supportedTrayGeometries;
	
	/** 
	 * Constructor
	 */
	public BDLSRFortessaFCSMetadata(FolderNode folderNode, 
			OpenBISProjectNode openBISProjectNode) {
	
		// Assign folder and openBIS project nodes
		this.folderNode = folderNode;
		this.openBISProjectNode = openBISProjectNode;
		
		// Set the supported geometries
		this.supportedTrayGeometries = new ArrayList<String>();
		this.supportedTrayGeometries.add("96_WELLS_8X12");
		this.supportedTrayGeometries.add("384_WELLS_16x24");
		
		// Set default values for the tray geometry
		Map<String, Tray> trays = getTraysForExperiment();
		for (String key : trays.keySet()) {
			trays.get(key).geometry =
					this.supportedTrayGeometries.get(0);
		}

		// Set empty description
		this.description = "";
	}

	/**
	 * Get the folder name
	 * @return the folder name
	 */
	public String getFolderName() {
		return folderNode.toString();
	}
	
	/**
	 * Get the experiment name
	 * @return the experiment name
	 */
	public Experiment getExperiment() {
		assert(folderNode.getChildCount() == 1);
		ExperimentNode e = (ExperimentNode) folderNode.getChildAt(0);
		return (Experiment) e.getUserObject();
	}

	/**
	 * Get the experiment name
	 * @return the experiment name
	 */
	public String getExperimentName() {
		assert(folderNode.getChildCount() == 1);
		return folderNode.getChildAt(0).toString();
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

	/**
	 * Get the plates for the stored Experiment.
	 * @return Plates contained in current Experiment. 
	 */
	public Map<String, Tray> getTraysForExperiment() {
		
		// Empty result set
		Map<String, Tray> emptySet = new LinkedHashMap<String, Tray>();
		
		// Do we have a folder node already?
		if (this.folderNode == null) {
			return emptySet;
		}
		
		// Do we have experiments in the folder?
		if (this.folderNode.getChildCount() == 0) {
			return  emptySet;
		}
		
		// The first (and only) child of a folder MUST be an Experiment 
		assert(this.folderNode.getChildAt(0).getClass().
				getSimpleName().equals("Experiment"));
		
		// Get the Experiment
		ExperimentNode e = (ExperimentNode) this.folderNode.getChildAt(0);
		return ((Experiment) e.getUserObject()).trays;
	}
}
