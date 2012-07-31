package ch.eth.scu.importer.gui.components;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class SubImageNode extends CustomTreeNode {

	private static final long serialVersionUID = 1L;

	public SubImageNode(Object object, String type) {
		super(object, type);
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(getClass().getResource("subimage.png"));
	}
	
	public String getTooltip() {
		return "Sub-image";
	}
}