package ch.eth.scu.importer.at.gui.viewers.openbis.model;

/**
 * This Node is to be used temporarily and discarded as soon as the
 * refactoring of the OpenBISViewer class into Viewer and separate
 * (customizable) Processor is completed.
 * 
 * @author Aaron Ponti
 *
 */	
public class OpenBISSampleListNode extends AbstractOpenBISNode {
	
	private static final long serialVersionUID = 1L;

	String s;

	/**
	 * Constructor 
	 * @param s Sample object
	 */		
	public OpenBISSampleListNode(String s) {
		super(s);
		this.s = s;
	}

	/**
	 * Returns the type of the node
	 * @return Type of the node
	 */			
	@Override
	public String getType() { return "SampleList"; }

	/**
	 * String representation of the node
	 * @return String representation of the node
	 */			
	@Override
	public String toString() {
		return this.s;
	}

	/**
	 * Sample code
	 * @return Sample code
	 */			
	@Override
	public String getCode() { return "None"; }
	
	/**
	 * Sample identifier
	 * @return Sample identifier
	 */			
	@Override
	public String getIdentifier() { return "None"; }

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
	public String getTooltip() { 
		return "Total number of samples in current Experiment";
	}
	
	/**
	 * Sample does not have children and is therefore a leaf
	 * @return true
	 */
	@Override
	public boolean isLeaf() {
		return true;
	}
	
}
