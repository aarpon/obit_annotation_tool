package ch.eth.scu.importer.gui.editors.data.model;

import ch.eth.scu.importer.gui.viewers.data.model.FolderNode;
import ch.eth.scu.importer.gui.viewers.openbis.model.OpenBISProjectNode;

/**
 * Collects all relevant metadata to allow the registration of an
 * experiment from the BD LSR FORTESSA flow cytometer into OpenBIS.
 * @author Aaron Ponti
 *
 */
public class BDLSRFortessaFCSMetadata extends AbstractMetadataElement {

	public FolderNode folderNode;
	public OpenBISProjectNode openBISProjectNode;
	public String trayGeometry;
	public String description;
	
	/** 
	 * Constructor
	 */
	public BDLSRFortessaFCSMetadata(FolderNode folderNode, 
			OpenBISProjectNode openBISProjectNode) {
	
		this.folderNode = folderNode;
		this.openBISProjectNode = openBISProjectNode;
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
	
}
