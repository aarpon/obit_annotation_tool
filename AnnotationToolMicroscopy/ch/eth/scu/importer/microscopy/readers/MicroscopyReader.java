/**
 * 
 */
package ch.eth.scu.importer.microscopy.readers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aaron Ponti
 *
 */
public class MicroscopyReader extends BaseBioFormatsReader {

	/**
	 * Metadata attributes
	 */
	protected Map<String,HashMap<String,String>> attr;

	/**
	 * @param filename File name with full path.
	 */
	public MicroscopyReader(File filename) {
		
		// Call base constructor
		super(filename);

		// Initialize String-String attributes map
		attr = new HashMap<String, HashMap<String, String>>();
		
	}

	/**
	 * String-string map of data attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 */
	public Map<String,HashMap<String,String>> getAttributes() {
		
		// If we have already scanned the file we just return the attributes
		if (attr.size() > 0) {
			return attr;
		}

		// Otherwise, we first scan and then return them
		parse();
		return attr;

	}

	/**
	 * Scan the file for metadata information and stores it as a 
	 * String-String map of key-value attributes.
	 * @return true if parsing was successful, false otherwise.
	 */
	@Override
	public boolean parse() {

		// If we have already scanned the file we just return true
		if (attr.size() > 0) {
			return true;
		}

		try {

			// Number of series
			int numSeries = reader.getSeriesCount();

			// Get and store all series
			for (int i = 0; i < numSeries; i++) {

				// Set the series
				reader.setSeries(i);

				// Create a hashmap for the attributes of current series
				HashMap<String, String> seriesAttr = new HashMap<String, String>();
				
				// Series number
				seriesAttr.put("numSeries", Integer.toString(i));

				// Image name
				seriesAttr.put("name", omexmlMeta.getImageName(i));

				// Image size X
				seriesAttr.put("sizeX", Integer.toString(reader.getSizeX()));

				// Image size Y
				seriesAttr.put("sizeY", Integer.toString(reader.getSizeY()));

				// Image size Z
				seriesAttr.put("sizeZ", Integer.toString(reader.getSizeZ()));

				// Image size C
				seriesAttr.put("sizeC", Integer.toString(reader.getSizeC()));

				// Image size T
				seriesAttr.put("sizeT", Integer.toString(reader.getSizeT()));

				// Pixel type
				seriesAttr.put("pixelType",
						loci.formats.FormatTools.getPixelTypeString(
								reader.getPixelType()));

				// Key prefix
				String seriesKey = "series_" + i; 

				// Now add the metadata for current series
				attr.put(seriesKey, seriesAttr);
				
				}

			} catch (Exception e) {

				return false;

			}
			
		return true;

	}

	/**
	 * Return the file name of associated to the MicroscopyReader.
	 * @return String containing the file name associated to the MicroscopyReader. 
	 */
	public String toString() {
		return filename.getName();
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override
	public String getType() {
		return "microscopy";
	}


	/**
	 * Return information regarding the file format.
	 * @return descriptive String for the Reader.
	 */
	public String info() {
		return "Bioformats-compatible microscopy file format.";
	}

}
