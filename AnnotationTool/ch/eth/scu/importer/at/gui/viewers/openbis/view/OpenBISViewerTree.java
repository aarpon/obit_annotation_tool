package ch.eth.scu.importer.at.gui.viewers.openbis.view;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Custom JTree that uses a customized renderer
 * @author Aaron Ponti
 */
public class OpenBISViewerTree extends JTree {

	private static final long serialVersionUID = 1L;

	public OpenBISViewerTree(DefaultMutableTreeNode node) {
		
		// Pass the node on to the base constructor
		super(node);
		
		// Set the custom renderer
	    setCellRenderer(new OpenBISViewerTreeCellRenderer());
	    
	    // Tooltip manager
	    ToolTipManager.sharedInstance().registerComponent(this); 
		
	}
}
