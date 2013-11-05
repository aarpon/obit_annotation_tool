package ch.eth.scu.importer.at.gui.viewers.openbis.model;

import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;

/**
 * A Custom Experiment TreeNode
 * @author Aaron Ponti
 *
 */	
public class OpenBISExperimentNode extends AbstractOpenBISNode {
	
	private static final long serialVersionUID = 1L;

	private Experiment e;

	/**
	 * Constructor 
	 * @param e Experiment object
	 */		
	public OpenBISExperimentNode(Experiment e) {
		super(e);
		this.e = e;
	}

	/**
	 * Returns the type of the node
	 * @return Type of the node
	 */			
	@Override
	public String getType() { return "Experiment"; }

	/**
	 * String representation of the node
	 * @return String representation of the node
	 */			
	@Override
	public String toString() { 
		// Get the name if present or fall back to the experiment code
		Map<String, String> properties = e.getProperties();
		if (properties.containsKey("LSR_FORTESSA_EXPERIMENT_NAME")) {
			return properties.get("LSR_FORTESSA_EXPERIMENT_NAME");
		}
		return e.getCode(); 
	}

	/**
	 * Experiment code
	 * @return Experiment code
	 */			
	@Override
	public String getCode() { return e.getCode(); }
	
	/**
	 * Experiment identifier
	 * @return Experiment identifier
	 */			
	@Override
	public String getIdentifier() { return e.getIdentifier(); }

	/**
	 * Get the icon to be displayed in the JTree
	 * @return the icon to be displayed in the JTree
	 */	
	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/experiment.png"));
	}

	/**
	 * Get the tooltip to be displayed when the user places the mouse 
	 * over the Node in the JTree
	 * @return the tooltip to be displayed over the Node
	 */	
	public String getTooltip() { return e.getCode(); }

	/**
	 * Experiment has children and is therefore not a leaf
	 * @return false
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}

}
