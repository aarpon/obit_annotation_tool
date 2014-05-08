package ch.ethz.scu.obit.at.gui.viewers.openbis.view;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import ch.ethz.scu.obit.at.gui.viewers.openbis.model.AbstractOpenBISNode;

/**
 * Custom Cell Renderer that adapts to individual Node implementations
 * @author Aaron Ponti
 */
public class OpenBISViewerTreeCellRenderer extends DefaultTreeCellRenderer{
	
	private static final long serialVersionUID = 1L;

    /**   
    * Constructor  
    */ 
    public OpenBISViewerTreeCellRenderer() { 
    }
    
    /**  
    * getTreeCellRendererComponent  
    * This method is overridden to set the node specific icon and tooltip 
    *     
    * @return The Component object used to render the cell value 
    */  
	public Component getTreeCellRendererComponent(JTree tree, Object value, 
			boolean sel, boolean expanded, boolean leaf, int row, 
			boolean hasFocus) {
		
		// Call the base class's getTreeCellRendererComponent()
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
				row, hasFocus);

		// Override the icon with the Node's one and add the custom
		// tool-tip text
		if ((value != null) && (value instanceof AbstractOpenBISNode) ) {
			AbstractOpenBISNode node = (AbstractOpenBISNode) value;
			setIcon(node.getIcon());
			setToolTipText(node.getTooltip());
		}
	
		return this;

	}
}