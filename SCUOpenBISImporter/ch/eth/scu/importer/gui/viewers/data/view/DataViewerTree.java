package ch.eth.scu.importer.gui.viewers.data.view;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;

import ch.eth.scu.importer.gui.viewers.data.view.DataViewerTreeCellRenderer;

/**
 * Custom JTree that uses a customized renderer
 * @author Aaron Ponti
 */
public class DataViewerTree extends JTree {

	private static final long serialVersionUID = 1L;

	public DataViewerTree(DefaultMutableTreeNode node) {
		
		// Pass the node on to the base constructor
		super(node);
		
		// Set the custom renderer
	    setCellRenderer(new DataViewerTreeCellRenderer());
	    
	    // Tooltip manager
	    ToolTipManager.sharedInstance().registerComponent(this); 
		
	}
}
