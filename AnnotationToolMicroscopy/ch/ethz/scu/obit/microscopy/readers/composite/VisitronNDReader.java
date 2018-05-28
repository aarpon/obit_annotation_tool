package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import java.util.regex.Pattern;
import ch.ethz.scu.obit.microscopy.readers.BioFormatsWrapper;


/**
 * A composite reader for Visitron ND files (STK or TIFFs).
 *
 * @author Aaron Ponti
 *
 */
public class VisitronNDReader extends AbstractCompositeMicroscopyReader {

    private final static String FILENAME_REGEX =
            "^(?<basename>.*?)" +                            // Series basename: group 1
                    "(_w(?<channel>\\d.*?)" +                // Channel number (optional)
                    "(?<channelname>.*?))?" +                // Channel name (optional)
                    "(conf(?<wavelength>\\d.*?))?" +         // Wavelength
                    "(_s(?<series>\\d.*?))?" +               // Series number (optional)
                    "(_t(?<timepoint>\\d.*?))?" +            // Time index (optional)
                    "(\\.tif{1,2}|\\.stk)$";                 // File extension

    private final static String ATTR_REGEX =
            "^(?<key>.*?)((?<channel>\\d*?))?$";             // Name of the attribute followed by an optional channel number

    private static Pattern FILENAME_PATTERN = Pattern.compile(FILENAME_REGEX, Pattern.CASE_INSENSITIVE);
    private static Pattern ATTR_PATTERN = Pattern.compile(ATTR_REGEX, Pattern.CASE_INSENSITIVE);

    /* Protected instance variables */
    private File folder;
    private String basename = "";
    private boolean isValid = false;

    BioFormatsWrapper bioformatsWrapperForNDFile =  null;
    Map<String, HashMap<String, String>> combinedAttr;
    List<Integer> combinedSeriesIndices;

    // Constructor
    /**
     * @param folder Folder to be scanned.
     */
    public VisitronNDReader(File folder) {

        this.folder = folder;

        // Initialize the combined attributes and series indices
        combinedAttr = new HashMap<String, HashMap<String, String>>();
        combinedSeriesIndices = new ArrayList<Integer>();
    }

    /**
     * Return true if the reader can parse the passed folder
     * @param folder Folder to be parsed.
     * @return true if the folder can be parsed, false otherwise.
     */
    static public boolean canRead(File folder) {

        boolean NDFileFound = false;
        boolean dataFilesFound = false;

        // Get a list of all files
        File[] allFiles = folder.listFiles();

        for (File file : allFiles) {

            // If we have found the ND and STK files we return
            if (NDFileFound == true && dataFilesFound == true) {
                return true;
            }

            // Get the file name
            String name = file.getName();

            // Do we have an ND or an STK file?
            if (name.toUpperCase().endsWith(".ND")) {
                NDFileFound = true;
                continue;
            }

            if (name.toUpperCase().endsWith(".TIF") ||
                    name.toUpperCase().endsWith(".TIFF") ||
                    name.toUpperCase().endsWith(".STK")) {
                dataFilesFound = true;
                continue;
            }

        }

        // If we have found the ND and STK files we return
        if (NDFileFound == true && dataFilesFound == true) {
            return true;
        }

        return false;

    }

    /**
     * Return true if the composite microscopy dataset could be parsed
     * successfully.
     * @return true if the composite microscopy dataset could be parsed
     * successfully, false otherwise.
     */
    @Override
    public boolean isParsed() {
        return isValid;
    }

    /**
     * Parse the folder.
     *
     * The metadata information is all stored in the .ND file.
     *
     * @return true if the parsing was successful, false otherwise.
     */
    @Override
    public boolean parse() throws Exception {

        // Initialize the list of found and referenced files
        List<String> foundFiles = new ArrayList<String>();

        // Make sure there is only one ND file in the folder
        Boolean NDFileAlreadyFound = false;

        // Get a list of all files
        File[] allFiles = folder.listFiles();
        if (allFiles == null) {

            // Mark failure
            isValid = false;
            errorMessage = "The folder is empty.";
            return isValid;

        }

        // Now process them
        for (File file : allFiles) {

            // Get the file name
            String name = file.getName();

            // TIF|TIFF or STK files are expected
            if (name.toUpperCase().endsWith(".TIF") ||
                    name.toUpperCase().endsWith(".TIFF") ||
                    name.toUpperCase().endsWith(".STK")) {

                // Add the file size to the total
                totalDatasetSizeInBytes += file.length();

                // Add the file to the list of found binary files
                foundFiles.add(file.getAbsolutePath());

                // Continue to the next file
                continue;

            } else if (name.toUpperCase().endsWith(".ND")) {

                // The ND file is the one we process to extract metadata information.
                // If something goes wrong in the parsing, the parseNDFile() method
                // will set the isValid flag to false and the errorMessage string
                // appropriately.


                if (NDFileAlreadyFound == true) {

                    isValid = false;
                    errorMessage = "Only one experiment (i.e. one ND file) per folder expected!";
                    return isValid;
                }

                NDFileAlreadyFound = true;

                // Set the "basename"
                String NDFileName = file.getName();
                basename = NDFileName.substring(0, NDFileName.length() - 3);

                // If parsing of the ND file succeeded, we still continue
                // iterating over the files to update the total dataset size!

            } else {

                // These are non-data files that we will just store in the DSS
                // with the composite dataset folder.
                // Nothing to do.
            }

        }

        // Make sure we found some files to process
        if (foundFiles.size() == 0) {
            // Mark failure
            isValid = false;
            errorMessage = "Could not find any image files in the folder!";
            return isValid;
        }

        // Now arrange the found files into series
        if (! buildSeriesFromFiles(foundFiles)) {
            // The status and the error message have been set by buildSeriesFromFiles()
            return false;
        }

        // Mark success
        isValid = true;
        errorMessage = "";
        return isValid;

    }

    /**
     * Return information regarding the file format.
     * @return descriptive String for the Reader.
     */
    @Override
    public String info() {
        return "Visitron ND (composite) file format.";
    }

    /**
     * Return a simplified class name to use in XML.
     * @return simplified class name.
     */
    @Override
    public String getType() {
        return "Visitron ND";
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
     * Return the list of series indices
     * @return List of series indices
     */
    @Override
    public List<Integer> getSeriesIndices() {
        return combinedSeriesIndices;
    }

    /**
     * Returns the (metadata) attributes stored after parsing the file
     * String-String map
     */
    @Override
    public Map<String, HashMap<String, String>> getAttributes() {

        // Return the attributes
        return combinedAttr;
    }

    /**
     * Returns the last error message.
     * @return String containing the last error message.
     */
    public String getLastError() {
        return errorMessage;
    }

    /**
     * Merge two series attributes.
     * @param attrTarget First series attribute to be extended.
     * @param attrSource Second series attributes to be integrated.
     * @return Merged series attributes.
     */
    private Map<String, HashMap<String, String>> mergeSeriesAttr(Map<String, HashMap<String, String>> attrTarget,
            Map<String, HashMap<String, String>> attrSource) {

        // Only one series per file is expected (and allowed)
        assert(attrTarget.keySet().size() == 1);
        assert(attrSource.keySet().size() == 1);

        Iterator<Entry<String, HashMap<String, String>>> targetIt = attrTarget.entrySet().iterator();
        Iterator<Entry<String, HashMap<String, String>>> sourceIt = attrSource.entrySet().iterator();

        while (targetIt.hasNext()) {

            // Get the series from the target
            Entry<String, HashMap<String, String>> targetPair = targetIt.next();
            String targetKey = targetPair.getKey();
            HashMap<String, String> targetSeries = targetPair.getValue();

            // Number of channels in the target series
            int numChannelsInTarget = countChannelsInMetadata(targetSeries);

            while (sourceIt.hasNext()) {

                // Get the series from the source
                Entry<String, HashMap<String, String>> sourcePair = sourceIt.next();
                String sourceKey = sourcePair.getKey();
                HashMap<String, String> sourceSeries = sourcePair.getValue();

                // Number of channels in the source series. It MUST be 1.
                int numChannelsInSource = countChannelsInMetadata(sourceSeries);
                assert(numChannelsInSource == 1);

                // Make sure we are trying to fuse attributes for the same series!
                assert(sourceKey.equals(targetKey));

                Set<String> keySetSource = sourceSeries.keySet();

                // If the channel name is the same, we fuse by time; otherwise by channel
                String channelNameTarget = targetSeries.get("channelName0");
                String channelNameSource = sourceSeries.get("channelName0");

                // Add by channel or by time point?
                Boolean addAsNewChannel = false;
                if (! channelNameTarget.equals(channelNameSource)) {
                    addAsNewChannel = true;
                }

                // Do we have a new channel to add?
                if (addAsNewChannel == true) {

                    // Check geometry. These test should pass since the geometries per series
                    // were checked by fileGeometryInSeriesMatch().
                    assert(Integer.parseInt(targetSeries.get("sizeX")) == Integer.parseInt(sourceSeries.get("sizeX")));
                    assert(Integer.parseInt(targetSeries.get("sizeY")) == Integer.parseInt(sourceSeries.get("sizeY")));
                    assert(Integer.parseInt(targetSeries.get("sizeZ")) == Integer.parseInt(sourceSeries.get("sizeZ")));
                    assert(Integer.parseInt(targetSeries.get("sizeT")) == Integer.parseInt(sourceSeries.get("sizeT")));

                    // New channel number (for the channel to be transferred)
                    int newChannelNum = numChannelsInTarget;

                    // This is a new channel
                    for (String key : keySetSource) {

                        Matcher m = ATTR_PATTERN.matcher(key);
                        if (m.find()) {
                            // Add a new key to the target attributes with the value of
                            // the source attributes
                            if (! m.group("channel").equals("")) {
                                String updateKey = m.group("key") + newChannelNum;
                                int currentSourceChannel = Integer.parseInt(m.group("channel"));
                                if (m.group("key").equals("channelColor")) {

                                    // Source channel
                                    double [] defC = BioFormatsWrapper.getDefaultChannelColor(currentSourceChannel);
                                    String c  = "";
                                    for (int k = 0; k < defC.length - 1; k++) {
                                        c += defC[k] + ", ";
                                    }
                                    c += defC[defC.length - 1];
                                    targetSeries.put(key, c);

                                    // Target channel
                                    defC = BioFormatsWrapper.getDefaultChannelColor(newChannelNum);
                                    c  = "";
                                    for (int k = 0; k < defC.length - 1; k++) {
                                        c += defC[k] + ", ";
                                    }
                                    c += defC[defC.length - 1];
                                    targetSeries.put(updateKey, c);

                                } else {
                                    targetSeries.put(updateKey, sourceSeries.get(key));
                                }
                            }
                        }
                    }

                    // Do we have a new timepoint to add?
                    targetSeries.put("sizeC", "" + (numChannelsInTarget + 1));

                } else {

                    // Number of timepoints in the source and target series
                    int numTimepointsInSource = Integer.parseInt(sourceSeries.get("sizeT"));
                    int numTimepointsInTarget = Integer.parseInt(targetSeries.get("sizeT"));
                    targetSeries.put("sizeT", "" + (numTimepointsInTarget + numTimepointsInSource));
                }

                // Merge the filenames
                targetSeries.put("filenames", sourceSeries.get("filenames") + ";" +
                        targetSeries.get("filenames"));

            }

        }

        return attrTarget;
    }

    /**
     * Set the series number in place.
     * @param attr Current attribute series.
     * @param targetSeriesNum Number of the series to set.
     *
     */
    private Map<String, HashMap<String, String>> setSeriesNumber(Map<String, HashMap<String, String>> attr, int targetSeriesNum) {

        // Make sure to set the final series number
        HashMap<String, String> entries = attr.remove("series_0");
        if (entries.containsKey("name")) {
            if (entries.get("name").toLowerCase().equals("series_0")) {
                entries.put("name", "series_" + targetSeriesNum);
            }
        }
        attr.put("series_" + targetSeriesNum, entries);
        return attr;
    }

    /**
     * Build Series objects from the list of files.
     * @param listOfFiles List of files to process
     * @return true if the parsing of the files into series was successful, false otherwise.
     */
    private Boolean buildSeriesFromFiles(List<String> listOfFiles) throws Exception {

        // Create a map of file attributes
        Map<String, Map<String, HashMap<String, String>>> fileAttributes =
                new HashMap<String, Map<String, HashMap<String, String>>>();

        // Create a map of series numbers to list of file names
        Map<Integer, ArrayList<String>> seriesMap = new HashMap<Integer, ArrayList<String>>();

        // Keep track of the file basenames
        List<String> basenames = new ArrayList<String>();

        // Set the first candidate series number - 1 to enter the loop
        int currentNewSeriesIndex = -1;

        // Process all files
        for (int i = 0; i < listOfFiles.size(); i++) {

            // Open the file
            BioFormatsWrapper reader = new BioFormatsWrapper(new File(listOfFiles.get(i)), false);

            // Parse the file
            reader.parse();

            // Get the attributes
            Map<String, HashMap<String, String>> currAttr = reader.getAttributes();

            // Add the filename
            currAttr.get("series_0").put("filenames", listOfFiles.get(i));

            // Store the updated attributes
            fileAttributes.put(listOfFiles.get(i), currAttr);

            // Close the file
            reader.close();

            // Default basename for current file
            String basename = "default";

            // Default channel number for current file (unused)
            //int channel = 0;

            // Default channel name for current file (unused)
            //String  channelName = "";

            // Default wavelength (string) for current file (unused)
            // String wavelength = "";

            // Default timepoint for current file (unused)
            int timepoint = 0;

            // Series number
            int seriesNum = 0;

            // First parse the file name to extract some information
            Matcher m = FILENAME_PATTERN.matcher(listOfFiles.get(i));
            if (m.find()) {

                // Get the base name
                if (m.group("basename") != null && m.group("basename") != "") {
                    basename = m.group("basename");
                }

                // Does it exist in the baseNames list already? If not, increase
                // the series number
                if (basenames.indexOf(basename) == -1) {

                    // Increase the series index
                    currentNewSeriesIndex += 1;

                    // Add the new basename to the list
                    basenames.add(basename);
                }

                // Get the information from the file name
                if (m.group("series") != null && m.group("series") != "") {

                    // The series number in the file name is 1-based
                    seriesNum = Integer.parseInt(m.group("series")) - 1;

                } else {
                    // Not defined
                    seriesNum = -1;
                }

                // Get the timepoint
                if (m.group("timepoint") != null && m.group("timepoint") != "") {
                    // The timepoint in the file name is 1-based
                    timepoint = Integer.parseInt(m.group("timepoint")) - 1;
                } else {

                    // Not defined
                    timepoint = -1;
                }

                // If both series and timepoint are not defined, we force a new series
                if (seriesNum == -1 && timepoint == -1) {
                    seriesNum = currentNewSeriesIndex;
                }

                // Make sure that the geometry of the files in the series match,
                // otherwise force a new series
                if (! fileGeometryInSeriesMatch(seriesMap, fileAttributes, seriesNum, currAttr)) {
                    currentNewSeriesIndex += 1;
                    seriesNum +=1;
                }

                // Store the file name in current series
                if (seriesMap.containsKey(seriesNum)) {
                    seriesMap.get(seriesNum).add(listOfFiles.get(i));
                } else {
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(listOfFiles.get(i));
                    seriesMap.put(seriesNum, list);
                }

            } else {

                // Could not parse and process the file name
                isValid = false;
                errorMessage = "Could not parse file name for file " + listOfFiles.get(i);
                return isValid;

            }

        }

        // Get all series numbers
        Object[] seriesNumbers = seriesMap.keySet().toArray();
        Arrays.sort(seriesNumbers);

        // Now combine the attributes
        for (int i = 0; i < seriesNumbers.length; i++) {

            // Get current series number
            int s = (int) seriesNumbers[i];

            // Get all files that belong to current series
            ArrayList<String> fileList = seriesMap.get(s);

            // Get the attributes of the first file
            Map<String, HashMap<String, String>> currentAttr =
                    fileAttributes.get(fileList.get(0));

            // Set the series number
            currentAttr = setSeriesNumber(currentAttr, s);

            // Merge attributes
            for (int f = 1; f < fileList.size(); f++) {
                currentAttr = mergeSeriesAttr(currentAttr,
                        setSeriesNumber(fileAttributes.get(fileList.get(f)), s));
            }

            combinedAttr.putAll(currentAttr);

            // And the series number
            combinedSeriesIndices.add(s);

        }

        return true;
    }

    /**
     * Check that the geometry of the file to be added to the series matches the
     * geometry of the files already assigned to it.
     * @param seriesMap
     * @param fileAttributes
     * @param seriesNum Number of the series
     * @param currAttr
     * @param currAttr Attributes of the file to compare.
     * @return true if the geometry fits, false otherwise.
     */
    private Boolean fileGeometryInSeriesMatch(Map<Integer, ArrayList<String>> seriesMap,
            Map<String, Map<String, HashMap<String, String>>> fileAttributes,
            int seriesNum, Map<String, HashMap<String, String>> currAttr) {

        // Get current series values
        HashMap<String, String> currentSeries = currAttr.get("series_0");

        // Compare the geometry from currAttr with all stored geometries
        if (seriesMap.containsKey(seriesNum)) {
            ArrayList<String> processedFiles = seriesMap.get(seriesNum);
            for (String processedFile : processedFiles) {
                // Get the attributes
                Map<String, HashMap<String, String>> processedFileAttr = fileAttributes.get(processedFile);

                // Now compare the geometry
                HashMap<String, String> processedSeries = processedFileAttr.get("series_0");

                return (processedSeries.get("sizeX").equals(currentSeries.get("sizeX")) &&
                        processedSeries.get("sizeY").equals(currentSeries.get("sizeY")) &&
                        processedSeries.get("sizeZ").equals(currentSeries.get("sizeZ")));
            }
        } else {
            // Since it is the first image of this series, it is compatible by definition.
            return true;
        }

        return false;
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
