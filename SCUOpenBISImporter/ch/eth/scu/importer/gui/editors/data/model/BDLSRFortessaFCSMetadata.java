package ch.eth.scu.importer.gui.editors.data.model;

import java.util.ArrayList;

import ch.eth.scu.importer.gui.viewers.data.model.FolderNode;
import ch.eth.scu.importer.gui.viewers.openbis.model.OpenBISProjectNode;

/**
 * Collects all relevant metadata to allow the registration of an
 * experiment from the BD LSR FORTESSA flow cytometer into OpenBIS.
 * @author Aaron Ponti
 *
 */
public class BDLSRFortessaFCSMetadata extends AbstractMetadata {

	public FolderNode folderNode;
	public OpenBISProjectNode openBISProjectNode;
	public String trayGeometry;
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
		
		// Set default values for trayGeometry and description
		this.trayGeometry = this.supportedTrayGeometries.get(0);
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

}
