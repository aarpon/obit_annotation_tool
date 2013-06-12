package ch.eth.scu.importer.processors.nikonnd2.model;

import ch.eth.scu.importer.processors.model.AbstractDescriptor;


public class ExperimentDescriptor extends AbstractDescriptor{

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Experiment";
	}

}
