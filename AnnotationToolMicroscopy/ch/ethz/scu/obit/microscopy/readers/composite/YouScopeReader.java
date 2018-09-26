package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.scu.obit.microscopy.readers.BioFormatsWrapper;

public class YouScopeReader extends AbstractCompositeMicroscopyReader {

    /* Protected instance variables */
    private BioFormatsWrapper bioformatsWrapper = null;
    private File folder;
    private final static String REGEX_POS = "(position: (?<position>\\d+)*(, )?)*(y-tile: (?<y>\\d+)*(, )?)*(x-tile: (?<x>\\d+)*(, )?)*(z-stack: (?<z>\\d+))*";
    private final static String REGEX_TIME_NAME = ".*_time(?<time>\\d*)\\.tif{1,2}$";
    private final static String REGEX_TIME_FB = ".*_time_(.*)_\\(number_(?<time>\\d*)\\)\\.tif{1,2}$";
    private final static String REGEX_POS_NAME_FB = ".*\\(pos_(?<pos>\\d*)\\).*$"; // Fall-back for rare cases

    /* Private instance variables */
    private static Pattern p_pos = Pattern.compile(REGEX_POS, Pattern.CASE_INSENSITIVE);
    private static Pattern p_time = Pattern.compile(REGEX_TIME_NAME, Pattern.CASE_INSENSITIVE);
    private static Pattern p_time_fb = Pattern.compile(REGEX_TIME_FB, Pattern.CASE_INSENSITIVE);
    private static Pattern p_pos_name_fb = Pattern.compile(REGEX_POS_NAME_FB, Pattern.CASE_INSENSITIVE); // Fall-back for rare cases

    private boolean isValid = false;

    private LinkedHashMap<String, String[]> csvTable = null;
    private HashMap<String, String> seriesNamesMapper = null;
    private List<String> channelNames = null;

    /**
     * Constructor
     *
     * @param folder
     *            Folder to be scanned.
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
        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < attr.size(); i++) {
            indices.add(i);
        }
        return indices;
    }

    @Override
    public boolean parse() throws Exception {

        // Parse the images.csv file
        Map<String, String[]> csvTable = new LinkedHashMap<String, String[]>();
        csvTable = buildImagesCSVTable(this.folder + "/images.csv");
        if (csvTable.isEmpty()) {
            // The buildImagesCSVTable() function already set the
            // isValid flag and the errorMessage string.
            assert (isValid == false);
            return isValid;
        }

        // Initialize the series name mapper
        seriesNamesMapper = new HashMap<String, String>();

        // Now extract series and build metadata
        for (Map.Entry<String, String[]> entry : csvTable.entrySet()) {

            // Get current row
            String[] row = entry.getValue();

            // Coordinates
            int position = -1;
            int tileX = -1;
            int tileY = -1;
            int planeNum = -1;
            int timeNum = -1;
            String well = "";

            // Get the well (if present) from the Well column
            well = row[4];

            // Get the positions from the Position column
            Matcher m_pos = p_pos.matcher(row[5]);
            if (m_pos.find()) {
                if (m_pos.group("position") != null) {
                    position = Integer.parseInt(m_pos.group("position"));
                }
                if (m_pos.group("x") != null) {
                    tileX = Integer.parseInt(m_pos.group("x"));
                }
                if (m_pos.group("y") != null) {
                    tileY = Integer.parseInt(m_pos.group("y"));
                }
                if (m_pos.group("z") != null) {
                    planeNum = Integer.parseInt(m_pos.group("z"));
                }
            }

            // Try the fall-back option?
            Matcher m_pos_name_fb = p_pos_name_fb.matcher(row[6]);
            if (m_pos_name_fb.find()) {
                if (m_pos_name_fb.group("pos") != null) {
                    String pos = m_pos_name_fb.group("pos");
                    tileX = Integer.parseInt(pos.substring(0, 2));
                    tileY = Integer.parseInt(pos.substring(2, 4));
                }
            }

            // Get the timepoint
            Matcher m_time = p_time.matcher(row[6]);
            if (m_time.find()) {
                if (m_time.group("time") != null) {
                    timeNum = Integer.parseInt(m_time.group("time"));
                }
            } else {
                // Try the fallback option
                Matcher m_time_fb = p_time_fb.matcher(row[6]);
                if (m_time_fb.find()) {
                    if (m_time_fb.group("time") != null) {
                        timeNum = Integer.parseInt(m_time_fb.group("time"));
                    }
                }
            }

            // Fallback
            if (position == -1) {
                position = 1;
            }
            if (tileX == -1) {
                tileX = 1;
            }
            if (tileY == -1) {
                tileY = 1;
            }
            if (planeNum == -1) {
                planeNum = 1;
            }
            if (timeNum == -1) {
                timeNum = 1;
            }

            // Channel name
            String channelName = buildChannelName(row);

            // Build series ID from row (if present, use path information to build a unique
            // id)
            String seriesID = "Well_" + well + "_Pos_" + position + "_" + tileX + "_" + tileY + "_Path_" + pathInfoAsID(row[6]);

            // Build full file name
            File file = new File(this.folder + "/" + row[6]);

            // Make sure that the file exists
            if (!file.exists()) {
                // Mark failure
                isValid = false;
                errorMessage = "File '" + file + "' referenced in 'images.csv' does not exist!";
                return isValid;
            }

            // Add the the file size to the total size of the composite dataset.
            totalDatasetSizeInBytes += file.length();

            // Current series metadata
            HashMap<String, String> metadata;

            // Store the series index if not yet present
            if (seriesNamesMapper.containsKey(seriesID)) {

                // Get the attr key name
                String key = seriesNamesMapper.get(seriesID);

                // Get the metadata
                metadata = attr.get(key);

            } else {

                // Create a new SeriesMetadata object
                metadata = new HashMap<String, String>();

                // Add the unique series ID
                metadata.put("uniqueSeriesID", seriesID);

                // And add it to the attribute map
                int numSeries = seriesNamesMapper.size();
                String attrKey = "series_" + numSeries;
                seriesNamesMapper.put(seriesID, attrKey);
                attr.put(attrKey, metadata);

                // Does the metadata already contain information that can only
                // be obtained by opening at least one file from the series?
                if (!metadata.containsKey("sizeX")) {

                    // Try to open the first image file of the series
                    try {

                        // Initialize the BioFormatsWrapper
                        bioformatsWrapper = new BioFormatsWrapper(file, false);
                        bioformatsWrapper.parse();

                    } catch (Exception e) {

                        // Mark failure
                        isValid = false;
                        errorMessage = e.getMessage();
                        return isValid;

                    }

                    // Store some metadata
                    String width = "";
                    String heigth = "";
                    String datatype = "Unknown";
                    String voxelX = "NaN";
                    String voxelY = "NaN";
                    String voxelZ = "NaN";
                    if (bioformatsWrapper != null) {

                        // Get the attributes for current file
                        Map<String, HashMap<String, String>> currAttrs = bioformatsWrapper.getAttributes();

                        // Assertion: only one series in the file!
                        assert (bioformatsWrapper.getNumberOfSeries() == 1);

                        HashMap<String, String> seriesOneAttrs = currAttrs.get("series_0");

                        // Get width
                        width = seriesOneAttrs.get("sizeX");

                        // Get heigth
                        heigth = seriesOneAttrs.get("sizeY");

                        // Get datatype
                        datatype = bioformatsWrapper.getDataType();

                        // Get voxel size
                        voxelX = seriesOneAttrs.get("voxelX");
                        voxelY = seriesOneAttrs.get("voxelY");
                        voxelZ = seriesOneAttrs.get("voxelZ");

                        // Now close the file
                        bioformatsWrapper.close();
                        bioformatsWrapper = null;

                    }

                    // Store the extracted values
                    metadata.put("sizeX", width);
                    metadata.put("sizeY", heigth);
                    metadata.put("datatype", datatype);

                    // Store default values. These should be replaced
                    // with information extracted from the external
                    // metadata information.
                    metadata.put("voxelX", voxelX);
                    metadata.put("voxelY", voxelY);
                    metadata.put("voxelZ", voxelZ);

                    // Store the well
                    metadata.put("well", well);

                    // TODO: Get wavelengths from metadata

                    // Get acquisition time from metadata
                    metadata.put("acquisitionDate", row[3]);

                    // Give a more useful name to the series
                    String name = "";
                    if (!well.equals("")) {
                        name = attrKey + " (" + well + ")";
                    } else {
                        name = attrKey;
                    }
                    metadata.put("name", name);
                }

            }

            // Update the metadata object

            // Number of planes
            int numPlanes = getMetadataValueOrZero(metadata, "sizeZ");
            if (planeNum == 0) {
                planeNum = 1;
            }
            if (planeNum > numPlanes) {
                metadata.put("sizeZ", Integer.toString(planeNum));
            }

            // Find the channel number from the list of channel names
            int channelNum = channelNames.indexOf(channelName);
            metadata.put("channelName" + channelNum, channelName);

            // Update the count of channels
            int numChannels = countChannelsInMetadata(metadata);
            metadata.put("sizeC", Integer.toString(numChannels));

            // Number of timepoints
            int numTimepoints = getMetadataValueOrZero(metadata, "sizeT");
            if (timeNum == 0) {
                timeNum = 1;
            }
            if (timeNum > numTimepoints) {
                metadata.put("sizeT", Integer.toString(timeNum));
            }

        }

        // Make sure that all files in the folder are referenced
        if (!allFilesInTable(this.folder)) {
            // The allFilesInTable() function already marked failure and set the
            // error message accordingly.
            assert (isValid == false);
            return isValid;
        }

        // Mark success
        isValid = true;
        errorMessage = "";
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
     * Return true if the composite microscopy dataset could be parsed successfully.
     *
     * @return true if the composite microscopy dataset could be parsed
     *         successfully, false otherwise.
     */
    @Override
    public boolean isParsed() {
        return isValid;
    }

    /**
     * Return true if the reader can parse the passed folder
     *
     * @param folder
     *            Folder to be parsed.
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
     * Parse the images.csv file and return a map of the content. Each row is stored
     * with the file name (with relative path) as its key.
     *
     * If something goes wrong while processing the file, this function will set the
     * 'isValid' flag to false, set the 'errorMessage' accordingly and return an
     * empty map.
     *
     * @param fileName
     *            Full path to the images.csv file.
     * @return Linked Hash map of strings with file name as key and array of string for
     *         each row.
     */
    private LinkedHashMap<String, String[]> buildImagesCSVTable(String fileName) {

        // Initialize the table
        csvTable = new LinkedHashMap<String, String[]>();

        // Initialize a linked hash set
        LinkedHashSet<String> channelNamesSet = new LinkedHashSet<String>();

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

                // Make parsing robust against empty lines
                if (row.length == 0) {

                    // Read next line
                    line = br.readLine();
                    continue;
                }

                // Remove '"' and '\' characters if needed
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].replace("\"", "");
                    row[i] = row[i].replace("\\\\", "\\");
                    row[i] = row[i].replace("\\", "/");
                }

                // Check that the file was not erroneously added twice to the images.csv file
                if (csvTable.containsKey(row[6])) {
                    // Mark failure
                    isValid = false;
                    errorMessage = "File '" + row[6] + "' referenced more than once in 'images.csv'!";
                    return new LinkedHashMap<String, String[]>();
                }

                // Add the row with the file name as key
                csvTable.put(row[6], row);

                // Add the (composed) channel to the channelNames linked hash set
                channelNamesSet.add(buildChannelName(row));

                // Read next line
                line = br.readLine();

            }
        } catch (FileNotFoundException e) {
            // Mark failure
            isValid = false;
            errorMessage = "File 'images.csv' not found!";
            return new LinkedHashMap<String, String[]>();
        } catch (IOException e) {
            // Mark failure
            isValid = false;
            errorMessage = "Could not read file 'images.csv'!";
            return new LinkedHashMap<String, String[]>();
        } catch (Exception e) {
            // Mark failure
            isValid = false;
            errorMessage = "Could not read file 'images.csv'! The error was: " + e.getMessage();
            return new LinkedHashMap<String, String[]>();
        }

        // Change the linked hash set to a list
        channelNames = new ArrayList<String>(channelNamesSet);

        // Return the table
        return csvTable;

    }

    /**
     * Make sure that all image files are referenced in the csv file.
     *
     * @param currentFolder Full path to the folder to preocess.
     * @return true if all files are referenced, false otherwise.
     */
    private boolean allFilesInTable(File currentFolder) {

        // Get relative path
        String relativePath;
        if (currentFolder.equals(folder)) {
            relativePath = "";
        } else {
            try {
                int index = this.folder.toString().length();
                relativePath = currentFolder.toString().substring(index + 1);
                // Only forward slashes allowed
                relativePath = relativePath.replace("\\\\", "\\");
                relativePath = relativePath.replace("\\", "/");
            } catch (Exception e) {
                relativePath = "";
            }
        }

        // Initialize result
        boolean result = true;

        // Get and check all image files, recursively
        File[] fList = currentFolder.listFiles();
        for (File file : fList) {
            if (file.isFile()) {

                String filename = file.getName();
                String filenameForCheck = filename.toLowerCase();
                if (filenameForCheck.endsWith(".tif") || filenameForCheck.endsWith(".tiff")) {

                    // Relative path to test
                    String relativePathToTest = "";
                    if (relativePath.equals("")) {
                        relativePathToTest = filename;
                    } else {
                        // Only forward slashes allowed
                        relativePathToTest = relativePath + "/" + filename;
                    }

                    if (!csvTable.containsKey(relativePathToTest)) {
                        // Mark as invalid
                        isValid = false;
                        errorMessage = "File " + relativePathToTest + " is not referenced in images.csv!";
                        return isValid;
                    }
                }
            } else if (file.isDirectory()) {
                result = allFilesInTable(file);
            }
        }
        return result;
    }

    /**
     * Return an ID built from the path information of the file
     *
     * @param filename
     *            Image file with (relative) path as obtained from images.csv
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

    /**
     * Return current value for given metadata entry, or zero if not in the map.
     *
     * @param metadata
     *            Map of string - string key:value pairs.
     * @param key
     *            Name of the metadata value to query.
     * @return the value for the requested metadata value, or zero if it is not in
     *         the map.
     */
    protected int getMetadataValueOrZero(Map<String, String> metadata, String key) {
        int value = 0;
        if (metadata.containsKey(key)) {
            value = Integer.parseInt(metadata.get(key));
        }
        return value;
    }

    /**
     * Builds the channel name from the relevant columns in the CSV table row.
     *
     * @param row
     *            Current row
     * @return channel name.
     */
    private String buildChannelName(String[] row) {

        // Get the ID
        String id = row[7];

        // Get the various parts that compose the channel name
        String channelName;
        if (row[9].equals("") && row[10].equals("")) {
            channelName = "undefined";
        } else if (!row[9].equals("") && row[10].equals("")) {
            channelName = row[9];
        } else if (row[9].equals("") && !row[10].equals("")) {
            channelName = row[10];
        } else {
            channelName = row[9] + "_" + row[10];
        }
        if (!id.equals("")) {
            channelName = id + "_" + channelName;
        }

        return channelName;
    }

    /**
     * Count the number of channels in the metadata.
     *
     * Please note that some channels might be missing. One series could have
     * channel 0 and 2 but no 1.
     *
     * @param metadata
     *            Metadata map.
     * @return number of channels in the series.
     */
    private int countChannelsInMetadata(Map<String, String> metadata) {
        int numChannels = 0;
        for (String key : metadata.keySet()) {
            if (key.startsWith("channelName")) {
                numChannels += 1;
            }
        }
        return numChannels;
    }
}
