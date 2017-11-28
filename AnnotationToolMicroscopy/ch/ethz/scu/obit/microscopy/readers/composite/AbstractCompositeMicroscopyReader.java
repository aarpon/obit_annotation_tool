/**
 * 
 */
package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import ch.ethz.scu.obit.readers.AbstractReader;
import loci.plugins.util.ImageProcessorReader;

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

	/* Protected instance variables */
	protected File folder;

	/** 
	 * Total composite dataset file in bytes
	 */
	protected long totalDatasetSizeInBytes = 0;

	/**
	 * Metadata attributes
	 */
	protected final Map<String, HashMap<String, String>> attr =
			new HashMap<String, HashMap<String, String>>();

	/**
	 * Bio-formats ImageProcessorReader
	 */
	protected ImageProcessorReader reader = null;

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
	 * @return list of series indices.
	 */
	abstract public List<Integer> getSeriesIndices();

	/**
	 * Returns total size in bytes for the composite dataset.
	 * @return total dataset size in bytes.
	 */
	public long totalDatasetSizeInBytes() {
		return totalDatasetSizeInBytes;
	}
	
	/**
	 * String-string map of data attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 * @return String-string map of attributes.
	 */
	public Map<String, HashMap<String, String>> getAttributes() {
		return attr;
	}

	/**
	 * Return current value for given metadata entry, or zero if not in the map.
	 * @param metadata Map of string - string key:value pairs.
	 * @param key Name of the metadata value to query.
	 * @return the value for the requested metadata value, or zero if
	 * it is not in the map.
	 */
	protected int getMetadataValueOrZero(Map<String, String> metadata, String key) {
		int value = 0;
		if (metadata.containsKey(key)) {
			value = Integer.parseInt(metadata.get(key));
		}
		return value;
	}

	/**
	 * Return the data type
	 * @return string datatype, one of "uint8", "uint16", "float", "unsupported".
	 */
	public String getDataType() {

		// Get and store the dataset type
		String datatype;
		switch (loci.formats.FormatTools.getBytesPerPixel(reader.getPixelType())) {
			case 1:
				datatype = "uint8";
				break;
			case 2:
				datatype = "uint16";
				break;
			case 4:
				datatype = "float";
				break;
			default:
				datatype = "unsupported";
				break;
		}

		return datatype;
	}

	/**
	 * Returns the last error message.
	 * @return String containing the last error message.
	 */
	public String getLastError() {
		return errorMessage;
	}
}
