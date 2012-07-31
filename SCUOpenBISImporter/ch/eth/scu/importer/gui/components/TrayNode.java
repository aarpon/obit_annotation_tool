package ch.eth.scu.importer.gui.components;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class TrayNode extends CustomTreeNode {

	private static final long serialVersionUID = 1L;

	public TrayNode(Object object, String type) {
		super(object, type);
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(getClass().getResource("plate.png"));
	}
	
	public String getTooltip() {
		return "Tray";
	}
}