package ch.eth.scu.importer.gui.viewers.data.model;

import ch.eth.scu.importer.processor.model.FirstLevelDescriptor;

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
				getClass().getResource("./icons/folder.png"));
	}
}