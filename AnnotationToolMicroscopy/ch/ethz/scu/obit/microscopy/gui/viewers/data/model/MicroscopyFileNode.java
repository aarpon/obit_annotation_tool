package ch.ethz.scu.obit.microscopy.gui.viewers.data.model;

import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.at.interfaces.ILazyNode;
import ch.ethz.scu.obit.processors.data.model.DatasetDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class MicroscopyFileNode extends AbstractNode implements ILazyNode {

	private static final long serialVersionUID = 1L;

	protected boolean isLoaded = false;

	public MicroscopyFileNode(DatasetDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/microscopyfile.png"));
	}
	
	public String getTooltip() {
		return "Microscopy file";
	}
	
	/**
	 * MicroscopyFileNode has children and is therefore not a leaf
	 * @return false
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public void setLoaded() {
		isLoaded = true;
	}
}
