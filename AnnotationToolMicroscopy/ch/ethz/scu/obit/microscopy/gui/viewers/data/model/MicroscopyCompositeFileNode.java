package ch.ethz.scu.obit.microscopy.gui.viewers.data.model;

import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.at.interfaces.ILazyNode;
import ch.ethz.scu.obit.processors.data.model.DatasetDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class MicroscopyCompositeFileNode extends AbstractNode implements ILazyNode {

	private static final long serialVersionUID = 1L;

	protected boolean isLoaded = false;

	public MicroscopyCompositeFileNode(DatasetDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/microscopycompositefile.png"));
	}

	public String getTooltip() {
		return "Composite microscopy file";
	}

	@Override
	public void setLoaded() {
		isLoaded = true;
	}

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}
	
	/**
	 * MicroscopyCompositeFileNode has no children and is therefore a leaf
	 * 
	 * @return true
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}

}
