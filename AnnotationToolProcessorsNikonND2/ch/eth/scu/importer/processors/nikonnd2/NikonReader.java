/**
 * 
 */
package ch.eth.scu.importer.processors.nikonnd2;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ch.eth.scu.importer.processors.BaseBioFormatsProcessor;

/**
 * @author pontia
 *
 */
public class NikonReader extends BaseBioFormatsProcessor {

	/**
	 * Metadata attributes
	 */
	protected Map<String, String> attr;

	/**
	 * @param filename
	 */
	public NikonReader(File filename) {
		
		// Call base constructor
		super(filename);

		// Initialize String-String attributes map
		attr = new HashMap<String, String>();

	}

	/**
	 * String-string map of data attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 */
	public Map<String, String> getAttributes() {
		
		// If we already scanned the file we just return the attributes
		if (attr.size() > 0) {
			return attr;
		}

		// Otherwise, we first scan and then return them
		scan();
		return attr;

	}

	/**
	 * Scan the file for metadata information and stores it as a 
	 * String-String map of key-value attributes.
	 */
	protected void scan() {

		// Number of series
		int numSeries = reader.getSeriesCount();
		
		// Get and store all subimages
		for (int i = 0; i < numSeries; i++) {

			reader.setSeries(i);

			// Image name
			attr.put("numSeries", Integer.toString(numSeries));

			String key_series = "series" + i + "_"; 

			// Image name
			attr.put(key_series + "name", omexmlMeta.getImageName(i));

			// Image size X
			attr.put(key_series + "sizeX", 
					Integer.toString(reader.getSizeX()));

			// Image size Y
			attr.put(key_series + "sizeY", 
					Integer.toString(reader.getSizeY()));

			// Image size Z
			attr.put(key_series + "sizeZ", 
					Integer.toString(reader.getSizeZ()));

			// Image size C
			attr.put(key_series + "sizeC", 
					Integer.toString(reader.getSizeC()));

			// Image size T
			attr.put(key_series + "sizeT", 
					Integer.toString(reader.getSizeT()));

			// Pixel type
			attr.put(key_series + "pixelType",
					loci.formats.FormatTools.getPixelTypeString(
							reader.getPixelType()));
		}

	}

}
