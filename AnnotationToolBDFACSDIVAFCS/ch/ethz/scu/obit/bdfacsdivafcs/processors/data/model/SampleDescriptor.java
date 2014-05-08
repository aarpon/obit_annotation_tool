package ch.ethz.scu.obit.bdfacsdivafcs.processors.data.model;

import ch.ethz.scu.obit.processors.data.model.AbstractDescriptor;


public abstract class SampleDescriptor extends AbstractDescriptor{

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Sample";
	}

}
