package ch.ethz.scu.obit.flow.processors.data.model;

import ch.ethz.scu.obit.processors.data.model.AbstractDescriptor;


/**
 * @author Aaron Ponti
 * A sample descriptor.
 */
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
