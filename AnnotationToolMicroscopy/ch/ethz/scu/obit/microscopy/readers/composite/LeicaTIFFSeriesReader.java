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
    
//	private volatile List<File> validFiles = new ArrayList<File>();
	private File metadataFolder; 
	private String basename = "";
	
	private boolean isValid = false;

	protected ImageProcessorReader reader = null;
	
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
								// Create the reader
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
							
							int width = 0;
							int heigth = 0;
							
							// Store width and height
							if (reader != null) {
								width = reader.getSizeX();
								heigth = reader.getSizeY();
								
								// Try closing it
								try {
									reader.close();
								} catch (IOException e) {
									// Report
									System.err.println("Could not close file!");
								}
							}
							
							metadata.put("SizeX", Integer.toString(width));
							metadata.put("SizeY", Integer.toString(heigth));
						}

						// Update the metadata object
						int numPlanes = getMetadataValueOrZero(metadata, "SizeZ");
						if ((planeNum + 1) > numPlanes) {
							metadata.put("SizeZ", Integer.toString(planeNum + 1));
						}

						int numChannels = getMetadataValueOrZero(metadata, "SizeC");
						if ((channelNum + 1) > numChannels) {
							metadata.put("SizeC", Integer.toString(channelNum + 1));
						}
						
						// TODO: Add time
						int TimeNum = 0;
						int numTimepoints = getMetadataValueOrZero(metadata, "SizeT");
						if ((TimeNum + 1) > numTimepoints) {
							metadata.put("SizeT", Integer.toString(TimeNum + 1));
						}
						
						// Now update the series map
						attr.put(key, metadata);
							
					}

				} catch (NumberFormatException n) {
					isValid = false;
					return isValid;
				}

			} else {

				// This should not happen -- unless Java regular expressions
				// are broken...
				isValid = false;
				return isValid;
			}

//			// Valid file: append it to the list
//			validFiles.add(file);
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

}
