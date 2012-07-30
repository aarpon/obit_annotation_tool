package ch.eth.scu.importer.gui.components;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class TubeNode extends Node{

	private static final long serialVersionUID = 1L;

	public TubeNode(Object object) {
		super(object);
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(getClass().getResource("tube.png"));
	}
	
	public String getTooltip() {
		return "Tube";
	}
}