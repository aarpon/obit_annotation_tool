package ch.eth.scu.importer.processor.model;


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
