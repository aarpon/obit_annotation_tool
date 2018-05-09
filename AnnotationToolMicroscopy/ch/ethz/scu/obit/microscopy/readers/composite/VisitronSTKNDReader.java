package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
//import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import java.util.regex.Pattern;
import ch.ethz.scu.obit.microscopy.readers.BioFormatsWrapper;


/**
 * A composite reader for Visitron STK + ND files.
 *
 * @author Aaron Ponti
 *
 */
public class VisitronSTKNDReader extends AbstractCompositeMicroscopyReader {

    /* Protected instance variables */
    private File folder;
    private String basename = "";
    private boolean isValid = false;
    private List<String> foundSTKFiles;
    private List<String> referencedSTKFiles;

    BioFormatsWrapper bioformatsWrapperForNDFile =  null;
    Map<String, HashMap<String, String>> combinedAttr;
    List<Integer> combinedSeriesIndices;

    // Constructor
    /**
     * @param folder Folder to be scanned.
     */
    public VisitronSTKNDReader(File folder) {

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
        boolean STKFilesFound = false;

        // Get a list of all files
        File[] allFiles = folder.listFiles();

        for (File file : allFiles) {

            // If we have found the ND and STK files we return
            if (NDFileFound == true && STKFilesFound == true) {
                return true;
            }

            // Get the file name
            String name = file.getName();

            // Do we have an ND or an STK file?
            if (name.toUpperCase().endsWith(".ND")) {
                NDFileFound = true;
                continue;
            }

            if (name.toUpperCase().endsWith(".STK")) {
                STKFilesFound = true;
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
        foundSTKFiles = new ArrayList<String>();
        referencedSTKFiles = new ArrayList<String>();

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

            // STK files are expected
            if (name.toUpperCase().endsWith(".STK")) {

                // Add the file size to the total
                totalDatasetSizeInBytes += file.length();

                // Add the file to the list of found TIFF files
                foundSTKFiles.add(file.getAbsolutePath());

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
        if (referencedSTKFiles.size() == 0) {
            // Mark failure
            isValid = false;
            errorMessage = "The ND file does not reference any TIFF file!";
            return isValid;
        }

        if (foundSTKFiles.size() == 0) {
            // Mark failure
            isValid = false;
            errorMessage = "Could not find any TIFF files in the folder!";
            return isValid;
        }

        if (foundSTKFiles.size() != referencedSTKFiles.size()) {

            // Extract the found TIFF files that are not referenced in the ND file
            List<String> diffFiles = new ArrayList<String>();
            try {
                diffFiles = STKFilesFoundButNotReferenced();
            } catch (Exception e) {
                // Mark failure
                isValid = false;
                errorMessage = e.getMessage();
                return isValid;
            }

            // Process the TIFF files
            int firstSeriesIndex = bioformatsWrapperForNDFile.getNumberOfSeries();
            for (int i = 0; i < diffFiles.size(); i++) {

                BioFormatsWrapper localBioformatsWrapper = new BioFormatsWrapper(
                        new File(diffFiles.get(i)), false);
                if (! localBioformatsWrapper.parse(firstSeriesIndex)) {
                    // Mark failure
                    isValid = false;
                    errorMessage = "Could not process file " + diffFiles.get(i);
                    return isValid;
                }
                Map<String, HashMap<String, String>> localAttrs = localBioformatsWrapper.getAttributes();
                List<Integer> localSeriesIndices = localBioformatsWrapper.getSeriesIndices();
                localBioformatsWrapper.close();

                // Append the new attributes
                combinedAttr.putAll(localAttrs);

                // Append the series indices
                combinedSeriesIndices.addAll(localSeriesIndices);

                // Update the series count
                firstSeriesIndex = firstSeriesIndex + localSeriesIndices.size();
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
        referencedSTKFiles = bioformatsWrapperForNDFile.getReferencedFiles();

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
        return "Visitron ND + STK (composite) file format.";
    }

    /**
     * Return a simplified class name to use in XML.
     * @return simplified class name.
     */
    @Override
    public String getType() {
        return "Visitron ND + STK";
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
     * Return the list of TIFF files that were found in the folder but not referenced in the ND file
     * @return list of TIFF files that were found in the folder but not referenced in the ND file
     * @throws Exception
     */
    private List<String> STKFilesFoundButNotReferenced() throws Exception {

        // If all found files are referenced, we do not need to do anything else
        if (foundSTKFiles.size() == referencedSTKFiles.size()) {
            return new ArrayList<String>();
        }

        // If there are more referenced files that we found in the folder, the dataset is incomplete
        if (foundSTKFiles.size() < referencedSTKFiles.size()) {
            throw new Exception("The ND files references more files than there are in the folder!");
        }

        // Unfortunately, the bio-formats library ImageProcessorReader.getAdvancedUsedFiles()
        // does not necessarily return the file name with the correct case, so we need to
        // equalize them before passing them to the HashSet constructor. Since we need to be
        // still Java-7 compatible, we do not use lambda functions.
        List<String> normfoundSTKFiles = new ArrayList<String>();
        for (int i = 0; i < foundSTKFiles.size(); i++) {
            normfoundSTKFiles.add(foundSTKFiles.get(i).toLowerCase());
        }
        List<String> normreferencedSTKFiles = new ArrayList<String>();
        for (int i = 0; i < referencedSTKFiles.size(); i++) {
            normreferencedSTKFiles.add(referencedSTKFiles.get(i).toLowerCase());
        }

        // Calculate the difference of the two sets of file names
        Set<String> setOffoundSTKFiles = new LinkedHashSet<String>(normfoundSTKFiles);
        Set<String> setOfreferencedSTKFiles = new LinkedHashSet<String>(normreferencedSTKFiles);
        setOffoundSTKFiles.removeAll(setOfreferencedSTKFiles);

        // Return the file names in the difference set as list
        List<String> diffList = new ArrayList<String>();
        diffList.addAll(setOffoundSTKFiles);

        return diffList;

    }
}
