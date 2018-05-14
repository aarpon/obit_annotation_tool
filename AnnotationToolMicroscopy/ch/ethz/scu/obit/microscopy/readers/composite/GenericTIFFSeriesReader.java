package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.scu.obit.microscopy.readers.BioFormatsWrapper;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

/**
 * A composite reader for Generic TIFF series.
 *
 * @author Aaron Ponti
 *
 */
public class GenericTIFFSeriesReader extends AbstractCompositeMicroscopyReader {

    /* Protected instance variables */
    private File folder;
    private final static String REGEX =
            "^(.*?)" +                    // Series basename: group 1
                    "((_Series|_s)(\\d.*?))?" +   // Series number (optional): group 4
                    "(_t(\\d.*?))?" +             // Time index (optional): group 6
                    "_z(\\d.*?)" +                // Plane number: group 7
                    "_ch(\\d.*?)" +               // Channel number: group 8
                    "\\.tif{1,2}$";               // File extension

    private static Pattern p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private String basename = "";

    private boolean isValid = false;

    private ImageProcessorReader reader = null;

    // Constructor
    /**
     * @param folder Folder to be scanned.
     */
    public GenericTIFFSeriesReader(File folder) {

        this.folder = folder;
    }

    /**
     * Return true if the reader can parse the passed folder
     * @param folder Folder to be parsed.
     * @return true if the folder can be parsed, false otherwise.
     */
    static public boolean canRead(File folder) {

        boolean expectedFiles = false;

        // Get a list of all files
        File[] allFiles = folder.listFiles();

        for (File file : allFiles) {

            // No subfolders are allowed!
            if (file.isDirectory()) {
                return false;
            }

            // Get the file name
            String name = file.getName();

            // Check files
            if (!expectedFiles) {

                // The regex test is run only once.
                if (file.isFile()) {

                    Matcher m = p.matcher(name);
                    if (m.find()) {
                        expectedFiles = true;
                        continue;

                    }
                }
            }
        }

        return expectedFiles;

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

    @Override
    public boolean parse() throws Exception {

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

            // Do we have the 'MetaData' folder?
            if (file.isDirectory()) {

                // We can only have one folder called 'MetaData'
                isValid = false;
                errorMessage = "No subfolders allowed!";
                return isValid;

            }

            // Current series metadata
            HashMap<String, String> metadata;

            // Extract the information
            Matcher m = p.matcher(name);
            if (m.find()) {

                // This is an image file. We add its size to the total size of
                // the composite dataset.
                totalDatasetSizeInBytes += file.length();

                // Get base name
                String currentBase = m.group(1);
                if (basename.equals("")) {
                    basename = currentBase;
                } else if (currentBase.equals(basename)) {
                    // Nothing to do
                } else {
                    // More than one base name. Multiple Generic TIFF series.
                    isValid = false;
                    errorMessage = "Multiple series per folder not supported.";
                    return isValid;
                }

                //
                // Extract index information from the file name structure
                //

                // The series index is not always defined
                int seriesNum = 0;
                if (m.group(2) != null) {
                    seriesNum = Integer.parseInt(m.group(4));
                }

                // The time number is not always defined
                int TimeNum = 0;
                if (m.group(5) != null) {
                    TimeNum = Integer.parseInt(m.group(6));
                }

                // Plane number (z)
                int planeNum = Integer.parseInt(m.group(7));

                // Channel number
                int channelNum = Integer.parseInt(m.group(8));

                // Build the key
                String key = "series_" + seriesNum;

                // Store the series index if not yet present
                if (attr.containsKey(key)) {

                    metadata = attr.get(key);

                } else {

                    // Create a new SeriesMetadata object
                    metadata = new HashMap<String, String>();

                    // And add it to the arribute map
                    attr.put(key, metadata);

                    // Read the file
                    if (reader == null) {

                        // Initialize the reader
                        reader = new ImageProcessorReader(
                                new ChannelSeparator(
                                        LociPrefs.makeImageReader()));
                    }

                    // Try to open the image file
                    try {
                        reader.setId(file.getCanonicalPath());
                    } catch (FormatException e) {
                        reader = null;
                    } catch (IOException e) {
                        reader = null;
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

                // Store default color for channel
                metadata.put("channelColor" + channelNum,
                        implode(BioFormatsWrapper.getDefaultChannelColor(channelNum)));


                // Update the number of planes
                int numPlanes = getMetadataValueOrZero(metadata, "sizeZ");
                if ((planeNum + 1) > numPlanes) {
                    metadata.put("sizeZ", Integer.toString(planeNum + 1));
                }

                // Update the number of channels
                int numChannels = getMetadataValueOrZero(metadata, "sizeC");
                if ((channelNum + 1) > numChannels) {
                    metadata.put("sizeC", Integer.toString(channelNum + 1));
                }

                // Update the number of timepoints
                int numTimepoints = getMetadataValueOrZero(metadata, "sizeT");
                if ((TimeNum + 1) > numTimepoints) {
                    metadata.put("sizeT", Integer.toString(TimeNum + 1));
                }

            } else {

                // Found an unexpected file
                isValid = false;
                errorMessage = "Unexpected file name found.";
                return isValid;

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
        return "Generic TIFF series (composite) file format.";
    }

    /**
     * Return a simplified class name to use in XML.
     * @return simplified class name.
     */
    @Override
    public String getType() {
        return "Generic TIFF Series";
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
     * Return the list of series indices extracted from the file names
     * @return List of series indices
     */
    @Override
    public List<Integer> getSeriesIndices() {
        List<Integer> indices = new ArrayList<Integer>();
        for (String key : attr.keySet()) {
            // Get the index from the series_{n} key
            indices.add(Integer.parseInt(key.substring(7)));
        }
        Collections.sort(indices);
        return indices;
    }

    /**
     * Return current value for given metadata entry, or zero if not in the map.
     * @param metadata Map of string - string key:value pairs.
     * @param key Name of the metadata value to query.
     * @return the value for the requested metadata value, or zero if
     * it is not in the map.
     */
    private int getMetadataValueOrZero(Map<String, String> metadata, String key) {
        int value = 0;
        if (metadata.containsKey(key)) {
            value = Integer.parseInt(metadata.get(key));
        }
        return value;
    }

    /**
     * Return the data type
     * @return string datatype, one of "uint8", "uint16", "float", "unsupported".
     */
    public String getDataType() {

        // Get and store the dataset type
        String datatype;
        switch (loci.formats.FormatTools.getBytesPerPixel(reader.getPixelType())) {
        case 1:
            datatype = "uint8";
            break;
        case 2:
            datatype = "uint16";
            break;
        case 4:
            datatype = "float";
            break;
        default:
            datatype = "unsupported";
            break;
        }

        return datatype;
    }

    /**
     * Returns the last error message.
     * @return String containing the last error message.
     */
    public String getLastError() {
        return errorMessage;
    }

    /**
     * Implement an 'implode' function as in PHP.
     * @param values Array of doubles
     * @return Comma-separated list of values as string.
     */
    private String implode(double [] values) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i != values.length - 1) {
                sb.append(", ");
            }
        }
        String joined = sb.toString();
        return joined;
    }
}
