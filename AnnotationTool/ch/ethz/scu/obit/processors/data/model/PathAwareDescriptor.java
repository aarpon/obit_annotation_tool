package ch.ethz.scu.obit.processors.data.model;

import java.io.File;
import java.io.IOException;

/**
 * Provides utility methods to handle path-related information for the
 * descriptor.
 * @author Aaron Ponti
 *
 */
public abstract class PathAwareDescriptor extends AbstractDescriptor{

	/**
	 * Global root of the users folder (parent to the current {user} folder). 
	 */
	protected File basePath;

	/**
	 * Full path of the entity 
	 */
	protected File fullPath;
	
	/**
	 * Path of the entity, relative to the root of the user dir 
	 */
	protected String relativePath;
	
	/**
	 * Constructor
	 * @param fullPath Full path of the entity
	 * @param userRootDataPath Full path to the root of the user folder
	 */
	public PathAwareDescriptor(File fullPath, File userRootDataPath) {

		// Store base and full path
		this.basePath = userRootDataPath;
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
	 * to the user folder.
	 * @return relative path 
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
	 * Computes and store the folder path relative to the base path.
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
		
		// First check that the initial part of the paths overlap
		String rel = "";
		if (fullFolderStr.indexOf(basePathStr) == 0) {
			try {
				rel = fullFolderStr.substring(basePathStr.length());
				if (rel.startsWith(File.separator) &&
						rel.length() > 1) {
					rel = rel.substring(1);
				}
			} catch (Exception e) {
				rel = "/";
			}
		}
		this.relativePath = rel;
	}
	
}
