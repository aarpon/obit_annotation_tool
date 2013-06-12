package ch.eth.scu.importer.processors.model;

import java.io.File;

/**
 * Abstract class that must be extended by first level descriptors, i.e.
 * those descriptors that are immediate children of the RootDescriptor.
 * @author Aaron Ponti
 *
 */
abstract public class FirstLevelDescriptor extends PathAwareDescriptor {

	/**
	 * Constructor
	 * @param baseDir: base directory (Datamover's incoming directory)
	 * @param fullDir: full path ot the entity
	 */
	public FirstLevelDescriptor(File fullPath) {
		super(fullPath);
	}

}
