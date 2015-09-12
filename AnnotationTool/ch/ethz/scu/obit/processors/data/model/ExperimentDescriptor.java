package ch.ethz.scu.obit.processors.data.model;

import java.io.File;


public class ExperimentDescriptor extends PathAwareDescriptor{

	/** 
	 * Constructor
	 * @param fullPath Full path to the experiment folder.
	 */
	public ExperimentDescriptor(File fullPath, File userRootDataPath) {
		super(fullPath, userRootDataPath);
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Experiment";
	}

	/**
	 * Returns the properties file name with relative path 
	 * @return the properties file name with relative path
	 */
	public String getPropertiesFileNameWithRelPath() {
		return (getRelativePath() + File.separator +
				getName() + "_properties.oix");
	}

}
