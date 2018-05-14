package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
                    "(_w(?<channel>\\d.*?))?" +              // Channel number (optional)
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
    private List<String> foundFiles;
    private List<String> referencedFiles;

    BioFormatsWrapper bioformatsWrapperForNDFile =  null;
    Map<String, HashMap<String, String>> combinedAttr;
    List<Integer> combinedSeriesIndices;

    // Internal class used to collect files that do not belong
    // to the dataset referenced by the accompanying ND file.
    private class Series {

        // Series number
        private int seriesNumber;

        // List of file names that belong to this series
        private List<String> fileNames = new ArrayList<String>();

        // List of channel numbers
        private List<Integer> channels = new ArrayList<Integer>();

        // List of timepoints
        private List<Integer> timepoints = new ArrayList<Integer>();

        // Constructor
        private Series(int firstIndexNumber) {
            seriesNumber = firstIndexNumber;
        }

        // Add a new file by channel
        private void addFile(String filename, int channel, int timepoint) {
            fileNames.add(filename);
            channels.add(channel);
            timepoints.add(timepoint);
        }

    }

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
        foundFiles = new ArrayList<String>();
        referencedFiles = new ArrayList<String>();

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

                if (! parseNDFile(file)) {

                    // If parsing failed, we return immediately
                    isValid = false;
                    errorMessage = "Cold not process ND file.";
                    return isValid;
                }

                NDFileAlreadyFound = true;

                // Set the "basename"
                String NDFileName = file.getName();
                basename = NDFileName.substring(0, NDFileName.length() - 3);

                // If parsing of the ND file succeeded, we still continue
                // iterating over the files to update the total dataset size!

            } else {

                // Any other file type is not supported.
                isValid = false;
                errorMessage = "The file " + file.getAbsolutePath() + " is not recognized!";
                return isValid;

            }

        }

        // Check that the number of reference files and the number of found files match
        if (referencedFiles.size() == 0) {
            // Mark failure
            isValid = false;
            errorMessage = "The ND file does not reference any image file!";
            return isValid;
        }

        if (foundFiles.size() == 0) {
            // Mark failure
            isValid = false;
            errorMessage = "Could not find any image files in the folder!";
            return isValid;
        }

        if (foundFiles.size() != referencedFiles.size()) {

            // Extract the found image files that are not referenced in the ND file
            List<String> diffFiles = new ArrayList<String>();
            try {
                diffFiles = TIFFFilesFoundButNotReferenced();
            } catch (Exception e) {
                // Mark failure
                isValid = false;
                errorMessage = e.getMessage();
                return isValid;
            }

            // Process the image files
            int firstSeriesIndex = bioformatsWrapperForNDFile.getNumberOfSeries();

            // Break down the files into series
            List<Series> seriesToProcess = arrangeFilesIntoSeries(diffFiles, firstSeriesIndex);

            // Now process the Series objects
            for (int i = 0; i < seriesToProcess.size(); i++) {

                // Current series
                Series currentSeries = seriesToProcess.get(i);

                // Now process all files in the series
                Map<String, HashMap<String, String>> allSeriesAttrs =
                        new HashMap<String, HashMap<String, String>>();

                for (int j = 0; j < currentSeries.fileNames.size(); j++) {

                    // Parse the file end extract the metadata
                    BioFormatsWrapper localBioformatsWrapper = new BioFormatsWrapper(
                            new File(currentSeries.fileNames.get(j)), false);
                    if (! localBioformatsWrapper.parse(currentSeries.seriesNumber)) {
                        // Mark failure
                        isValid = false;
                        errorMessage = "Could not process file " + diffFiles.get(i);
                        return isValid;
                    }
                    Map<String, HashMap<String, String>> localAttrs = localBioformatsWrapper.getAttributes();

                    // Merge
                    if (allSeriesAttrs.isEmpty()) {
                        allSeriesAttrs = localAttrs;
                    } else {
                        allSeriesAttrs = mergeSeriesAttr(allSeriesAttrs, localAttrs, currentSeries.channels.get(j),
                                currentSeries.timepoints.get(j));
                    }
                }

                // Now store the updated series back
                combinedAttr.putAll(allSeriesAttrs);

                // And the series number
                combinedSeriesIndices.add(currentSeries.seriesNumber);

            }

        }

        // Mark success
        isValid = true;
        errorMessage = "";
        return isValid;

    }

    /**
     * Parse the ND file and extracts all metadata information.
     * @param file Full path to the ND file (java.io.File).
     * @return true if the parsing was successful, false otherwise.
     */
    private Boolean parseNDFile(File file) {

        // We use the BioFormarsWrapper to scan the ND file
        bioformatsWrapperForNDFile = new BioFormatsWrapper(file, true);
        if (! bioformatsWrapperForNDFile.parse()) {
            errorMessage = bioformatsWrapperForNDFile.getErrorMessage();
            return false;
        }

        // Store the list of files references in the ND file
        referencedFiles = bioformatsWrapperForNDFile.getReferencedFiles();

        // Store the attributes and the file series indices
        combinedAttr = bioformatsWrapperForNDFile.getAttributes();
        combinedSeriesIndices = bioformatsWrapperForNDFile.getSeriesIndices();

        // Close the reader
        bioformatsWrapperForNDFile.close();

        // Return success
        return true;
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
     * Return the list of image files that were found in the folder but not referenced in the ND file
     * @return list of image files that were found in the folder but not referenced in the ND file
     * @throws Exception
     */
    private List<String> TIFFFilesFoundButNotReferenced() throws Exception {

        // If all found files are referenced, we do not need to do anything else
        if (foundFiles.size() == referencedFiles.size()) {
            return new ArrayList<String>();
        }

        // If there are more referenced files that we found in the folder, the dataset is incomplete
        if (foundFiles.size() < referencedFiles.size()) {
            throw new Exception("The ND files references more files than there are in the folder!");
        }

        // Unfortunately, the bio-formats library ImageProcessorReader.getAdvancedUsedFiles()
        // does not necessarily return the file name with the correct case, so we need to
        // equalize them before passing them to the HashSet constructor. Since we need to be
        // still Java-7 compatible, we do not use lambda functions.
        List<String> normFoundTIFFFiles = new ArrayList<String>();
        for (int i = 0; i < foundFiles.size(); i++) {
            normFoundTIFFFiles.add(foundFiles.get(i).toLowerCase());
        }
        List<String> normReferencedTIFFFiles = new ArrayList<String>();
        for (int i = 0; i < referencedFiles.size(); i++) {
            normReferencedTIFFFiles.add(referencedFiles.get(i).toLowerCase());
        }

        // Calculate the difference of the two sets of file names
        Set<String> setOfFoundTIFFFiles = new LinkedHashSet<String>(normFoundTIFFFiles);
        Set<String> setOfReferencedTIFFFiles = new LinkedHashSet<String>(normReferencedTIFFFiles);
        setOfFoundTIFFFiles.removeAll(setOfReferencedTIFFFiles);

        // Return the file names in the difference set as list
        List<String> diffList = new ArrayList<String>();
        diffList.addAll(setOfFoundTIFFFiles);

        return diffList;

    }

    /**
     * Merge two series attributes.
     * @param existingLocatAttrs First series attribute to be extended.
     * @param localAttrs Second series attributes to be integrated.
     * @param channel Channel number of the (second) series to be merged.
     * @param timepoint Timepoint of the (second) series to be merged.
     * @return Merged series attributes.
     */
    private Map<String, HashMap<String, String>> mergeSeriesAttr(Map<String, HashMap<String, String>> attrTarget,
            Map<String, HashMap<String, String>> attrSource, int channel, int timepoint) {

        assert(attrTarget.keySet().size() == 1);
        assert(attrSource.keySet().size() == 1);

        Iterator<Entry<String, HashMap<String, String>>> targetIt = attrTarget.entrySet().iterator();
        Iterator<Entry<String, HashMap<String, String>>> sourceIt = attrSource.entrySet().iterator();
        while (targetIt.hasNext()) {

            // Get the series from the target
            Entry<String, HashMap<String, String>> targetPair = targetIt.next();
            HashMap<String, String> targetSeries = targetPair.getValue();

            while (sourceIt.hasNext()) {

                // Get the series from the source
                Entry<String, HashMap<String, String>> sourcePair = sourceIt.next();
                HashMap<String, String> sourceSeries = sourcePair.getValue();

                Set<String> keySetSource = sourceSeries.keySet();

                // First, get the number of channels and timepoints in the target
                int targetNumChannels = Integer.parseInt(targetSeries.get("sizeC"));
                int targetNumTimepoints = Integer.parseInt(targetSeries.get("sizeT"));

                // Do we have a new channel to add?
                if (channel + 1 > targetNumChannels) {

                    int finalNumC = channel;

                    // This is a new channel
                    for (String key : keySetSource) {

                        Matcher m = ATTR_PATTERN.matcher(key);
                        if (m.find()) {
                            // Add a new key to the target attributes with the value of
                            // the source attributes
                            if (! m.group("channel").equals("")) {
                                String updateKey = m.group("key") + finalNumC;
                                if (m.group("key").equals("channelColor")) {
                                    String sourceValue = sourceSeries.get(key);
                                    if (sourceValue.equals(targetSeries.get(key))) {
                                        double [] defC = BioFormatsWrapper.getDefaultChannelColor(channel);
                                        String c  = "";
                                        for (int k = 0; k < defC.length - 1; k++) {
                                            c += defC[k] + ", ";
                                        }
                                        c += defC[defC.length - 1];
                                        targetSeries.put(updateKey, c);
                                    } else {
                                        targetSeries.put(updateKey, sourceSeries.get(key));
                                    }
                                } else {
                                    targetSeries.put(updateKey, sourceSeries.get(key));
                                }
                            }
                        }
                    }

                    // Do we have a new timepoint to add?
                    targetSeries.put("sizeC", "" + (targetNumChannels + 1));
                }

                // Do we have a new timepoint to add?
                if (timepoint + 1 > targetNumTimepoints) {
                    targetSeries.put("sizeT", "" + (targetNumTimepoints + 1));
                }

            }

        }

        return attrTarget;
    }

    /**
     * Organize the files not referenced by the ND file into series.
     * @param diffFiles List of files not referenced in the ND file.
     * @param firstSeriesIndex Index of the fist series.
     * @return a Map of series number to file lists.
     * @throws Exception If the file name cannot be parsed.
     */
    private List<Series> arrangeFilesIntoSeries(List<String> diffFiles, int firstSeriesIndex) throws Exception {

        // Create a map of Series objects
        Map<String, Series> mapSeries = new HashMap<String, Series>();

        // Process all files
        for (int i = 0; i < diffFiles.size(); i++) {

            // Default series number for current file
            int seriesNum = firstSeriesIndex;

            // Default basename for current file
            String basename = "default";

            // Default channel number for current file
            int channel = 0;

            // Default timepoint for current file
            int timepoint = 0;

            // First parse the file name to extract some information
            Matcher m = FILENAME_PATTERN.matcher(diffFiles.get(i));
            if (m.find()) {

                // Get the base name
                if (m.group("basename") != null) {
                    basename = m.group("basename");
                }

                // Get the series number
                if (m.group("series") != null) {
                    // The series number in the file name is 1-based
                    seriesNum = Integer.parseInt(m.group("series")) - 1;
                } else {
                    seriesNum = firstSeriesIndex;
                }

                // Get the channel  number
                if (m.group("channel") != null) {
                    // The channel number in the file name is 1-based
                    channel = Integer.parseInt(m.group("channel")) - 1;
                } else {
                    channel = 0;
                }

                // Get the timepoint
                if (m.group("timepoint") != null) {
                    // The timepoint in the file name is 1-based
                    timepoint = Integer.parseInt(m.group("timepoint")) - 1;
                } else {
                    timepoint = 0;
                }
            } else {
                throw new Exception("Could not parse file name for file " + diffFiles.get(i));
            }

            // Get the Series that matches this file
            String newKey = basename + "_" + seriesNum;
            Series existingSeries;
            if (mapSeries.containsKey(newKey)) {

                // Get the existing series
                existingSeries = mapSeries.get(newKey);

            } else {

                // Create new series
                existingSeries = new Series(seriesNum);

                // Add it to the map
                mapSeries.put(newKey, existingSeries);
            }

            // Add the new file
            existingSeries.addFile(diffFiles.get(i), channel, timepoint);
        }

        // Return a simple list of Series
        List<Series> listOfSeries = new ArrayList<Series>();
        for (Entry<String, Series> entry : mapSeries.entrySet()) {
            listOfSeries.add(entry.getValue());
        }
        return listOfSeries;
    }

}
