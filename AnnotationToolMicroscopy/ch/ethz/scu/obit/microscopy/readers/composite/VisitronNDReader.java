package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            "(?<basename>.*?)" +                     // Base name
                    "(_w(?<channel>\\d.*?)" +        // Channel number (optional)
                    "(?<channelName>.*?))?" +        // Channel name (optional)
                    "(_s(?<series>\\d.*?))?" +       // Series number (optional)
                    "(_t(?<timepoint>\\d.*?))?" +    // Time index (optional)
                    "(\\.tif{1,2}|\\.stk)$";         // File extension

    private static Pattern FILENAME_PATTERN = Pattern.compile(FILENAME_REGEX, Pattern.CASE_INSENSITIVE);

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

        // First, find the ND file
        File[] ndFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toUpperCase().endsWith(".ND");
            }
        });

        // There can be only one ND file per (composite) folder
        if (ndFiles.length == 0) {
            // Mark failure
            isValid = false;
            errorMessage = "No ND file found in the folder!";
            return isValid;
        }
        if (ndFiles.length > 1) {
            // Mark failure
            isValid = false;
            errorMessage = "There must be exactly one ND file per folder.";
            return isValid;
        };

        // Get the ND file
        File ndFile = ndFiles[0];

        // Now extract the basename
        String NDFileName = ndFile.getName();
        basename = NDFileName.substring(0, NDFileName.length() - 3);

        /*
         *
         * FIRST PASS
         *
         *     Split the files into the subset that belongs to the ND file
         *     and the subset of additional files (like overviews, ...)
         */
        List<File> filesAssociatedToNDFile = new ArrayList<File>();
        List<File> additionalFiles = new ArrayList<File>();

        // Get a list of all files in the folder
        File[] allFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.toUpperCase().endsWith(".TIF") ||
                        name.toUpperCase().endsWith(".TIFF") ||
                        name.toUpperCase().endsWith(".STK"));
            }
        });
        if (allFiles == null) {

            // Mark failure
            isValid = false;
            errorMessage = "There are no image files in the folder.";
            return isValid;

        }

        // Calculate total dataset size
        for (File file : allFiles) {
            totalDatasetSizeInBytes += file.length();
        }

        /*
         *
         * SECOND PASS
         *
         *     Partition the files into the subset that belongs to the ND file
         *     and the subset of additional files (like overviews, ...)
         */
        for (File file : allFiles) {

            // Get the file name
            String name = file.getName();

            if (name.startsWith(basename)) {
                filesAssociatedToNDFile.add(file);
            } else {
                additionalFiles.add(file);
            }
        }

        /*
         *
         * THIRD PASS
         *
         *     Condense the non-basename part of the file name into a pattern.
         */
        // Create a map of series numbers to list of file names
        Map<String, ArrayList<File>> mutableFileNamePattern = new HashMap<String, ArrayList<File>>();

        // Consider only the part of the file name without the basename
        int startIndex = basename.length();

        for (File file : filesAssociatedToNDFile) {

            // First parse the file name to extract some information
            String currentFileNamePart = "";
            try {
                currentFileNamePart = file.getName().substring(startIndex);
            } catch (Exception e) {
                currentFileNamePart = file.getName();
            }


            // Remove all digits
            String pattern = currentFileNamePart.replaceAll("[^A-Za-z]","");

            ArrayList<File> list;
            if (mutableFileNamePattern.containsKey(pattern)) {
                list = mutableFileNamePattern.get(pattern);
            } else {
                list = new ArrayList<File>();
            }
            list.add(file);
            mutableFileNamePattern.put(pattern, list);
        }

        // There should be only one pattern; if not, we pick the one with most files
        // and move the files from all other patterns into additionalFiles.
        if (mutableFileNamePattern.keySet().size() > 1) {

            int maxNumFilesPerPattern = -1;
            String maxNumFilesPerPatternString = "";

            for (String key : mutableFileNamePattern.keySet()) {
                if (mutableFileNamePattern.get(key).size() > maxNumFilesPerPattern) {
                    maxNumFilesPerPattern = mutableFileNamePattern.get(key).size();
                    maxNumFilesPerPatternString = key;
                }
            }

            // Move all the files that do not belong to the "winning" template
            // to the additionalFiles list
            for (String key : mutableFileNamePattern.keySet()) {
                if (key.equals(maxNumFilesPerPatternString)) {
                    continue;
                }

                // Move the files
                for (File file : mutableFileNamePattern.get(key)) {
                    int index = filesAssociatedToNDFile.indexOf(file);
                    if (index == -1) {
                        throw new Exception("There was a problem assigning files to the ND file!");
                    }
                    filesAssociatedToNDFile.remove(index);
                    additionalFiles.add(file);
                }
            }
        }


        // Reset the highestKnownSeriesNumber counter
        int minAllowedSeriesNum = 0;

        // Process the files associated to the ND file
        if (! buildSeriesFromFileList(filesAssociatedToNDFile, minAllowedSeriesNum)) {
            // The status and the error message have been set by buildSeriesFromFiles()
            return false;
        }

        // Find the higest series number used
        int mx = -1;
        for (Integer s : combinedSeriesIndices) {
            if (s > mx) {
                mx = s;
            }
        }
        minAllowedSeriesNum = mx + 1;

        // The additional files are added as independent series.
        // The basename obtained from the ND file can no longer be used.
        for (File file : additionalFiles) {
            // Create a list with one file
            List<File> currentFile = new ArrayList<File>(1);
            currentFile.add(file);
            if (! buildSeriesFromFileList(currentFile, minAllowedSeriesNum)) {
                return false;
            }
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
     * Set the series number in place.
     * @param attr Current attribute series.
     * @param targetSeriesNum Number of the series to set.
     * @return attr Updated attribute series.
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
     * @throws Exception
     */
    private Boolean buildSeriesFromFileList(List<File> filesAssociatedToNDFile, int minAllowedSeriesNum) throws Exception {

        // Keep track of the channels
        // Create a map of channel names and numbers
        Map<Integer, Map<String, Integer>> seriesChannels = new HashMap<Integer, Map<String, Integer>>();

        // Create a map of timepoint indices
        Map<Integer, ArrayList<Integer>> seriesTimepoints = new HashMap<Integer, ArrayList<Integer>>();

        // Create a map of file attributes
        Map<String, Map<String, HashMap<String, String>>> fileAttributes = new HashMap<String, Map<String, HashMap<String, String>>>();

        // Create a map of series numbers to list of file names
        Map<Integer, ArrayList<String>> seriesFileList = new HashMap<Integer, ArrayList<String>>();

        // Create a map of series numbers to list of file names
        Map<Integer, String> seriesBasename = new HashMap<Integer, String>();

        // Create a map of series dimensions
        Map<Integer, Integer[]> seriesDimensions = new HashMap<Integer, Integer[]>();

        // Consider only the part of the file name without the basename
        int startIndex = 0; // basename.length();

        // Keep track of the last file extension seen
        String lastFileExtensionSeen = "";

        // Process all files
        for (int i = 0; i < filesAssociatedToNDFile.size(); i++) {

            int seriesNum = 0;
            String channelNameFromFileName = "";
            int channelNumberFromFileName = -1;
            int timepoint = 0;

            // First, get the series number from the file name
            String currentFileNamePart = "";
            try {
                currentFileNamePart = filesAssociatedToNDFile.get(i).getName().substring(startIndex);
            } catch (Exception e) {
                currentFileNamePart = filesAssociatedToNDFile.get(i).getName();
            }

            // Get the file extension
            int lastDotIndex = currentFileNamePart.lastIndexOf(".");
            String currentFileExtension = currentFileNamePart.substring(lastDotIndex);

            // Initialize currentBasename as the basename
            String currentBasename = basename;

            // Parse the file name (template)
            Matcher m = FILENAME_PATTERN.matcher(currentFileNamePart);
            if (m.find()) {

                // Get the information from the file name
                if (m.group("basename") != null && !m.group("basename").equals("")) {

                    currentBasename = m.group("basename");

                    // Compare it with the known basename
                    if (!basename.equals("") && !currentBasename.equals(basename)) {
                        // Could not parse and process the file name
                        isValid = false;
                        errorMessage = "The file " + filesAssociatedToNDFile.get(i) +
                                " associated to the ND file does not have the " +
                                "expected basename (" + basename + ")!";
                        return isValid;
                    }
                }

                // Get the information from the file name
                if (m.group("series") != null && !m.group("series").equals("")) {

                    // The series number in the file name is 1-based
                    seriesNum = Integer.parseInt(m.group("series")) - 1;

                } else {
                    // No series number: fall back to 0
                    seriesNum = 0;
                }

                // Retrieve the channel number from the file name
                if (m.group("channel") != null && !m.group("channel").equals("")) {
                    // The channel in the file name is 1-based
                    channelNumberFromFileName = Integer.parseInt(m.group("channel")) - 1;
                } else {
                    // No channel number; fall back to 0
                    channelNumberFromFileName = -1;
                }

                // Retrieve the channel name from the file name
                if (m.group("channelName") != null && !m.group("channelName").equals("")) {
                    // The channel in the file name is 1-based
                    channelNameFromFileName = m.group("channelName");
                } else {
                    // Not channel name; fall back to ""
                    channelNameFromFileName = "";
                }

                // Get the timepoint
                if (m.group("timepoint") != null && !m.group("timepoint").equals("")) {
                    // The timepoint in the file name is 1-based
                    timepoint = Integer.parseInt(m.group("timepoint")) - 1;
                } else {
                    // Not timepoint: fall back to 0
                    timepoint = 0;
                }
            } else {

                // Could not parse and process the file name
                isValid = false;
                errorMessage = "Could not parse file name for file " + filesAssociatedToNDFile.get(i);
                return isValid;

            }

            // Open the file
            BioFormatsWrapper reader = new BioFormatsWrapper(filesAssociatedToNDFile.get(i), false);

            // Parse the file
            reader.parse();

            // Close the file
            reader.close();

            // Get the attributes
            Map<String, HashMap<String, String>> currAttr = reader.getAttributes();

            // Get the X, Y, Z size from the file
            int seriesSizeX = Integer.parseInt(currAttr.get("series_0").get("sizeX"));
            int seriesSizeY = Integer.parseInt(currAttr.get("series_0").get("sizeY"));
            int seriesSizeZ = Integer.parseInt(currAttr.get("series_0").get("sizeZ"));
            int seriesSizeT = Integer.parseInt(currAttr.get("series_0").get("sizeT"));

            // The C dimension in the individual file MUST be 1.
            int seriesSizeC = Integer.parseInt(currAttr.get("series_0").get("sizeC"));

            if (seriesSizeC != 1) {
                isValid = false;
                errorMessage = "Only one channel expected in a file associated to an ND file!";
                return isValid;
            }

            // Dimensions
            Integer[] currentSeriesDimensions;
            if (seriesDimensions.containsKey(seriesNum)) {
                currentSeriesDimensions = seriesDimensions.get(seriesNum);
                if (currentSeriesDimensions[0] != seriesSizeX ||
                        currentSeriesDimensions[1] != seriesSizeY ||
                        currentSeriesDimensions[2] != seriesSizeZ ||
                        currentSeriesDimensions[3] != seriesSizeT ||
                        (!lastFileExtensionSeen.equals("") && !currentFileExtension.equals(lastFileExtensionSeen)) ) {

                    // Start a new series
                    seriesNum++;

                }
            } else {
                currentSeriesDimensions = new Integer[] {seriesSizeX, seriesSizeY, seriesSizeZ, seriesSizeT};
                seriesDimensions.put(seriesNum, currentSeriesDimensions);
            }

            // Update the lastFileExtensionSeen property
            lastFileExtensionSeen = currentFileExtension;

            // Are we allowed to use this series number?
            if (i == 0) {
                // First file in the series
                if (seriesNum < minAllowedSeriesNum) {
                    seriesNum = minAllowedSeriesNum;
                }
            }

            // Get channel names from series map
            Map<String, Integer> currentSeriesChannels;
            if (seriesChannels.containsKey(seriesNum)) {
                currentSeriesChannels = seriesChannels.get(seriesNum);
            } else {
                currentSeriesChannels = new HashMap<String, Integer>();
            }

            // Retrieve the channel name from the file metadata (there is always
            // only one channel in a single file)
            String channelNameFromMetadata = currAttr.get("series_0").get("channelName0");

            // Get consistent channel name and number of the series
            int channelNumber = -1;
            String channelName = "";

            // Do we have a channel number?
            if (channelNumberFromFileName != -1) {
                channelNumber = channelNumberFromFileName;
            }

            // Do we have a channel name?
            if (!channelNameFromMetadata.equals("")) {
                channelName = channelNameFromMetadata;
            } else if (!channelNameFromFileName.equals("")) {
                channelName = channelNameFromFileName;
            } else {
                channelName = "";
            }

            if (channelName.equals("")) {
                if (channelNumber == -1) {
                    channelNumber = 0;
                    channelName = "UNKNOWN";
                }
            }

            // Does the channel name already exist in the map?
            if (! currentSeriesChannels.containsKey(channelName)) {
                if (channelNumber == -1) {
                    // Find the max channel number in the map and add 1
                    if (currentSeriesChannels.size() == 0) {
                        channelNumber = 0;
                    } else {
                        int maxChannelNumber = -1;
                        for (String key: currentSeriesChannels.keySet()) {
                            if (currentSeriesChannels.get(key) > maxChannelNumber) {
                                maxChannelNumber = currentSeriesChannels.get(key) + 1;
                            }
                            channelNumber = maxChannelNumber;
                        }
                    }
                }
                currentSeriesChannels.put(channelName, channelNumber);
            }
            seriesChannels.put(seriesNum, currentSeriesChannels);

            // Does the timepoint already exist in the map?
            if (seriesTimepoints.containsKey(seriesNum)) {
                ArrayList<Integer> timepoints = seriesTimepoints.get(seriesNum);
                if (seriesSizeT == 1) {
                    if (! timepoints.contains(timepoint)) {
                        timepoints.add(timepoint);
                    }
                } else {
                    for (int t = 0; t <= timepoint; t++) {
                        if (! timepoints.contains(t)) {
                            timepoints.add(t);
                        }
                    }
                }
            } else {
                ArrayList<Integer> timepoints = new ArrayList<Integer>();
                if (seriesSizeT == 1) {
                    timepoints.add(timepoint);
                } else {
                    for (int t = 0; t <= timepoint; t++) {
                        timepoints.add(t);
                    }
                }
                seriesTimepoints.put(seriesNum, timepoints);
            }

            // Store the series basename
            seriesBasename.put(seriesNum, currentBasename);

            // Store the file name in current series
            if (seriesFileList.containsKey(seriesNum)) {
                seriesFileList.get(seriesNum).add(filesAssociatedToNDFile.get(i).getAbsolutePath());
            } else {
                ArrayList<String> list = new ArrayList<String>();
                list.add(filesAssociatedToNDFile.get(i).getAbsolutePath());
                seriesFileList.put(seriesNum, list);
            }

            // Add the filename
            currAttr.get("series_0").put("filenames", filesAssociatedToNDFile.get(i).getAbsolutePath());

            // Store the updated attributes
            fileAttributes.put(filesAssociatedToNDFile.get(i).getAbsolutePath(), currAttr);
        }

        // Get all series numbers
        Object[] seriesNumbers = seriesFileList.keySet().toArray();
        Arrays.sort(seriesNumbers);

        // Now combine the attributes
        for (int i = 0; i < seriesNumbers.length; i++) {

            // Get current series number
            int s = (int) seriesNumbers[i];

            // Get all files that belong to current series
            ArrayList<String> fileList = seriesFileList.get(s);

            // Get the attributes of the first file
            Map<String, HashMap<String, String>> currentAttr =
                    fileAttributes.get(fileList.get(0));

            // Set the series number
            currentAttr = setSeriesNumber(currentAttr, s);

            // Update the series metadata
            currentAttr.put("series_" + s,
                    updateSeriesMetadata(
                            currentAttr.get("series_" + s),
                            seriesChannels.get(s),
                            seriesTimepoints.get(s),
                            seriesFileList.get(s),
                            seriesBasename.get(s)
                            )
                    );

            combinedAttr.putAll(currentAttr);

            // And the series number
            combinedSeriesIndices.add(s);

        }

        return true;
    }

    private HashMap<String, String> updateSeriesMetadata(HashMap<String, String> seriesAttr,
            Map<String, Integer> channels, List<Integer> timepoints, List<String> fileList, String basename) {

        // New attributes
        HashMap<String, String> newSeriesAttr = new HashMap<String, String>();

        // Copy all properties with the exception of channels, time points and file-related entries
        for (String key : seriesAttr.keySet()) {
            if (key.startsWith("exWavelength") ||
                    key.startsWith("channelName") ||
                    key.startsWith("channelColor") ||
                    key.startsWith("exWaveLength") ||
                    key.startsWith("emWaveLength") ||
                    key.equals("filenames") ||
                    key.equals("sizeC") ||
                    key.equals("sizeT")) {

                // Skip

            } else {
                newSeriesAttr.put(key, seriesAttr.get(key));
            }
        }

        // Now add the channel names and colors and the channel number
        for (String channelName : channels.keySet()) {
            int channelNumber = channels.get(channelName);
            double[] color = BioFormatsWrapper.getDefaultChannelColor(channelNumber);

            newSeriesAttr.put("channelName" + channelNumber, channelName);
            newSeriesAttr.put("channelColor" + channelNumber, implode(color));
            newSeriesAttr.put("exWaveLength" + channelNumber, "NaN");
            newSeriesAttr.put("emWaveLength" + channelNumber, "NaN");
        }
        newSeriesAttr.put("sizeC", "" + channels.size());

        // Add the number of timepoints
        newSeriesAttr.put("sizeT", "" + timepoints.size());

        // Add the file list
        newSeriesAttr.put("filenames", implode(fileList));

        // Add the basename
        newSeriesAttr.put("basename", basename);

        return newSeriesAttr;
    }
}
