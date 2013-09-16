package ch.eth.scu.importer.lsrfortessa.processors.model;

import java.io.File;

import ch.eth.scu.importer.processors.model.PathAwareDescriptor;


public class ExperimentDescriptor extends PathAwareDescriptor{

	/** 
	 * Constructor
	 * @param fullPath Full path to the experiment folder.
	 */
	public ExperimentDescriptor(File fullPath) {
		super(fullPath);
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
	 * @returnthe properties file name with relative path
	 */
	public String getPropertiesFileNameWithRelPath() {
		return (getRelativePath() + File.separator +
				getName() + "_properties.oix");
	}

}
