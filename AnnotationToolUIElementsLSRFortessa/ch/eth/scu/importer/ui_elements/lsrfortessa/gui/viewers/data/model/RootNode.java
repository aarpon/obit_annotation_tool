package ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model;

import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.processors.model.RootDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class RootNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public RootNode(RootDescriptor object) {
		super(object);
		this.type = "root";
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/root.png"));
	}
}