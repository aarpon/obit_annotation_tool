package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import loci.plugins.util.ImageProcessorReader;


public class YouScopeReader extends AbstractCompositeMicroscopyReader {

	/* Private instance variables */
	private boolean isValid = false;
	private ImageProcessorReader reader = null;
	HashMap<String, String[]> images = null;

    // Constructor
    /**
     * @param folder Folder to be scanned.
     */
    public YouScopeReader(File folder) {

        this.folder = folder;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public File getFolder() {
        return folder;
    }

    @Override
    public List<Integer> getSeriesIndices() {
        return null;
    }

    @Override
    public boolean parse() throws Exception {

        // Parse the images.csv file
    	parseImagesCSV(this.folder + "/images.csv");
    	
        Iterator it = images.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        
        return false;
    }

    @Override
    public String info() {
        return "YouScope (composite) file format.";
    }

    @Override
    public String getType() {
        return "YouScope Experiment Reader";
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
    
    private void parseImagesCSV(String fileName) {
    	
    	// Instantiate map
    	images = new HashMap<String, String[]>();
    	
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
    			
    			// Store them
    			images.put(row[0], Arrays.copyOfRange(row, 1, row.length));

    			// Read next line 
    			line = br.readLine();

    		} 
    	} catch (FileNotFoundException e) {
			return;
		} catch (IOException e) {
			return;
		} 
    	return;
    }
    	
}
