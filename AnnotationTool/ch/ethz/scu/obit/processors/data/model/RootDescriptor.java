package ch.ethz.scu.obit.processors.data.model;

import java.io.File;

/**
 * The RootDescriptor is the top node in the data model tree and 
 * maps to the user folder.
 * 
 * @author Aaron Ponti
 *
 */
public class RootDescriptor extends PathAwareDescriptor{

	/**
	 * Constructor.
	 * @param fullUserFolderPath Full user folder path.
	 */
	public RootDescriptor(File fullUserFolderPath, File userRootDataPath) {
		super(fullUserFolderPath, userRootDataPath);
	}
	
	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Root";
	}
	
	/*
	 * Return the String representation of the descriptor
	 * @return the relative path as String representation
	 */
	@Override
	public String toString() {
		return this.relativePath;
	}
}
