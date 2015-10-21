package ch.ethz.scu.obit.processors.data.model;

import java.io.File;

/**
 * @author Aaron Ponti
 * A dataset descriptor.
 *
 */
public class DatasetDescriptor extends PathAwareDescriptor{

	/**
	 *  Dataset description
	 */
	public String description = "";
	
	/**
	 * Constructor
	 * @param fullPath Full path of the entity
	 * @param userRootDataPath Path of the root of the user folder.
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
