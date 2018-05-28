package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final static String REGEX_POS = "(y-tile: (?<y>\\d+)*(, )?)*(x-tile: (?<x>\\d+)*(, )?)*(z-stack: (?<z>\\d+))*";
    private final static String REGEX_TIME_NAME = ".*_time(?<time>\\d*)\\.tif{1,2}$";
    private final static String REGEX_TIME_FB = ".*_time_(.*)_\\(number_(?<time>\\d*)\\)\\.tif{1,2}$";
    private final static String REGEX_POS_NAME = ".*position(?<pos>\\d*)_time.*\\.tif{1,2}$";
    private final static String REGEX_POS_NAME_FB = ".*\\(pos_(?<pos>\\d*)\\).*$";

    /* Private instance variables */
    private static Pattern p_pos = Pattern.compile(REGEX_POS, Pattern.CASE_INSENSITIVE);
    private static Pattern p_time = Pattern.compile(REGEX_TIME_NAME, Pattern.CASE_INSENSITIVE);
    private static Pattern p_time_fb = Pattern.compile(REGEX_TIME_FB, Pattern.CASE_INSENSITIVE);
    private static Pattern p_pos_name = Pattern.compile(REGEX_POS_NAME, Pattern.CASE_INSENSITIVE);
    private static Pattern p_pos_name_fb = Pattern.compile(REGEX_POS_NAME_FB, Pattern.CASE_INSENSITIVE);

    private boolean isValid = false;

    private HashMap<String, String[]> csvTable = null;
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
        Map<String, String[]> csvTable = new HashMap<String, String[]>();
        csvTable = buildImagesCSVTable(this.folder + "/images.csv");
        if (csvTable.isEmpty()) {
            // The buildImagesCSVTable() function already set the
            // isValid flag and the errorMessage string.
            assert(isValid == false);
            return isValid;
        }

        // Initialize the series name mapper
        seriesNamesMapper = new HashMap<String, String>();

        // Now extract series and build metadata
        for (Map.Entry<String, String[]> entry : csvTable.entrySet()) {

            // Get current row
            String[] row = entry.getValue();

            int tileX = -1;
            int tileY = -1;
            int planeNum = -1;
            int timeNum = -1;
            String well = "";

            // First, get position information
            Matcher m_pos = p_pos.matcher(row[5]);
            if (m_pos.find()) {
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

            // Get the well
            well = row[4];

            // If the positional information could not be extracted from the corresponding
            // column, try to get it from the file name
            Matcher m_pos_name = p_pos_name.matcher(row[6]);
            if (m_pos_name.find()) {
                if (m_pos_name.group("pos") != null) {
                    Map<String, String> map = processPosFromFileName(m_pos_name.group("pos"));
                    if (tileX == -1 && map.get("tileX") != "") {
                        tileX = Integer.parseInt(map.get("tileX"));
                    }
                    if (tileY == -1 && map.get("tileY") != "") {
                        tileY = Integer.parseInt(map.get("tileY"));
                    }
                    if (planeNum == -1 && map.get("planeNum") != "") {
                        planeNum = Integer.parseInt(map.get("planeNum"));
                    }
                    if (well == "" && map.get("well") != "") {
                        well = map.get("well");
                    }
                }
            } else {
                // Try the fallback option
                Matcher m_pos_name_fb = p_pos_name_fb.matcher(row[6]);
                if (m_pos_name_fb.find()) {
                    if (m_pos_name_fb.group("pos") != null) {
                        Map<String, String> map = processPosFromFileName(m_pos_name_fb.group("pos"));
                        if (tileX == -1 && map.get("tileX") != "") {
                            tileX = Integer.parseInt(map.get("tileX"));
                        }
                        if (tileY == -1 && map.get("tileY") != "") {
                            tileY = Integer.parseInt(map.get("tileY"));
                        }
                        if (planeNum == -1 && map.get("planeNum") != "") {
                            planeNum = Integer.parseInt(map.get("planeNum"));
                        }
                        if (well == "" && map.get("well") != "") {
                            well = map.get("well");
                        }
                    }
                }
            }

            // Then, get time information
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

            // Build series ID from row (if present, use path information to build a unique id)
            String seriesID = "Well_" + well + "_Pos_" + tileX + "_" + tileY + "_Path_"
                    + pathInfoAsID(row[6]);

            // Build full file name
            File file = new File(this.folder + "/" + row[6]);

            // Make sure that the file exists
            if (! file.exists()) {
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
                        assert(bioformatsWrapper.getNumberOfSeries() == 1);

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
     * Parse the images.csv file and return a map of the content. Each row is stored with the
     * file name (with relative path) as its key.
     *
     * If something goes wrong while processing the file, this function will set the 'isValid'
     * flag to false, set the 'errorMessage' accordingly and return an empty map.
     *
     * @param fileName Full path to the images.csv file.
     * @return Hash map of strings with file name as key and array of string for each row.
     */
    private HashMap<String, String[]> buildImagesCSVTable(String fileName) {

        // Initialize the table
        csvTable = new HashMap<String, String[]>();

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
                    return new HashMap<String, String[]>();
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
            return new HashMap<String, String[]>();
        } catch (IOException e) {
            // Mark failure
            isValid = false;
            errorMessage = "Could not read file 'images.csv'!";
            return new HashMap<String, String[]>();
        } catch (Exception e) {
            // Mark failure
            isValid = false;
            errorMessage = "Could not read file 'images.csv'! The error was: " + e.getMessage();
            return new HashMap<String, String[]>();
        }

        // Change the linked hash set to a list
        channelNames = new ArrayList<String>(channelNamesSet);

        // Return the table
        return csvTable;

    }

    /**
     * Return an ID built from the path information of the file
     *
     * @param file
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
     * Process the position string extracted from the file name.
     *
     * @param pos
     *            A numeric string of the form '010101' (not strictly 6-character
     *            long).
     * @return array of positions x, y, z
     */

    private Map<String, String> processPosFromFileName(String pos) {

        // Initialize positions
        Map<String, String> map = new HashMap<String, String>();
        map.put("tileX", "");
        map.put("tileY", "");
        map.put("planeNum", "");
        map.put("well", "");

        if (pos == "") {
            return map;
        }

        int len = pos.length();
        if (len == 4) {
            // No well, no tiles, and no Z information (2D acquisition)
            map.put("tileX", "" + Integer.parseInt(pos.substring(0, 2)));
            map.put("tileY", "" + Integer.parseInt(pos.substring(2, 4)));
        } else if (len == 6 || len == 7) {
            // Note: the number of digits that encode the well are
            // hard-coded to 4. They do not have to be; unfortunately,
            // it is not possible to know how to break down the pos
            // string in its components. Usually, the well information
            // is stored in the well column of image.csv, and therefore
            // this information is not used.
            // No tiles and no Z information (2D acquisition)
            map.put("well", wellFromPosition(pos.substring(0,4)));
            map.put("planeNum", "" + Integer.parseInt(pos.substring(4)));
        } else if (len == 8) {
            map.put("well", wellFromPosition(pos.substring(0,4)));
            map.put("tileX", "" + Integer.parseInt(pos.substring(4, 6)));
            map.put("tileY", "" + Integer.parseInt(pos.substring(6, 8)));
        } else if (len == 10 || len == 11) {
            map.put("well", wellFromPosition(pos.substring(0,4)));
            map.put("tileX", "" + Integer.parseInt(pos.substring(4, 6)));
            map.put("tileY", "" + Integer.parseInt(pos.substring(6, 8)));
            map.put("planeNum", "" + Integer.parseInt(pos.substring(8)));
        } else {
            System.err.println("Unexpected 'pos' length!");
        }
        return map;
    }

    /**
     * Maps a position to a well.
     *
     * The position is a n-digit string, such as '0202' that maps to well B2.
     * The number of digits must be even, and the function will divide them in
     * two n/2 subsets.
     *
     * The row is given by one or more letters, the column by an integer:
     * e.g. 2712 maps to AA12.
     * @param pos n-digit string that encodes the well (e.g. '0202').
     * @return string representing the well (e.g. 'B2')
     */
    private String wellFromPosition(String pos) {

        // Number of digits
        int len_pos = pos.length();
        int sub_len = len_pos / 2;

        // Extract the 'row' part
        int row = Integer.parseInt(pos.substring(0, sub_len));
        if (row == 0) {
            return "";
        }

        // Extract the 'column' part
        int col = Integer.parseInt(pos.substring(sub_len, len_pos));

        // Row string
        String R = "";

        if (row <= 26) {
            R = (row - 1) >= 0 && (row - 1) < 26 ? String.valueOf((char) ((row - 1) + 65)) : "";
            return R + col;
        }

        // The row part of the well name if a made
        // of multiple letters

        // Number of digits
        double n_digits = Math.log(row) / Math.log(26);

        while (n_digits > 0) {

            // Step
            int step = (int)Math.pow(26, (int)n_digits);

            // Right-most letter
            int r = row / step;

            // Append the letter
            String R_tmp = (r - 1) >= 0 && (r - 1) < 26 ? String.valueOf((char) ((r - 1) + 65)) : "";
            R = R + R_tmp;

            // Now go to the next letter
            row = row - step;
            n_digits = n_digits - 1;
        }

        return R + col;

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
     * Builds the channel name from the relevant columns in the CSV table row.
     * @param row Current row
     * @return channel name.
     */
    private String buildChannelName(String [] row) {

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
     * Please note that some channels might be missing. One series could have channel 0 and 2 but no 1.
     * @param metadata Metadata map.
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
