package ch.eth.scu.importer.gui.components;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class DefaultNode extends Node{

	private static final long serialVersionUID = 1L;

	public DefaultNode(Object object) {
		super(object);
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(getClass().getResource("folder.png"));
	}
}