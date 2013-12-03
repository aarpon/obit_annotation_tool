package ch.eth.scu.importer.processors.model;

import java.io.File;



public class DatasetDescriptor extends PathAwareDescriptor{

	/**
	 * Constructor
	 * @param fullPath Full path of the entity
	 */
	public DatasetDescriptor(File fullPath) {
		super(fullPath);
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Dataset";
	}

}
