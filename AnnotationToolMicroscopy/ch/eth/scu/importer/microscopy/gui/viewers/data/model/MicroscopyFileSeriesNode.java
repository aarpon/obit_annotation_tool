package ch.eth.scu.importer.microscopy.gui.viewers.data.model;

import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.at.interfaces.ILazyNode;
import ch.eth.scu.importer.microscopy.processors.data.MicroscopyProcessor.MicroscopyFileSeries;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * 
 * @author Aaron Ponti
 */
public class MicroscopyFileSeriesNode extends AbstractNode implements ILazyNode {

	private static final long serialVersionUID = 1L;

	protected boolean isLoaded = false;

	public MicroscopyFileSeriesNode(MicroscopyFileSeries microscopyFileSeries) {
		super(microscopyFileSeries);
		this.type = microscopyFileSeries.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(getClass().getResource(
				"icons/series.png"));
	}

	public String getTooltip() {
		return "Microscopy file series";
	}

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public void setLoaded() {
		isLoaded = true;
	}

	/**
	 * MicroscopyFileSeriesNode has no children and is therefore a leaf
	 * 
	 * @return true
	 */
	@Override
	public boolean isLeaf() {
		return true;
	}
}