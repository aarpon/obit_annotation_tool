package ch.ethz.scu.obit.flow.gui.data.model;

import ch.ethz.scu.obit.at.gui.data.model.AbstractNode;
import ch.ethz.scu.obit.flow.processors.data.model.FCSFileParameterList;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class FCSFileParemeterListNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param object A FCS file parameter list.
	 */
	public FCSFileParemeterListNode(FCSFileParameterList object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/xml.png"));
	}
	
	public String getTooltip() {
		return "FCS file parameters";
	}
}