package ch.ethz.scu.obit.bdfacsdivafcs.gui.viewers.data.model;

import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.BDFACSDIVAFCSProcessor.FCSFileParameterList;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class FCSFileParemeterListNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

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