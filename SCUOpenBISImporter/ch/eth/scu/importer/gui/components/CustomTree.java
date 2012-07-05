package ch.eth.scu.importer.gui.components;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import ch.eth.scu.importer.gui.components.CustomTreeCellRenderer;

/**
 * Custom JTree that uses a customized renderer
 * @author Aaron Ponti
 */
public class CustomTree extends JTree {

	private static final long serialVersionUID = 1L;

	public CustomTree(DefaultMutableTreeNode node) {
		
		// Pass the node on to the base constructor
		super(node);
		
		// Set the custom renderer
	    setCellRenderer(new CustomTreeCellRenderer());	
		
	}
}
