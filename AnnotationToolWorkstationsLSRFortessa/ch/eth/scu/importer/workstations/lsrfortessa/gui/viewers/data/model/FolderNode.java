package ch.eth.scu.importer.workstations.lsrfortessa.gui.viewers.data.model;

import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.processors.model.FirstLevelDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class FolderNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public FolderNode(FirstLevelDescriptor object) {
		super(object);
		this.type = "root";
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/folder.png"));
	}
}