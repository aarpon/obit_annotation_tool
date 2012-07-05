package ch.eth.scu.importer.gui.components;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Custom Cell Renderer that adapts to individual Node implementations
 * @author Aaron Ponti
 */
public class CustomTreeCellRenderer extends DefaultTreeCellRenderer{
	
	private static final long serialVersionUID = 1L;

	public Component getTreeCellRendererComponent(JTree tree, Object value, 
			boolean sel, boolean expanded, boolean leaf, int row, 
			boolean hasFocus) {
		
		// Call the base class's getTreeCellRendererComponent()
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
				row, hasFocus);
		
		// Override the icon with the Node's one
		if ((value != null) && (value instanceof Node) ) {
			setIcon(((Node)value).getIcon());
		}
	
		return this;

	}
}