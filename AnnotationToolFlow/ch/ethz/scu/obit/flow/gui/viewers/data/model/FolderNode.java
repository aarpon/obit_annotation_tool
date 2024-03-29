package ch.ethz.scu.obit.flow.gui.viewers.data.model;

import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.processors.data.model.PathAwareDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class FolderNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param object A path aware descriptor.
	 */
	public FolderNode(PathAwareDescriptor object) {
		super(object);
		this.type = "root";
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/folder.png"));
	}
}