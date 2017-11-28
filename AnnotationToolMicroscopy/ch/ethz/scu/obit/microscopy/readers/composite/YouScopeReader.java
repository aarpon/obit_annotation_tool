package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;


public class YouScopeReader extends AbstractCompositeMicroscopyReader {

	/* Private instance variables */
	private boolean isValid = false;

	HashMap<String, Series> images = null;
	HashMap<Integer, String[]> csvTable = null;

    // Constructor
    /**
     * @param folder Folder to be scanned.
     */
    public YouScopeReader(File folder) {

    	// Store the root folder
        this.folder = folder;
        
        // Reset the total file size in bytes
        totalDatasetSizeInBytes = 0;
    }

    @Override
    public String getName() {
        return folder.getName();
    }

    @Override
    public File getFolder() {
        return folder;
    }

    @Override
    public List<Integer> getSeriesIndices() {
    	// TODO Implement me!
    	List<Integer> indices = new ArrayList<Integer>();
        return indices;
    }

    
    public boolean parse() throws Exception {
    	
        // Parse the images.csv file
    	Map<Integer, String[]> csvTable = new HashMap<Integer, String[]>();
    	csvTable = buildImagesCSVTable(this.folder + "/images.csv"); 
    	if (csvTable.isEmpty()) {
    		// Mark failure
			isValid = false;
			errorMessage = "Could not find and/or process 'images.csv' file.";
			return isValid;
    	}

		// Now process the files
		if (reader == null) {

			// Initialize the reader
			reader = new ImageProcessorReader(
					new ChannelSeparator(
							LociPrefs.makeImageReader()));
		}

    	// Now extract series and build metadata
    	for (Map.Entry<Integer, String[]> entry : csvTable.entrySet()) {

    		// Get current row
    		String[] row = entry.getValue();
    		
    		// Build series ID from row as
    		// (if present, use path information to build a unique id)
    		String seriesID = row[7] + "_" + row[4] + "_" +
    				row[0] + "_" + pathInfoAsID(row[6]);
    		
    		// Parse the position column
    		System.out.println(row[5]);
    		int numPlanes = 0;
    		int numTimePoints = 0;
    		int numChannels = 0;
    		
			// Current series metadata
			HashMap<String, String> metadata;

			// Store the series index if not yet present
			if (attr.containsKey(seriesID)) {

				// Get the metadata
				metadata = attr.get(seriesID);

			} else {

				// Create a new SeriesMetadata object 
				metadata = new HashMap<String, String>();

				// Build full file name
				File file = new File(this.folder + "/" + row[6]);

				// Does the metadata already contain information that can only
				// be obtained by opening at least one file from the series?
				if (! metadata.containsKey("sizeX")) {

		    		// Try to open the first image file of the series
		    		try {
		    			
		    			// Open the file
		    			reader.setId(file.getCanonicalPath());

		    		} catch (FormatException e) {

		    			// Mark failure
		    			isValid = false;
		    			errorMessage = "Could not open file '" + file + "'.";
		    			return isValid;

		    		} catch (IOException e) {

		    			// Mark failure
		    			isValid = false;
		    			errorMessage = "Could not open file '" + file + "'.";
		    			return isValid;

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

					// Store default values. These should be replaced
					// with information extracted from the properties XML
					// file in the Metadata folder.
					metadata.put("voxelX", "1.0");
					metadata.put("voxelY", "1.0");
					metadata.put("voxelZ", "1.0");

					// TODO: Get wavelengths from metadata

					// TODO: Get acquisition time from metadata
					metadata.put("acquisitionDate", "NaN");
		    		
				}
	    		
				// Add the the file size to the total size of the composite dataset.
				totalDatasetSizeInBytes += file.length();
				
				// And add it to the attribute map
				attr.put(seriesID, metadata);

			}

    	}

		// Mark success
		isValid = true;
		errorMessage = "";
		return isValid;
    }
    
    public boolean parse_old() throws Exception {

        // Parse the images.csv file
    	if (! parseImagesCSV(this.folder + "/images.csv")) {

    		// Mark failure
			isValid = false;
			errorMessage = "Could not find and/or process 'images.csv' file.";
			return isValid;
    	}
    	
    	// Build the metadata information
    	for (Map.Entry<String, Series> entry : images.entrySet()) {
    	    
    		String seriesID = entry.getKey();
    		Series series = entry.getValue();
    		
    		// Read the first file in the series to collect metadata information
    		if (reader == null) {

    			// Initialize the reader
    			reader = new ImageProcessorReader(
    					new ChannelSeparator(
    							LociPrefs.makeImageReader()));
    		}

    		// Try to open the first image file of the series
    		try {
    			
    			// Open the file
    			reader.setId(series.getFirstFileInSeries().getCanonicalPath());

    		} catch (FormatException e) {

    			// Mark failure
    			isValid = false;
    			errorMessage = "Could not open file '" + series.getFirstFileInSeries() + "'.";
    			return isValid;

    		} catch (IOException e) {

    			// Mark failure
    			isValid = false;
    			errorMessage = "Could not open file '" + series.getFirstFileInSeries() + "'.";
    			return isValid;

    		}
    	}

    	// Return success
    	isValid = true;
        return isValid;
    }

    @Override
    public String info() {
        return "YouScope (composite) file format.";
    }

    @Override
    public String getType() {
        return "YouScope Experiment";
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

    /**
     * Return true if the reader can parse the passed folder
     * @param folder Folder to be parsed.
     * @return true if the folder can be parsed, false otherwise.
     */
    static public boolean canRead(File folder) {

    	boolean foundConfigurationCSB = false;
    	boolean foundImagesCSV = false;
    	boolean foundYSConfigMicroscopeCFG = false;
    	
        // Get a list of all files
        File[] allFiles = folder.listFiles();
		if (allFiles == null) {
			return false;
		}
        
        for (File file : allFiles) {
        	
        	if (file.getAbsolutePath().toLowerCase().endsWith("configuration.csb")) {
        		foundConfigurationCSB = true;
        	} else if (file.getAbsolutePath().toLowerCase().endsWith("images.csv")) {
        		foundImagesCSV = true;
        	} else if (file.getAbsolutePath().toLowerCase().endsWith("ysconfig_microscope.cfg")) {
        		foundYSConfigMicroscopeCFG = true;
        	} else {
        		continue;
        	}
        }
        
        return foundConfigurationCSB & foundImagesCSV & foundYSConfigMicroscopeCFG;
    }
  
    /**
     * Parse the images.csv file and return a table.
     * @param fileName Full path to the images.csv file.
     * @return 2D array of strings with the content of the images.csv file.
     */
    private HashMap<Integer, String[]> buildImagesCSVTable(String fileName) {
    	
    	// Initialize the table
    	csvTable = new HashMap<Integer, String[]>();
    	
       	// Header
    	boolean isHeader = true;

    	// Row number
    	int rowNumber = 0;
    	
    	// Read the CSV file
    	try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

    		// read the first line from the text file 
    		String line = br.readLine();

    		// loop until all lines are read 
    		while (line != null) {

    			if (isHeader == true) {
    				
    				// We are past the header
    				isHeader = false;
    				
        			// Read next line 
        			line = br.readLine();
        			
    				continue;
    			}
    			
    			// Get all values for current row
    			String[] row = line.split(";");
    			
    			// Remove '"' and '\' characters if needed
    			for (int i = 0; i < row.length; i++) {
    				row[i] = row[i].replace("\"", "");
    				row[i] = row[i].replace("\\\\", "\\");
    				row[i] = row[i].replace("\\", "/");
    			}
    			
    			// Add the row
    			csvTable.put(rowNumber, row);

    			// Read next line 
    			line = br.readLine();
    			
    			// Update row number
    			rowNumber++;

    		} 
    	} catch (FileNotFoundException e) {
			return new HashMap<Integer, String[]>();
		} catch (IOException e) {
			return new HashMap<Integer, String[]>();
		}
    	return csvTable;
    	
    }
    
    /**
     * Parse the images.csv file
     * @param fileName Full path to the images.csv file.
     * @return True if the file could be processed successfully, false otherwise.
     */
    private boolean parseImagesCSV(String fileName) {
    	
    	// Instantiate map
    	images = new HashMap<String, Series>();
    	
    	// Header
    	boolean isHeader = true;

    	// Read the CSV file
    	try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

    		// read the first line from the text file 
    		String line = br.readLine();

    		// loop until all lines are read 
    		while (line != null) {

    			if (isHeader == true) {
    				
    				// We are past the header
    				isHeader = false;
    				
        			// Read next line 
        			line = br.readLine();
        			
    				continue;
    			}
    			
    			// Get all values for current row
    			String[] row = line.split(";");
    			
    			// Remove " characters if needed
    			for (int i = 0; i < row.length; i++) {
    				row[i] = row[i].replace("\"", "");
    			}
    			
    			// Build a unique (hopefully) series if
    			String seriesID = row[0] + "_" + row[4] + "_" + row[7];
    			
    			// Store them
    			Series series;
    			if (images.containsKey(seriesID)) {
    				series = images.get(seriesID);
    			} else {
    				System.out.println("Found on new Series: " + seriesID);
    				series = new Series(this.folder);
    			}
    			
    			// Append the file
				series.addFileName(row[6]);
				
				// Store the updated image
				images.put(seriesID, series);
				
    			// Read next line 
    			line = br.readLine();

    		} 
    	} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
    	return true;
    }
    
    /**
     * Return an ID built from the path information of the file
     * @param file Image file with (relative) path as obtained from images.csv
     * @return ID built from the path information of the file.
     */
	private String pathInfoAsID(String filename) {
		
		// Initialize output
		String pathInfo = "";
		
		// We work with a relative path
		filename = filename.replace("\\\\", "\\");
		filename = filename.replace("\\", "/");
		int pos = filename.lastIndexOf("/");
		if (pos != -1) {
			pathInfo = filename.substring(0, pos);
			pathInfo = pathInfo.replace("/", "_");
		} else {
			pathInfo = ".";
		}
		
		return pathInfo;
	}


    private class Series {
    	
    	private File folder;
    	private List<String> filenames;

    	/**
    	 * Constructor
    	 * @param rootFolder Full path to the root folder of the dataset.
    	 */
    	public Series(File rootFolder) {
    		
    		// Store the root folder
    		this.folder = rootFolder;
    		
    		// Initialize the list of filenames
    		filenames = new ArrayList<String>();
		}
    	
    	/**
    	 * Add filename to current series and updates to global 
    	 * total size in bytes of the dataset.
    	 * 
    	 * @param filename File name with full path.
    	 */
    	public void addFileName(String filename) {
    		
    		// Add the filename
    		filenames.add(filename);
    		
    		// Update the total file size (in the container class)
    		File file = new File(this.folder + "/" + filename);
    		totalDatasetSizeInBytes += file.length();
    	}
    	
    	/**
    	 * Get the first file name of the series.
    	 * @return First File ine the series.
    	 */
    	public File getFirstFileInSeries() {
    		if (filenames.isEmpty()) {
    			return new File("");
    		} else {
    			return new File(this.folder + "/" + filenames.get(0));
    		}
    	}
   	
    }
    	
}
