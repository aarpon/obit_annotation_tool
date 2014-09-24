package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import loci.common.DebugTools;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

/**
 * A composite reader for Leica TIFF series.
 * 
 * @author Aaron Ponti
 *
 */
public class LeicaTIFFSeriesReader extends AbstractCompositeMicroscopyReader {

	/* Protected instance variables */
	private File folder;
	private final String REGEX =
			"^(.*?)(_s(\\d.*?))?_z(\\d.*?)_ch(\\d.*?)\\.tif{1,2}$";
			
	private Pattern p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
    
	private File metadataFolder; 
	private String basename = "";
	
	private boolean isValid = false;

	private ImageProcessorReader reader = null;
	
	// Constructor
	public LeicaTIFFSeriesReader(File folder) {

		DebugTools.enableLogging("ERROR");
		
		this.folder = folder;
	}

	/**
	 * Return true if the reader can parse the passed folder
	 * @param folder Folder to be parsed.
	 * @return true if the folder can be parsed, false otherwise.
	 */
	static public boolean canRead(File folder) {
		
		// TODO: Implement!
		return true; 
	
	}

	/**
	 * Return true if the composite microscopy dataset could be parsed
	 * successfully.
	 * @return true if the composite microscopy dataset could be parsed
	 * successfully, false otherwise.
	 */
	public boolean isParsed() {
		return isValid;
	}

	@Override
	public boolean parse() throws Exception {

		// Get a list of all files
		File[] allFiles = folder.listFiles();

		// Now process them
		for (File file : allFiles) {

			// Get the file name
			String name = file.getName();

			// Do we have the 'MetaData' fodler?
			if (file.isDirectory()) {
				if (name.equalsIgnoreCase("MetaData")) {

					// Store the metadata folder
					metadataFolder = file;

					// Parse the metadata information
					if (!scanMetadataFolder()) {
						isValid = false;
						return isValid;
					}
					
					// Skip to the next iteration
					continue;

				} else {
					// We can only have one folder called 'MetaData'
					isValid = false;
					return isValid;
				}
			}

			// Extract the information
			Matcher m = p.matcher(name);
			if (m.find()) {

				// Get base name
				String currentBase = m.group(1);
				if (basename.equals("")) {
					basename = currentBase;
				} else if (currentBase.equals(basename)) {
					// Nothing to do
				} else {
					// More than one base name. Invalid Leica TIFF series
					isValid = false;
					return isValid;
				}

				try {

					// TODO: Use this information
					
					// The series index is not always defined
					int seriesNum = 0;

					// Plane number (z)
					int planeNum = Integer.parseInt(m.group(4));
					
					// Channel number
					int channelNum = Integer.parseInt(m.group(5));
					
					if (m.group(2) != null) {
						
						// Get the series number
						seriesNum = Integer.parseInt(m.group(3));
						
						// Build the key
						String key = "series_" + seriesNum;
						
						// Current series metadata
						HashMap<String, String> metadata;
						
						// Store the series index if not yet present
						if (attr.containsKey(key)) {
							
							metadata = attr.get(key);
							
						} else {
							
							// Create a new SeriesMetadata object 
							metadata = new HashMap<String, String>();
						
							// Read the file
							if (reader == null) {
								
								// Initialize the reader
								reader = new ImageProcessorReader(
										new ChannelSeparator(
										LociPrefs.makeImageReader()));
							}
							
							// Try to open the image file
							try {
								reader.setId(file.getCanonicalPath());
							} catch (FormatException e) {
								reader = null;
							} catch (IOException e) {
								reader = null;
							}

							// Store width and height
							int width = 0;
							int heigth = 0;
							String datatype = "Unknown";
							if (reader != null) {

								// Get width
								width = reader.getSizeX();
								
								// Get heigth
								heigth = reader.getSizeY();

								// Get datatype
								datatype = getDataType();
								
								// Now close the file
								try {
									reader.close();
								} catch (IOException e) {
									// Report
									System.err.println("Could not close file!");
								}

							}
							
							// Store the extracted values 
							metadata.put("sizeX", Integer.toString(width));
							metadata.put("sizeY", Integer.toString(heigth));
							metadata.put("datatype", datatype);
							
						}

						// Update the metadata object
						int numPlanes = getMetadataValueOrZero(metadata, "sizeZ");
						if ((planeNum + 1) > numPlanes) {
							metadata.put("sizeZ", Integer.toString(planeNum + 1));
						}

						int numChannels = getMetadataValueOrZero(metadata, "sizeC");
						if ((channelNum + 1) > numChannels) {
							metadata.put("sizeC", Integer.toString(channelNum + 1));
						}
						
						// TODO: Add time
						int TimeNum = 0;
						int numTimepoints = getMetadataValueOrZero(metadata, "sizeT");
						if ((TimeNum + 1) > numTimepoints) {
							metadata.put("sizeT", Integer.toString(TimeNum + 1));
						}
						
						// Now update the series map
						attr.put(key, metadata);
							
					}

				} catch (NumberFormatException n) {
					isValid = false;
					return isValid;
				}

			} else {

				// This should not happen -- unless there is an unexpected issue with
				// the regular expressions
				isValid = false;
				return isValid;
			}

		}

		// Mark success
		isValid = true;
		return isValid;

	}

	/**
	 * Return information regarding the file format.
	 * @return descriptive String for the Reader.
	 */
	@Override
	public String info() {
		return "Leica TIFF series (composite) file format.";
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override
	public String getType() {
		return "Leica TIFF Series";
	}

	/**
	 * Return the (representative) name of the composite microscopy dataset.
	 * @return the name of the composite microscopy dataset.
	 */		
	@Override
	public String getName() {
		return basename;
	}

	/**
	 * Return the folder that contains the composite microscopy dataset.
	 * @return the dataset folder.
	 */	
	@Override
	public File getFolder() {
		return folder;
	}

	/**
	 * Return the list of series indices extracted from the file names 
	 * @return List of series indices
	 */
	public List<Integer> getSeriesIndices() {
		List<Integer> indices = new ArrayList<Integer>();
		for (String key : attr.keySet()) {
			// Get the index from the series_{n} key
			indices.add(Integer.parseInt(key.substring(7)));
		}
		Collections.sort(indices);
		return indices;
	}
	
	/**
	 * Scans the MetadataFolder and stores all relevant information.
	 */
	private boolean scanMetadataFolder() {
		return true;
	}
	
	/**
	 * Return current value for given metadata entry, or zero if not in the map.
	 * @param metadata Map of string - string key:value pairs.
	 * @param key Name of the metadata value to query.
	 * @return the value for the requested metadata value, or zero if
	 * it is not in the map.
	 */
	private int getMetadataValueOrZero(Map<String, String> metadata, String key) {
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
}
