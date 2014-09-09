/**
 * 
 */
package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import ch.ethz.scu.obit.readers.AbstractReader;

/**
 * An abstract reader that should be specialized to manage composite datasets;
 * i.e. datasets that are composed of more than one file. The reader must 
 * return a single dataset but must store and manage the files belonging to it
 * properly to ensure correct registration in openBIS.
 * 
 * TODO: Add details.
 * 
 * @author Aaron Ponti
 *
 */
public abstract class CompositeMicroscopyReader extends AbstractReader {

	/**
	 * Return true if the specialized can read and process the dataset
	 * contained in the passed folder.
	 *   
	 * @param folder Folder containing the files that compose the dataset.
	 * @return true if the reader can read and process the dataset, false
	 *         otherwise.
	 */
	abstract public boolean canRead(File folder);

}
