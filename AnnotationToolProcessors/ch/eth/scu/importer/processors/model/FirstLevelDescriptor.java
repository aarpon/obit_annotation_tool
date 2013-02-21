package ch.eth.scu.importer.processors.model;

import java.io.File;

/**
 * Abstract class that must be extended by first level descriptors, i.e.
 * those descriptors that are immediate children of the RootDescriptor.
 * @author Aaron Ponti
 *
 */
abstract public class FirstLevelDescriptor extends AbstractDescriptor{

	/**
	 * Relative path stored as a String
	 */
	protected String relativePath = "";
	
	/**
	 * Return the folder name containing the processed dataset relative 
	 * to the Datamover incoming folder.
	 */
	public abstract String getRelativePath();
	
	/**
	 * Return the full file name of the properties name (the XML file with 
	 * all information needed by the dropbox to register the dataset).
	 * 
	 * Default implementation.
	 */
	public String getPropertiesNameForSaving() {
		return this.getRelativePath() + File.separator + this.name +
				"_properties.six"; 
	}
}
