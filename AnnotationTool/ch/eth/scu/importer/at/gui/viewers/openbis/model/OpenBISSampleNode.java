package ch.eth.scu.importer.at.gui.viewers.openbis.model;

import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

/**
 * A Custom Sample TreeNode
 * @author Aaron Ponti
 *
 */	
public class OpenBISSampleNode extends AbstractOpenBISNode {
	
	private static final long serialVersionUID = 1L;

	private Sample s;

	/**
	 * Constructor 
	 * @param s Sample object
	 */		
	public OpenBISSampleNode(Sample s) {
		super(s);
		this.s = s;
	}

	/**
	 * Returns the type of the node
	 * @return Type of the node
	 */			
	@Override
	public String getType() { return "Sample"; }

	/**
	 * String representation of the node
	 * @return String representation of the node
	 */			
	@Override
	public String toString() {
		// Get the name if present or fall back to the sample code
		Map<String, String> properties = s.getProperties();
		if (properties.containsKey("LSR_FORTESSA_PLATE_NAME")) {
			return properties.get("LSR_FORTESSA_PLATE_NAME");
		} else if (properties.containsKey("LSR_FORTESSA_WELL_NAME")) {
			return properties.get("LSR_FORTESSA_WELL_NAME");
		} else if (properties.containsKey("LSR_FORTESSA_TUBE_NAME")) {
			return properties.get("LSR_FORTESSA_TUBE_NAME");
		} else if (properties.containsKey("FACS_ARIA_PLATE_NAME")) {
				return properties.get("FACS_ARIA_PLATE_NAME");
		} else if (properties.containsKey("FACS_ARIA_WELL_NAME")) {
				return properties.get("FACS_ARIA_WELL_NAME");
		} else if (properties.containsKey("FACS_ARIA_TUBE_NAME")) {
				return properties.get("FACS_ARIA_TUBE_NAME");		
		} else {
			if (properties.isEmpty()) {
				return "Tubeset";
			}
		}
		return s.getCode();
	}

	/**
	 * Sample code
	 * @return Sample code
	 */			
	@Override
	public String getCode() { return s.getCode(); }
	
	/**
	 * Sample identifier
	 * @return Sample identifier
	 */			
	@Override
	public String getIdentifier() { return s.getIdentifier(); }

	/**
	 * Get the icon to be displayed in the JTree
	 * @return the icon to be displayed in the JTree
	 */	
	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/sample.png"));
	}

	/**
	 * Get the tooltip to be displayed when the user places the mouse 
	 * over the Node in the JTree
	 * @return the tooltip to be displayed over the Node
	 */	
	public String getTooltip() { return s.getCode(); }
	
	/**
	 * Sample does not have children and is therefore a leaf
	 * @return true
	 */
	@Override
	public boolean isLeaf() {
		return true;
	}
	
}
