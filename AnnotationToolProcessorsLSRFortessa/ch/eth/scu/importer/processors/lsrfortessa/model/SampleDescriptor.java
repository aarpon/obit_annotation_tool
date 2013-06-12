package ch.eth.scu.importer.processors.lsrfortessa.model;

import ch.eth.scu.importer.processors.model.AbstractDescriptor;


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
