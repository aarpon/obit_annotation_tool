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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

public class YouScopeReader extends AbstractCompositeMicroscopyReader {

    /* Protected instance variables */
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

    HashMap<String, String[]> csvTable = null;
    HashMap<String, String> seriesNamesMapper = null;

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

    public boolean parse() throws Exception {

        // Parse the images.csv file
        Map<String, String[]> csvTable = new HashMap<String, String[]>();
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
            reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
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
            String id = "";

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

            // Get the ID
            id = row[7];

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

            // Build series ID from row (if present, use path information to build a unique id)
            String seriesID = "Well_" + well + "_Pos_" + tileX + "_" + tileY + "_Path_"
                    + pathInfoAsID(row[6]);

            // Current series metadata
            HashMap<String, String> metadata;

            // Channel number
            int channelNum = 0;

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

                // Build full file name
                File file = new File(this.folder + "/" + row[6]);

                // Does the metadata already contain information that can only
                // be obtained by opening at least one file from the series?
                if (!metadata.containsKey("sizeX")) {

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
                    // with information extracted from the external
                    // metadata information.
                    metadata.put("voxelX", "NaN");
                    metadata.put("voxelY", "NaN");
                    metadata.put("voxelZ", "NaN");

                    // TODO: Get wavelengths from metadata

                    // Get acquisition time from metadata
                    metadata.put("acquisitionDate", row[3]);

                }

                // Add the the file size to the total size of the composite dataset.
                totalDatasetSizeInBytes += file.length();

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

            // Get the channel index
            channelNum = getChannelIndex(metadata, channelName);
            metadata.put("channelName" + channelNum, channelName);            
            int numChannels = getMetadataValueOrZero(metadata, "sizeC");
            if ((channelNum + 1) > numChannels) {
                metadata.put("sizeC", Integer.toString(channelNum + 1));
            }

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
     * Parse the images.csv file and return a table.
     * 
     * @param fileName
     *            Full path to the images.csv file.
     * @return 2D array of strings with the content of the images.csv file.
     */
    private HashMap<String, String[]> buildImagesCSVTable(String fileName) {

        // Initialize the table
        csvTable = new HashMap<String, String[]>();

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

                // Add the row with the file name as key
                csvTable.put(row[6], row);

                // Read next line
                line = br.readLine();

            }
        } catch (FileNotFoundException e) {
            return new HashMap<String, String[]>();
        } catch (IOException e) {
            return new HashMap<String, String[]>();
        } catch (Exception e) {
            return new HashMap<String, String[]>();
        }
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
            // Note: currently, it is assumed that 26 letters are enough to cover
            // all positions. I think, if there are more than 26 positions, the
            // letters will repeat: i.e., AA, ...
            // No tiles and no Z information (2D acquisition)
            int r = Integer.parseInt(pos.substring(0, 2)) - 1;
            int c = Integer.parseInt(pos.substring(2, 4));
            String row = r >= 0 && r < 26 ? String.valueOf((char) (r + 65)) : "";
            map.put("well", row + c);
            map.put("planeNum", "" + Integer.parseInt(pos.substring(4)));
        } else if (len == 8) {
            int r = Integer.parseInt(pos.substring(0, 2)) - 1;
            int c = Integer.parseInt(pos.substring(2, 4));
            String row = r >= 0 && r < 26 ? String.valueOf((char) (r + 65)) : "";
            map.put("well", row + c);
            map.put("tileX", "" + Integer.parseInt(pos.substring(4, 6)));
            map.put("tileY", "" + Integer.parseInt(pos.substring(6, 8)));
        } else if (len == 10 || len == 11) {
            int r = Integer.parseInt(pos.substring(0, 2)) - 1;
            int c = Integer.parseInt(pos.substring(2, 4));
            String row = r >= 0 && r < 26 ? String.valueOf((char) (r + 65)) : "";
            map.put("well", row + c);
            map.put("tileX", "" + Integer.parseInt(pos.substring(4, 6)));
            map.put("tileY", "" + Integer.parseInt(pos.substring(6, 8)));
            map.put("planeNum", "" + Integer.parseInt(pos.substring(8)));
        } else {
            System.err.println("Unexpected 'pos' length!");
        }
        return map;
    }

    /**
     * Return the index of the channel from the metadata map. If the channel does not exist, current
     * channel number is increased.
     * @param metadata Map of string - string key:value pairs.
     * @param channelName Name of the channel to search for in the metadata map.
     * @return the index of the channel that has given name; if not found, return max channel index + 1.
     */
    private int getChannelIndex(Map<String, String> metadata, String channelName) {

        // Initialize channels
        int channels = 0;

        // Are there channels already?
        if (metadata.containsKey("sizeC")) {
            channels = Integer.parseInt(metadata.get("sizeC"));
        } else {
            return channels;
        }

        // Is current channel 
        for (int i = 0; i < channels; i++) {
            if (metadata.get("channelName" + i).equals(channelName)) {
                return i;
            }
        }

        // We add a new channel
        return channels;
    }
}
