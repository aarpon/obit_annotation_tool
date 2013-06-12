package ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model;

import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.processors.lsrfortessa.model.SampleDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class TubeNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public TubeNode(SampleDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/tube.png"));
	}
	
	public String getTooltip() {
		return "Tube";
	}
}