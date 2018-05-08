package ch.ethz.scu.obit.microscopy.readers;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FileInfo;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import ome.units.quantity.Length;
import ome.xml.model.primitives.Color;
import ome.xml.model.primitives.Timestamp;

public class BioFormatsWrapper {

    /* Private instance variables */
    private File filename;
    private ImageProcessorReader reader;
    private ServiceFactory factory;
    private OMEXMLService service;
    private IMetadata omexmlMeta;
    private String errorMessage = "";
    private List<String> referencedFiles;
    private int numberOfSeries = 0;
    private final static double[][] defaultChannelColors = {
            {255.0,   0.0,   0.0, 255.0},
            {  0.0, 255.0,   0.0, 255.0},
            {  0.0,   0.0, 255.0, 255.0},
            {255.0, 255.0,   0.0, 255.0},
            {255.0,   0.0, 255.0, 255.0},
            {  0.0, 255.0, 255.0, 255.0},
            {255.0, 255.0, 255.0, 255.0},
    };

    private boolean isFileScanned = false;

    /**
     * Metadata attributes
     */
    private final Map<String, HashMap<String, String>> attr =
            new HashMap<String, HashMap<String, String>>();

    /**
     * Constructor
     * @param filename Full path to the file to process.
     */
    public BioFormatsWrapper(File filename) {

        // Store the filename
        this.filename = filename;

        // Initialize the list of referenced files
        this.referencedFiles = new ArrayList<String>();

        // Initialize
        init(false);

    }

    /**
     * Alternate constructor
     * @param filename Full path to the file to process.
     */
    public BioFormatsWrapper(File filename, Boolean groupFiles) {

        // Store the filename
        this.filename = filename;

        // Initialize the list of referenced files
        this.referencedFiles = new ArrayList<String>();

        // Initialize
        if (! init(groupFiles)) {
            throw new RuntimeException("Could not initialize the BioFormats Library for file " + filename);
        }

    }

    /**
     * Initialize the reader and sets up the OMEXML metadata store
     * @param groupFiles Toggles the file grouping behavior of the reader.
     * @return bool True if the initialization was successful, false otherwise.
     */
    private boolean init(Boolean groupFiles) {

        // Create the reader
        ImageReader imageReader = LociPrefs.makeImageReader();
        imageReader.setGroupFiles(groupFiles);
        reader = new ImageProcessorReader(new ChannelSeparator(imageReader));

        // Set OME-XML metadata
        factory = null;
        service = null;
        omexmlMeta = null;

        try {
            factory = new ServiceFactory();
            service = factory.getInstance(OMEXMLService.class);
        } catch (DependencyException e) {
            this.errorMessage =
                    "Could not initialize bio-formats library. " +
                            "Error was: " + e.getMessage();
            return false;
        }
        try {
            omexmlMeta = service.createOMEXMLMetadata();
        } catch (ServiceException e) {
            this.errorMessage =
                    "Could not initialize bio-formats library. " +
                            "Error was: " + e.getMessage();
            return false;
        }
        reader.setMetadataStore(omexmlMeta);

        // Try to open the image file
        try {
            reader.setId(filename.getCanonicalPath());
        } catch (FormatException e) {
            this.errorMessage =
                    "Could not open file. Error was: " + e.getMessage();
            return false;
        } catch (IOException e) {
            this.errorMessage =
                    "Could not open file. Error was: " + e.getMessage();
            return false;
        }

        this.errorMessage = "";
        return true;
    }

    /**
     * Close file.
     * The client is responsible to call close to release the file handle!
     * @return true if the file could be closed, false otherwise.
     */
    public boolean close() {

        // If the file is closed already, we return true
        if (reader == null) {
            return true;
        }

        // Try closing it
        try {
            reader.close();
        } catch (IOException e) {

            // Return failure
            return false;
        }

        // Return success
        return true;
    }

    /**
     * Implement finalize() to make sure that all file handles are released.
     */
    @Override
    public void finalize() throws Throwable {

        // Call close
        close();

        // Call the parent finalize()
        super.finalize();
    }

    /**
     * Return the version of the bio-formats library used.
     * @return String with the bio-formats library version.
     */
    public String bioformatsVersion() {
        return loci.formats.FormatTools.VERSION;
    }

    /**
     * Return the error message
     * @return The error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Return true if the file was scanned already, false otherwise.
     * @return true if the file was scanned already, false otherwise.
     */
    public boolean isScanned() {
        return isFileScanned;
    }

    /**
     * Scan the file for metadata information and stores it as a
     * String-String map of key-value attributes.
     * @return true if parsing was successful, false otherwise.
     */
    public boolean parse() {
        return parse(0);
    }

    /**
     * Scan the file for metadata information and stores it as a
     * String-String map of key-value attributes.
     * @param firstSeriesIndex Index of the first series in the keys of
     * the attributes property. Useful if one needs to concatenate the
     * attributes over consecutive calls of parse on different files.
     * @return true if parsing was successful, false otherwise.
     */
    public boolean parse(int firstSeriesIndex) {

        // If we have already scanned the file we just return true
        if (attr.size() > 0) {
            return true;
        }

        // Initialize file scanned to false
        isFileScanned = false;

        // Store the referenced files
        referencedFiles = processReferencedFiles();

        // Now process the file
        try {

            // Number of series
            numberOfSeries = reader.getSeriesCount();

            // Get and store all series
            for (int i = 0; i < numberOfSeries; i++) {

                // Set the series
                reader.setSeries(i);

                // Create a hashmap for the attributes of current series
                HashMap<String, String> seriesAttr = new HashMap<String, String>();

                // Series number
                seriesAttr.put("numSeries", Integer.toString(i));

                // Image name
                seriesAttr.put("name", omexmlMeta.getImageName(i));

                // Image size X
                seriesAttr.put("sizeX", Integer.toString(reader.getSizeX()));

                // Image size Y
                seriesAttr.put("sizeY", Integer.toString(reader.getSizeY()));

                // Image size Z
                seriesAttr.put("sizeZ", Integer.toString(reader.getSizeZ()));

                // Image size C
                seriesAttr.put("sizeC", Integer.toString(reader.getSizeC()));

                // Image size T
                seriesAttr.put("sizeT", Integer.toString(reader.getSizeT()));

                // Data type
                seriesAttr.put("datatype", getDataType());

                // Is Signed
                seriesAttr.put("isSigned",
                        Boolean.toString(loci.formats.FormatTools.isSigned(
                                reader.getPixelType())));

                // Is Little Endian
                seriesAttr.put("isLittleEndian",
                        Boolean.toString(reader.isLittleEndian()));

                // Acquisition date
                seriesAttr.put("acquisitionDate", getAcquisitionDate(i));

                // Voxel sizes
                double[] voxels = getVoxelSizes(i);
                seriesAttr.put("voxelX", Double.toString(voxels[0]));
                seriesAttr.put("voxelY", Double.toString(voxels[1]));
                seriesAttr.put("voxelZ", Double.toString(voxels[2]));

                // Channel names
                String[] channelNames = getChannelNames(i);
                for (int c = 0; c < reader.getSizeC(); c++) {
                    seriesAttr.put("channelName" + c,
                            Normalizer.normalize(channelNames[c],
                                    Normalizer.Form.NFD));
                }

                // Emission wavelengths
                double[] emWavelengths = getEmissionWavelengths(i);
                for (int c = 0; c < reader.getSizeC(); c++) {
                    seriesAttr.put("emWavelength" + c,
                            Double.toString(emWavelengths[c]));
                }

                // Excitation wavelengths
                double[] exWavelengths = getExcitationWavelengths(i);
                for (int c = 0; c < reader.getSizeC(); c++) {
                    seriesAttr.put("exWavelength" + c,
                            Double.toString(exWavelengths[c]));
                }

                // Stage position
                double[] stagePosition = getStagePosition();
                seriesAttr.put("positionX", Double.toString(stagePosition[0]));
                seriesAttr.put("positionY", Double.toString(stagePosition[1]));

                // Channel colors
                double[][] colors = getChannelColors(i);
                for (int c = 0; c < reader.getSizeC(); c++) {
                    seriesAttr.put("channelColor" + c , "" +
                            colors[c][0] + ", " +
                            colors[c][1] + ", " +
                            colors[c][2] + ", " +
                            colors[c][3]);
                }

                // NAs
                double[] NAs = getNAs();
                if (NAs.length == 0) {
                    seriesAttr.put("NA", Double.toString(Double.NaN));
                } else if (NAs.length == 1) {
                    seriesAttr.put("NA", Double.toString(NAs[0]));
                } else {
                    // In some cases, the bioformats library returns an NA per series
                    // in others just one for all.
                    String strNAs = "";
                    try {
                        strNAs = Double.toString(NAs[i]);
                    } catch (Exception e) {
                        for (int c = 0; c < NAs.length - 1; c++) {
                            strNAs += Double.toString(NAs[c]) + ", ";
                        }
                        strNAs += Double.toString(NAs[NAs.length - 1]);
                    }
                    seriesAttr.put("NA", strNAs);
                }

                // Is the series a thumbnail?
                seriesAttr.put("isThumbnail",
                        String.valueOf(reader.isThumbnailSeries()));

                // Key prefix
                String seriesKey = "series_" + (firstSeriesIndex + i);

                // Now add the metadata for current series
                attr.put(seriesKey, seriesAttr);

            }

        } catch (Exception e) {

            // Reset the attribute map
            attr.clear();

            // Make sure the isFileScanned flag is set to false
            isFileScanned = false;

            // Return failure
            return false;

        }

        // Now we can set the isFileScanned flag to true
        isFileScanned = true;

        // And return success
        return true;

    }

    /**
     * Return the parsed metadata attributes.
     * @return map of series attributes.
     */
    public Map<String, HashMap<String, String>> getAttributes() {
        return attr;
    }

    /**
     * Return the number of series in the file/dataset.
     * @return the number of series.
     */
    public int getNumberOfSeries() {
        return numberOfSeries;
    }

    /**
     * Return the sizeX attribute of the file.
     * @return the size X of the file or -1 if the file is not open.
     */
    public int getSizeX() {
        if (reader == null) {
            return -1;
        } else {
            return reader.getSizeX();
        }
    }

    /**
     * Return the sizeY attribute of the file.
     * @return the size Y of the file or -1 if the file is not open.
     */
    public int getSizeY() {
        if (reader == null) {
            return -1;
        } else {
            return reader.getSizeY();
        }
    }

    /**
     * Return the sizeZ attribute of the file.
     * @return the size Z of the file or -1 if the file is not open.
     */
    public int getSizeZ() {
        if (reader == null) {
            return -1;
        } else {
            return reader.getSizeZ();
        }
    }

    /**
     * Return the sizeC attribute of the file.
     * @return the size C of the file or -1 if the file is not open.
     */
    public int getSizeC() {
        if (reader == null) {
            return -1;
        } else {
            return reader.getSizeC();
        }
    }

    /**
     * Return the sizeT attribute of the file.
     * @return the size T of the file or -1 if the file is not open.
     */
    public int getSizeT() {
        if (reader == null) {
            return -1;
        } else {
            return reader.getSizeT();
        }
    }

    /**
     * Return all referenced files for all series in the dataset as a list of Strings
     * @return List of file names as Strings
     */
    public List<String> getReferencedFiles() {
        return referencedFiles;
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
     * Return the channel excitation wavelengths
     * @param seriesNum index of the series to query
     * @return array of excitation wavelengths.
     * Values that could not be retrieved will be replaced by Double.NaN.
     */
    private double[] getExcitationWavelengths(int seriesNum) {

        // Allocate space to store the channel names
        int nChannels = reader.getSizeC();
        double[] exWavelengths = new double[nChannels];

        for (int i = 0; i < nChannels; i++) {
            Length pEx;
            try {
                pEx = omexmlMeta.getChannelExcitationWavelength(seriesNum, i);
            } catch (IndexOutOfBoundsException e) {
                pEx = null;
            }
            double ex;
            if (pEx != null) {
                ex = (double) pEx.value();
            } else {
                ex = Double.NaN;
            }
            exWavelengths[i] = ex;
        }

        return exWavelengths;
    }

    /**
     * Return the channel emission wavelengths
     * @param seriesNum index of the series to query
     * @return array of emission wavelengths.
     * Values that could not be retrieved will be replaced by Double.NaN.
     */
    private double[] getEmissionWavelengths(int seriesNum) {

        // Allocate space to store the channel names
        int nChannels = reader.getSizeC();
        double[] emWavelengths = new double[nChannels];

        for (int i = 0; i < nChannels; i++) {
            Length pEm;
            try {
                pEm = omexmlMeta.getChannelEmissionWavelength(seriesNum, i);
            } catch (IndexOutOfBoundsException e) {
                pEm = null;
            }
            double em;
            if (pEm != null) {
                em = (double) pEm.value();
            } else {
                em = Double.NaN;
            }
            emWavelengths[i] = em;
        }

        return emWavelengths;
    }

    /**
     * Return the objective numerical aperture(s) for a given series
     * @return array of numerical apertures for the series.
     * Values that could not be retrieved will be replaced by Double.NaN.
     */
    private double [] getNAs() {

        // Number of instruments (usually one per series)
        int nInstr = omexmlMeta.getInstrumentCount();

        // Count objectives for this series (expected: 1)
        int nTotObj = 0;
        for (int i = 0; i < nInstr; i++) {
            for (int j = 0; j < omexmlMeta.getObjectiveCount(i); j++) {
                nTotObj++;
            }
        }

        // Allocate space to store the numerical apertures
        double[] numericalApertures = new double[nTotObj];

        // Get all NAs (expected: 1)
        int c = 0;
        for (int i = 0; i < nInstr; i++) {
            for (int j = 0; j < omexmlMeta.getObjectiveCount(i); j++) {
                Double NA = omexmlMeta.getObjectiveLensNA(i, j);
                if (NA == null) {
                    NA = Double.NaN;
                }
                numericalApertures[c++] = NA;
            }
        }
        return numericalApertures;
    }

    /**
     * Return the channel stage position for current series.
     *
     * The series must have set in advance with reader.setSeries(num).
     *
     * @return array of [X Y] stage position.
     * Values that could not be retrieved will be replaced by Double.NaN.
     */
    private double[] getStagePosition() {

        // Allocate memory to store the stage position
        double[] stagePosition = new double[2];

        // The series has been set earlier

        double x, y;

        // Does the stage position information exist?
        Hashtable<String, Object> metadata = reader.getSeriesMetadata();
        Set<String> keys = metadata.keySet();
        if (keys.contains("X position")) {
            Object m = metadata.get("X position");
            x = (double) ((Length)m).value(); // Java 6 compatibility
        } else {
            x = Double.NaN;
        }

        if (keys.contains("Y position")) {
            Object m = metadata.get("Y position");
            y = (double) ((Length)m).value(); // Java 6 compatibility
        } else {
            y = Double.NaN;
        }

        // Now store the retrieved stage positions
        stagePosition[0] = x;
        stagePosition[1] = y;

        // And return them
        return stagePosition;
    }

    /**
     * Return the channel colors [R, G, B, A]n for current series
     * @param seriesNum index of the series to query
     * @return array of arrays of [R, G, B, A]n, with n = 1 .. nChannels
     * Values that could not be retrieved will be replaced by Double.NaN.
     */
    private double[][] getChannelColors(int seriesNum) {

        // Allocate space to store the channel names
        int nChannels = reader.getSizeC();
        double[][] colors = new double[nChannels][4];

        for (int i = 0; i < nChannels; i++) {
            Color color;
            try {
                color = omexmlMeta.getChannelColor(seriesNum, i);
            } catch (IndexOutOfBoundsException e) {
                color = null;
            }
            if (color == null) {
                if (nChannels == 1) {
                    // If one channel only, make it gray
                    colors[i][0] = 255.0;
                    colors[i][1] = 255.0;
                    colors[i][2] = 255.0;
                    colors[i][3] = 255.0;
                } else {
                    // Recycle after we have run out of default colors
                    int n = i % defaultChannelColors.length;
                    colors[i] = defaultChannelColors[n];
                }
            } else {
                colors[i][0] = color.getRed();
                colors[i][1] = color.getGreen();
                colors[i][2] = color.getBlue();
                colors[i][3] = color.getAlpha();
            }
        }

        // Return the colors
        return colors;
    }

    /**
     * Return the acquisition date for the specified series
     * @param seriesNum index of series to query
     * @return string acquisition date
     * Values that could not be retrieved will be replaced by Double.NaN.
     */
    private String getAcquisitionDate(int seriesNum) {

        Timestamp timestamp = omexmlMeta.getImageAcquisitionDate(seriesNum);
        String acqDate;
        if (timestamp == null) {
            acqDate = "";
        } else {
            acqDate = timestamp.getValue();
        }

        return acqDate;
    }

    /**
     * Return the voxel sizes for given series
     * @param seriesNum index of series to query
     * @return array of doubles [voxelX, voxelY, voxelZ]
     * Values that could not be retrieved will be replaced by Double.NaN.
     */
    private double[] getVoxelSizes(int seriesNum) {

        Length pVoxelX;
        Length pVoxelY;
        Length pVoxelZ;
        double voxelX, voxelY, voxelZ;

        // Voxel size X
        pVoxelX = omexmlMeta.getPixelsPhysicalSizeX(seriesNum);
        if (pVoxelX == null) {
            voxelX = Double.NaN;
        } else {
            voxelX = (double) pVoxelX.value();
        }

        // Voxel size Y
        pVoxelY = omexmlMeta.getPixelsPhysicalSizeY(seriesNum);
        if (pVoxelY == null) {
            if (pVoxelX == null) {
                voxelY = Double.NaN;
            } else {
                // Some formats store just one size for X and Y,
                // if they are equal.
                voxelY = voxelX;
            }

        } else {
            voxelY = (double) pVoxelY.value();
        }

        // Voxel size Z
        pVoxelZ = omexmlMeta.getPixelsPhysicalSizeZ(seriesNum);
        if (pVoxelZ == null) {
            voxelZ = Double.NaN;
        } else {
            voxelZ = (double) pVoxelZ.value();
        }

        // Return
        return new double[]{voxelX, voxelY, voxelZ};

    }

    /**
     * Return the channel names
     * @param seriesNum index of the series to query
     * @return array of channel names
     */
    private String[] getChannelNames(int seriesNum) {

        // Allocate space to store the channel names
        int nChannels = reader.getSizeC();
        String[] channelNames = new String[nChannels];

        for (int i = 0; i < nChannels; i++) {
            String channelName;
            try {
                channelName = omexmlMeta.getChannelName(seriesNum, i);
            } catch (IndexOutOfBoundsException e) {
                channelName = null;
            }
            if (channelName == null) {
                channelName = "CHANNEL_" + i;
            }
            // Make sure to remove "\0" end of the name
            // (as found for instance in LSM files)
            if (channelName.endsWith("\0")) {
                channelName = channelName.substring(0, channelName.length() - 1);
            }
            channelNames[i] = channelName;
        }

        return channelNames;
    }

    /**
     * Return the list of series indices extracted from the file names
     * @return List of series indices
     */
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
     * Return the difference of two lists
     * @param series1 First list
     * @param series2 Second list
     * @return a list that contains only the elements that are only in series 1.
     */
    private <T> List<T> seriesDifference(List<T> series1, List<T> series2) {

        // Chabge the lists into Sets
        Set<T> setSeries1 = new LinkedHashSet<T>(series1);
        Set<T> setSeries2 = new LinkedHashSet<T>(series2);
        setSeries1.removeAll(setSeries2);

        // Return the file names in the difference set as list
        List<T> diffList = new ArrayList<T>();
        diffList.addAll(setSeries1);

        return diffList;
    }

    /**
     * Convert a FileInfo array into a List of filename Strings.
     * @param fileInfo List of strings.
     */
    private List<String> FileInfoToList(FileInfo [] fileInfo) {
        List<String> fileNames = new ArrayList<String>();
        for (int i = 0; i < fileInfo.length; i++) {
            fileNames.add(fileInfo[i].filename);
        }
        return fileNames;
    }

    /**
     * Get the list of referenced files (without the ND file)
     * @return list of references file names.
     */
    private List<String> processReferencedFiles() {
        List<String> noPixFiles = FileInfoToList(reader.getAdvancedUsedFiles(false));
        List<String> pixFiles = FileInfoToList(reader.getAdvancedUsedFiles(true));
        List<String> seriesDiff = seriesDifference(noPixFiles, pixFiles);
        return seriesDiff;
    }
}
