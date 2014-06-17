package ch.ethz.scu.obit.processors.data.model;

import java.io.File;
import java.util.regex.Pattern;


public class ExperimentDescriptor extends PathAwareDescriptor{

	// Used to filter the name of the ExperimentDescriptor
	static final Pattern p = Pattern.compile("[^a-zA-Z0-9\\-\\_]");

	/** 
	 * Constructor
	 * @param fullPath Full path to the experiment folder.
	 */
	public ExperimentDescriptor(File fullPath) {
		super(fullPath);
	}

	/**
	 * Sets the name of the descriptor. Since the name is mapped to an openBIS
	 * entity and cannot contain characters in the set: [^a-zA-Z0-9-_], this
	 * method cleans the passed string.
	 * @param name Name of the Descriptor. 
	 */
	@Override
	public void setName(String name) {
		name = p.matcher(name).replaceAll("_");
		super.setName(name);
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
