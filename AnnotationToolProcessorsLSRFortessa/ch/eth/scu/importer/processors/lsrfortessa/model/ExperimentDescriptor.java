package ch.eth.scu.importer.processors.lsrfortessa.model;

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

}