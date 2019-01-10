package ch.ethz.scu.obit.at.gui.openbis.view;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 * Custom JTree that uses a customized renderer
 * @author Aaron Ponti
 */
public class OpenBISViewerTree extends JTree {

	private static final long serialVersionUID = 1L;

	/**
	 * openBIS Viewer tree
	 * @param node Root node.
	 */
	public OpenBISViewerTree(DefaultMutableTreeNode node) {
		
		// Pass the node on to the base constructor
		super(node);
		
		// Set selection mode to single selection
		getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Set the custom renderer
	    setCellRenderer(new OpenBISViewerTreeCellRenderer());
	    
	    // Tooltip manager
	    ToolTipManager.sharedInstance().registerComponent(this); 
		
	}
}
