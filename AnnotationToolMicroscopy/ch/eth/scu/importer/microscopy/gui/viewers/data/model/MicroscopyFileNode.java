package ch.eth.scu.importer.microscopy.gui.viewers.data.model;

import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.processors.model.DatasetDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class MicroscopyFileNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public MicroscopyFileNode(DatasetDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/nd2.png"));
	}
	
	public String getTooltip() {
		return "ND2 file";
	}
}