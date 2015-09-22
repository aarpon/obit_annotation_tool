package ch.ethz.scu.obit.processors.data.model;

import java.io.File;



public class DatasetDescriptor extends PathAwareDescriptor{

	// Dataset description
	public String description = "";
	
	/**
	 * Constructor
	 * @param fullPath Full path of the entity
	 */
	public DatasetDescriptor(File fullPath, File userRootDataPath) {
		super(fullPath, userRootDataPath);
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
