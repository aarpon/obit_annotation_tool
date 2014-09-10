package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A composite reader for Leica TIFF series.
 * 
 * @author Aaron Ponti
 *
 */
public class LeicaTIFFSeriesReader extends AbstractCompositeMicroscopyReader {

	/* Protected instance variables */
	protected File folder;
	protected final String REGEX = "(.*?)_s(\\d.*?)_z(\\d.*?)_ch(\\d.*?)\\.tif";
	protected Pattern p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
    
	protected List<File> validFiles = new ArrayList<File>();
	protected File metadataFolder; 
	protected String basename = "";
	
	protected boolean isValid = false;
	
	// Constructor
	public LeicaTIFFSeriesReader(File folder) {

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
					int seriesNum = Integer.parseInt(m.group(2));
					int planeNum = Integer.parseInt(m.group(3));
					int channelNum = Integer.parseInt(m.group(4));

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

			// Valid file: append it to the list
			validFiles.add(file);
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
		return "composite microscopy";
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

}
