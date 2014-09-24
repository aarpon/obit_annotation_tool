/**
 * 
 */
package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public abstract class AbstractCompositeMicroscopyReader extends AbstractReader {

	/**
	 * Metadata attributes
	 */
	protected final Map<String, HashMap<String, String>> attr =
			new HashMap<String, HashMap<String, String>>();

	/**
	 * Return true if the specialized can read and process the dataset
	 * contained in the passed folder.
	 *   
	 * @param folder Folder containing the files that compose the dataset.
	 * @return true if the reader can read and process the dataset, false
	 *         otherwise.
	 */
	static public boolean canRead(File folder) { return false; }
	
	/**
	 * Return true if the composite microscopy dataset could be parsed
	 * successfully.
	 * @return true if the composite microscopy dataset could be parsed
	 * successfully, false otherwise.
	 */
	public boolean isParsed() { return false; }
	
	/**
	 * Return the (representative) name of the composite microscopy dataset.
	 * @return the name of the composite microscopy dataset.
	 */	
	abstract public String getName();
	
	/**
	 * Return the folder that contains the composite microscopy dataset.
	 * @return the dataset folder.
	 */
	abstract public File getFolder();
	
	/**
	 * Returns a list of indices for the series contained in the dataset
	 * @return
	 */
	abstract public List<Integer> getSeriesIndices();
	
	/**
	 * String-string map of data attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 */
	public Map<String, HashMap<String, String>> getAttributes() {
		return attr;
	}

}
