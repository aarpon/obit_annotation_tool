package ch.eth.scu.importer.processors.model;

public class DatasetDescriptor extends AbstractDescriptor{

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Dataset";
	}

}
