package ch.eth.scu.importer.processors.model;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ch.eth.scu.importer.common.properties.AppProperties;

/**
 * Provides utility methods to handle path-related information for the
 * descriptor.
 * @author Aaron Ponti
 *
 */
public abstract class PathAwareDescriptor extends AbstractDescriptor{

	/**
	 * Base path (Datamover's incoming directory) 
	 */
	protected File basePath;

	/**
	 * Full path of the entity 
	 */
	protected File fullPath;
	
	/**
	 * Path of the entity, relative to the base dir 
	 * (Datamover's incoming directory) 
	 */
	protected String relativePath;
	
	/**
	 * Constructor
	 * @param fullDir: full path ot the entity
	 */
	public PathAwareDescriptor(File fullPath) {

		// Store base and full path
		Properties appProperties = AppProperties.readPropertiesFromFile();
		this.basePath = new File(
				appProperties.getProperty("DatamoverIncomingDir"));;
		this.fullPath = fullPath;
		
		// Compute and store the relative path
		computeAndStoreRelativePath();
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	abstract public String getType();

	/**
	 * Return the folder name containing the processed dataset relative 
	 * to the Datamover incoming folder.
	 */
	public String getRelativePath() {
		return relativePath;
	}
	
	/**
	 * Return the full base path name.
	 * @return path relative to the base path as String
	 */
	public String getBasePath() {
		try {
			return basePath.getCanonicalPath();
		} catch (IOException e) {
			return "Invalid path";
		}
	}

	/**
	 * Return the relative path name.
	 * @return path relative to the base path as String
	 */
	public String getFullPath() {
		try {
			return fullPath.getCanonicalPath();
		} catch (IOException e) {
			return "Invalid path";
		}
	}

	/**
	 * Return the full file name of the properties name (the XML file with 
	 * all information needed by the dropbox to register the dataset).
	 * 
	 * Default implementation.
	 */
	public String getPropertiesNameForSaving() {
		return this.getRelativePath() + 
				File.separator + this.name + "_properties.six"; 
	}	
	
	/**
	 * Computes and store the folder path relative to the Datamover 
	 * incoming folder.
	 * @param fullPath Folder with full path.
	 */
	private void computeAndStoreRelativePath() {
		String basePathStr = "";
		try {
			basePathStr = basePath.getCanonicalPath();
		} catch (IOException e) {
			System.err.println("Error with base folder path " +
					"("+ basePath + ")");
			System.exit(1);
		}
			
		// Extract the path relative to the base dir
		String fullFolderStr = "";
		try {
			fullFolderStr = fullPath.getCanonicalPath();
		} catch (IOException e) {
			System.err.println("Error with dataset folder path " +
					"("+ fullPath.toString() + ")");
			System.exit(1);
		}
		this.relativePath = fullFolderStr.substring(basePathStr.length());
		if (this.relativePath.startsWith(File.separator)) {
			this.relativePath = this.relativePath.substring(1);
		}
	}
	
}
