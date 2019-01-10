package ch.ethz.scu.obit.microscopy.processors.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ch.ethz.scu.obit.microscopy.readers.BioFormatsWrapper;
import ch.ethz.scu.obit.microscopy.readers.MicroscopyReader;
import ch.ethz.scu.obit.microscopy.readers.composite.AbstractCompositeMicroscopyReader;
import ch.ethz.scu.obit.microscopy.readers.composite.CompositeMicroscopyReaderFactory;
import ch.ethz.scu.obit.processors.AbstractProcessor;
import ch.ethz.scu.obit.processors.data.model.AbstractDescriptor;
import ch.ethz.scu.obit.processors.data.model.CollectionDescriptor;
import ch.ethz.scu.obit.processors.data.model.DatasetDescriptor;
import ch.ethz.scu.obit.processors.data.model.ExperimentDescriptor;
import ch.ethz.scu.obit.processors.data.model.PathAwareDescriptor;
import ch.ethz.scu.obit.processors.data.model.RootDescriptor;
import ch.ethz.scu.obit.processors.data.validator.GenericValidator;

/**
 * MicroscopyProcessor parses microscopy files (using the bio-formats library).
 *
 * @author Aaron Ponti
 */
public final class MicroscopyProcessor extends AbstractProcessor {

    private static final int ROOT_LEVEL = 0;
    private static final int EXPERIMENT_LEVEL = 1;
    private static final int DATASET_LEVEL = 2;
    private static final int COMPOSITE_DATASET_LEVEL = 3;

    /* Folder to scan (recursively) */
    private File userFolder;

    private File userRootFolder;

    /* Keep track of the folder level when recursing into subfolders */
    private int folderLevel = ROOT_LEVEL;

    /* List of supported file formats
     *
     * This is the list of extensions that should be readable by bioformats.
     * Some of them would not be compatible with oBIT at this stage, and so
     * the actual list of supported formats is much shorter.
     *
     *      ".1sc", ".2", ".2fl", ".3", ".4", ".5", ".acff",
     *      ".afm", ".aim", ".al3d", ".am", ".amiramesh", ".apl",
     *      ".arf", ".avi", ".bip", ".bmp", ".c01", ".cfg", ".cr2",
     *      ".crw", ".cxd", ".czi", ".dat", ".dcm", ".dicom", ".dm2",
     *      ".dm3", ".dti", ".dv", ".eps", ".epsi", ".exp", ".fdf",
     *      ".fff", ".ffr", ".fits", ".flex", ".fli", ".frm", ".gel",
     *      ".gif", ".grey", ".hdr", ".hed", ".his", ".htd", ".html",
     *      ".hx", ".ics", ".ids", ".img", ".ims", ".inr", ".ipl",
     *      ".ipm", ".ipw", ".jp2", ".jpg", ".jpk", ".jpx", ".l2d",
     *      ".labels", ".lei", ".lif", ".liff", ".lim", ".lsm", ".mdb",
     *      ".mea", ".mnc", ".mng", ".mod", ".mov", ".mrc", ".mrw",
     *      ".mtb", ".mvd2", ".naf", ".nd", ".nd2", ".ndpi", ".nef",
     *      ".nhdr", ".nrrd", ".obsep", ".oib", ".oif", ".ome", ".par",
     *      ".pcx", ".pds", ".pgm", ".pic", ".pict", ".png", ".pnl",
     *      ".pr3", ".ps", ".psd", ".r3d", ".raw", ".res", ".scn",
     *      ".sdt", ".seq", ".sld", ".sm2", ".sm3", ".spi", ".stk",
     *      ".stp", ".svs", ".sxm", ".tfr", ".tga", ".tif", ".tiff",
     *      ".tnb", ".top", ".txt", ".v", ".vms", ".vsi", ".vws",
     *      ".wat", ".xdce", ".xml", ".xqd", ".xqf", ".xv", ".xys",
     *      ".zfp", ".zfr", ".zvi"));
     */
    private final ArrayList<String> supportedFormats = new ArrayList<String>(
            Arrays.asList(".czi", ".dv", ".ics", ".ids", ".ims", ".lei",
                    ".lif", ".liff", ".lsm", ".nd2", ".oib", ".oif",
                    ".ome", ".r3d", ".stk", ".tif", ".tiff", ".zvi"));

    /**
     * Descriptor for the user folder (root of the tree).
     */
    public UserFolder folderDescriptor = null;

    /**
     * Constructor
     *
     * @param fullFolderName
     *            Full path of the folder containing the exported experiment.
     */
    public MicroscopyProcessor(String fullFolderName) {

        // Instantiate the validator
        validator = new GenericValidator();

        // fullFolderName MUST be a folder! If it is not, there is
        // a major problem with the software setup!
        File folder = new File(fullFolderName);
        if (!folder.isDirectory()) {
            // TODO Proper handling of this case.
            System.err.println("Expected user folder, found file!");
            System.exit(1);
        }

        // Set the root folder
        this.userFolder = folder;
        this.userRootFolder = folder.getParentFile();

        // Create a descriptor for the user folder
        folderDescriptor = new UserFolder(folder, userRootFolder);

    }

    /**
     * Return the full path of the processed folder processed.
     *
     * @return path of the processed folder.
     */
    public File getFolder() {
        return this.userFolder;
    }

    @Override
    public boolean parse() {

        // The constructor already made sure that this.userFolder is
        // indeed a folder. So we can scan it recursively to find and
        // reconstruct the structure of all contained experiments.
        try {
            folderLevel = ROOT_LEVEL;
            recursiveDir(this.userFolder);
        } catch (IOException e) {
            this.errorMessage = "Could not parse the folder.";
            System.err.println(errorMessage);
            return false;
        }

        // Success
        this.errorMessage = "";
        return true;
    }

    @Override
    public String info() {
        return "Bioformats-compatible microscopy file format";
    }

    @Override
    public String getType() {
        // Return type
        return "MICROSCOPY";
    }

    /**
     * Scan the folder recursively and process all microscopy files found
     *
     * @param dir
     *            Full path to the directory to scan
     * @throws IOException
     *             Thrown if a file could not be processed
     */
    private void recursiveDir(File dir) throws IOException {

        // We do not allow recursion above folderLevel = DATASET_LEVEL == 2;
        // deeper levels must be taken care of by the CompositeMicroscopyReaders.
        if (folderLevel == DATASET_LEVEL) {
            return;
        }

        // Declare an AbstractCompositeMicroscopyReader
        AbstractCompositeMicroscopyReader reader = null;

        // Keep track whether we are processing a composite microscopy dataset
        boolean isCompositeDataset = false;

        // Update the folder level
        folderLevel++;

        // Get the directory listing
        String[] files = dir.list();
        if (files == null) {
            return;
        }

        // Empty subfolders are not accepted
        if (files.length == 0 && !dir.equals(this.userFolder)) {
            validator.isValid = false;
            validator.invalidFilesOrFolders.put(dir, "Empty folder");
            return;
        }

        // Go over the files and folders
        String fileName = "";
        for (String f : files) {

            File file = new File(dir + File.separator + f);

            // Is it a directory?
            if (file.isDirectory()) {

                if (folderLevel < DATASET_LEVEL) {

                    // Recurse into the subfolder
                    recursiveDir(file);

                    // Move on to the next file/folder
                    continue;

                } else {

                    // Get a reader to process the folder as a composite
                    // microscope type
                    reader = CompositeMicroscopyReaderFactory.createReader(file);
                    if (reader == null) {
                        validator.isValid = false;
                        validator.invalidFilesOrFolders.put(file,
                                "Unsupported composite file format in folder " + file);
                        continue;
                    }

                    // Now process the folder
                    try {

                        // Parse the folder
                        if (!reader.parse()) {
                            validator.isValid = false;
                            validator.invalidFilesOrFolders.put(file,
                                    reader.getErrorMessage());
                            continue;
                        }

                        // Label the folder as composite
                        isCompositeDataset = true;

                    } catch (Exception e) {
                        validator.isValid = false;
                        validator.invalidFilesOrFolders.put(file,
                                "Could not process folder " + file);
                        continue;
                    }

                }

            } else {

                // Label the file as standard (non-composite) dataset
                if (folderLevel == DATASET_LEVEL) {
                    isCompositeDataset = false;
                }

                // Delete some known garbage
                if (deleteIfKnownUselessFile(file)) {
                    continue;
                }

                // Check the file type.
                fileName = file.getName();
                int indx = fileName.lastIndexOf(".");
                if (indx == -1) {
                    continue;
                }
                String ext = fileName.substring(indx);

                // Check whether we find a data_structure.ois file. This
                // means that the whole folder has apparently been annotated
                // already, but for some unknown reason it has not been
                // moved into Datamover's incoming folder.
                // We break here.
                if (fileName.toLowerCase().equals("data_structure.ois")) {
                    validator.isValid = false;
                    validator.invalidFilesOrFolders.put(file,
                            "Failed registration to openBIS!");
                    return;
                }

                // Check whether an experiment is already annotated. Please
                // mind that at this stage we do not know WHICH experiment
                // was annotated. We just react to the fact that at least
                // one has been annotated, somewhere.
                if (fileName.toLowerCase().contains("_properties.oix")) {
                    validator.isValid = false;
                    validator.invalidFilesOrFolders.put(file,
                            "Experiment already annotated");
                    return;
                }

                // A microscopy file cannot be at the user folder root!
                if (file.getParent().equals(this.userFolder.toString())) {
                    validator.isValid = false;
                    validator.invalidFilesOrFolders.put(file,
                            "File must be in subfolder.");
                    continue;
                }

                // Do we have an unknown file (i.e. neither a supported dataset
                // format nor a valid attachment)? If we do, we add it to the
                // list of invalid files and move on to the next.
                if (!supportedFormats.contains(ext.toLowerCase()) &&
                        !ExperimentDescriptor.isValidAttachment(file)) {
                    validator.isValid = false;
                    validator.invalidFilesOrFolders.put(file,
                            "Invalid file type.");
                    continue;
                }

                // Failed Nikon acquisitions are known to create a 4096-byte header
                // only file that the bio-formats library will fail reading. We
                // check for such a case to prevent that the registration
                // will fail on the DataStore Server.
                if (ext.toLowerCase().equals(".nd2") && file.length() <= 4096) {
                    // We try opening the file -- if it fails, we flag it as corrupted.
                    try {
                        BioFormatsWrapper wrapper = new BioFormatsWrapper(file);
                        if (!wrapper.parse()) {
                            throw new Exception("Parsing the file " + file.getName() + " failed!");
                        }
                        wrapper.close();
                    } catch (Exception e) {
                        validator.isValid = false;
                        validator.invalidFilesOrFolders.put(file,
                                "The file seems to be corrupted.");
                        continue;
                    }
                }
            }

            // Create a new ExperimentDescriptor or reuse an existing one
            // The name of the experiment is the name of the folder that
            // contains current file
            Experiment expDesc;
            String experimentName = dir.getName();
            if (folderDescriptor.collectionDescriptor.experiments.containsKey(experimentName)) {
                expDesc = folderDescriptor.collectionDescriptor.experiments.get(experimentName);
            } else {
                expDesc = new Experiment(dir);
                folderDescriptor.collectionDescriptor.experiments.put(experimentName, expDesc);
            }

            // If the file is an attachment, add it and move on
            if (ExperimentDescriptor.isValidAttachment(file)) {

                if (! expDesc.addAttachment(file)) {
                    validator.isValid = false;
                    validator.invalidFilesOrFolders.put(file,
                            "Could not assign attachment to esperiment!");
                }

                // Move on to the next file
                continue;

            }

            // Now add the dataset to the tree
            if (isCompositeDataset) {

                // Store the composite microscopy dataset.
                //
                // The isParsed() call is not strictly necessary, since in
                // case something went wrong with the parsing, we would not
                //  reach this point (see the try .. catch block above).
                if (reader.isParsed()) {

                    // Store
                    String microscopyCompositeFileName = reader.getName();
                    String microscopyCompositeFileKey = experimentName + "_"
                            + microscopyCompositeFileName;

                    // Store it in the Experiment descriptor
                    if (! expDesc.microscopyCompositeFiles.containsKey(microscopyCompositeFileKey)) {
                        MicroscopyCompositeFile microscopyCompositeFileDesc;
                        microscopyCompositeFileDesc = new MicroscopyCompositeFile(reader, userRootFolder);
                        expDesc.microscopyCompositeFiles.put(microscopyCompositeFileKey,
                                microscopyCompositeFileDesc);
                    }

                }

            } else {

                // Normal dataset

                // Store
                MicroscopyFile microscopyFileDesc;
                String microscopyFileName = fileName;
                String microscopyFileKey = experimentName + "_"
                        + microscopyFileName;
                microscopyFileDesc = new MicroscopyFile(file, userRootFolder);

                // Store it in the Experiment descriptor
                expDesc.microscopyFiles.put(microscopyFileKey,
                        microscopyFileDesc);

            }

            // This should not happen!
            if (folderLevel > DATASET_LEVEL) {

                // Do we have an unknown file? If we do, we move on to the next.
                validator.isValid = false;
                validator.invalidFilesOrFolders.put(file,
                        "Unexpectedly deep folder hierarchy.");
            }

        }

        // We are about to leave the folder, we reduce the folder level
        folderLevel--;
    }

    /**
     * Deletes safe-to-remove microscopy-related files. It also
     * invokes the parent method to delete common safe-to-remove
     * files like .DS_Store, Thumbs.db, ...
     * @param file File to be checked.
     * @return true if the file was recognized as a useless hidden
     * file and deleted, false if the file is a relevant file to be
     * processed.
     */
    @Override
    protected boolean deleteIfKnownUselessFile(File file) {

        // Call parent method
        if (super.deleteIfKnownUselessFile(file) == true) {
            return true;
        }

        if (file.isDirectory()) {
            return false;
        }

        // Check for microscopy-related files
        String name = file.getName();
        if (name.toLowerCase().endsWith(".lifext")) {
            return file.delete();
        } else {
            return false;
        }
    }

    /**
     * Descriptor that represents a folder containing a dataset.
     *
     * @author Aaron Ponti
     */
    public class Folder extends PathAwareDescriptor {

        /**
         * Hash map of experiments
         */
        public final Map<String, Experiment> experiments =
                new LinkedHashMap<String, Experiment>();

        /**
         * Constructor
         * @param fullFolder Full path to the folder
         * @param userRootDataPath Full path to the user data folder.
         */
        public Folder(File fullFolder, File userRootDataPath) {

            // Invoke parent constructor
            super(fullFolder, userRootDataPath);

            // Set the descriptor name
            this.setName(fullFolder.getName());

        }

        @Override
        public String getType() {
            return "Folder";
        }

    }

    /**
     * Descriptor representing an experiment obtained from the FCS file.
     *
     * @author Aaron Ponti
     */
    public class Experiment extends ExperimentDescriptor {

        /**
         *  Experiment version
         * This is used to keep track of the structure of the experiment
         * so that older versions of Experiments stored in openBIS are
         * recognized and can potentially be upgraded.
         */
        public final String version = "1";

        /**
         *  Experiment description
         */
        public String description = "";

        /**
         *  Experiment tags (comma-separated list)
         */
        public String tags = "";

        /**
         *  Store the microscopy files associated with this Experiment
         */
        public final Map<String, MicroscopyFile> microscopyFiles =
                new LinkedHashMap<String, MicroscopyFile>();

        /**
         * Hash map of microscopy composite file descriptors.
         */
        public final Map<String, MicroscopyCompositeFile> microscopyCompositeFiles =
                new LinkedHashMap<String, MicroscopyCompositeFile>();

        /**
         * Constructor
         *
         * @param name
         *            Name of the experiment.
         */
        public Experiment(File name) {

            // Call base constructor
            super(name, userRootFolder);

            // Store the experiment name
            this.setName(name.getName());

        }

        /**
         * Return a simplified class name to use in XML.
         *
         * @return simplified class name.
         */
        @Override
        public String getType() {
            return "Experiment";
        }

    }

    /**
     * Descriptor that represents a the user top folder.
     *
     * @author Aaron Ponti
     */
    public class UserFolder extends RootDescriptor {

        /**
         * Descriptor for the experiment collection.
         */
        public Collection collectionDescriptor = null;

        /**
         * Constructor
         * @param fullFolder Full path to the folder
         * @param userRootFolder Full path to the user data folder.
         */
        public UserFolder(File fullFolder, File userRootFolder) {

            // Invoke parent constructor
            super(fullFolder, userRootFolder);

            // Set the descriptor name
            this.setName(fullFolder.getName());

            // Create the Collection descriptor
            collectionDescriptor = new Collection();
        }

        @Override
        public String getType() {
            return "Folder";
        }

    }

    public class Collection extends CollectionDescriptor {

        /**
         * Hash map of experiments.
         */
        public final Map<String, Experiment> experiments =
                new LinkedHashMap<String, Experiment>();

        /**
         * Constructor
         * @param fullFolder Full path to the folder
         * @param userRootFolder Full path to the user data folder.
         */
        public Collection() {

            // Invoke parent constructor
            super();

            // Set the descriptor name
            this.setName("All experiments");

        }

        @Override
        public String getType() {
            return "Collection";
        }
    }

    /**
     * Descriptor representing a microscopy file.
     *
     * @author Aaron Ponti
     */
    public class MicroscopyFile extends DatasetDescriptor {

        private boolean fileScanned = false;

        private final Map<String, MicroscopyFileSeries> series =
                new LinkedHashMap<String, MicroscopyFileSeries>();

        /**
         * Constructor.
         *
         * @param microscopyFileName Microscopy file name with full path.
         * @param userRootDataPath Full path the the user data folder.
         * @throws IOException if parsing the microscopy file failed.
         */
        public MicroscopyFile(File microscopyFileName, File userRootDataPath) throws IOException {

            // Call base constructor
            super(microscopyFileName, userRootDataPath);

            // Store the file name
            this.setName(microscopyFileName.getName());

            // Append the attibute file size.
            long s = microscopyFileName.length();
            attributes.put("datasetSize", String.format("%d", s));

            // Append the attribute relative file name. Since this
            // will be used by the openBIS dropboxes running on a Unix
            // machine, we make sure to use forward slashes for path
            // separators when we set it as an attribute.
            attributes.put("relativeFileName",
                    this.relativePath.replace("\\", "/"));
        }

        /**
         * Return a String representation of the extracted microscopy file.
         *
         * @return String representation of the microscopy file.
         */
        @Override
        public String toString() {
            return getName();
        }

        /**
         * Return a simplified class name to use in XML.
         *
         * @return simplified class name.
         */
        @Override
        public String getType() {
            return "MicroscopyFile";
        }

        /**
         * Scans the file and stores the metadata into the attributes
         * String-String map
         */
        @Override
        public Map<String, String> getAttributes() {

            // Return the attributes
            return attributes;
        }

        /**
         * Scans the file and stores the metadata into the attributes
         * String-String map
         * @return true of the scan was successful, false otherwise.
         */
        public boolean scanForSeries() {

            // If the file was already scanned we return the attributes
            if (fileScanned) {
                return true;
            }

            // Scan the file for series
            MicroscopyReader microscopyReader = new MicroscopyReader(
                    this.fullPath);
            boolean success = microscopyReader.parse();
            microscopyReader.close();
            if (!success) {

                // Add this file to the list of invalid datasets
                validator.isValid = false;
                validator.invalidFilesOrFolders.put(this.fullPath,
                        "Metadata parsing failed.");

                // Return failure
                fileScanned = false;
                return false;

            } else {
                fileScanned = true;
            }

            // Now update the data model

            // Get the attributes for the series
            Map<String, HashMap<String, String>> seriesAttr = microscopyReader
                    .getAttributes();

            // Process all series
            for (int i = 0; i < seriesAttr.size(); i++) {

                // Series key
                String keySeries = "series_" + i;

                // Create a new MicroscopyFileSeries descriptor
                MicroscopyFileSeries fileSeries = new MicroscopyFileSeries(i,
                        seriesAttr.get(keySeries));

                // Append it to the MicroscopyFile descriptor
                series.put(keySeries, fileSeries);
            }

            // Retun success
            return true;

        }

        /**
         * Return the extracted microscopy file series.
         * @return String, MicroscopyFileSeries map
         */
        public Map<String, MicroscopyFileSeries> getSeries() {
            return series;
        }
    }

    /**
     * Descriptor representing a microscopy file.
     *
     * @author Aaron Ponti
     */
    public class MicroscopyCompositeFile extends DatasetDescriptor {

        private AbstractCompositeMicroscopyReader reader;

        private List<Integer> seriesIndices = new ArrayList<Integer>();

        /**
         * Constructor.
         * @param reader An AbstractCompositeMicroscopyReader object: in reality,
         *               a concrete specialization.
         * @param userRootDataPath The full path to the root of the user data
         *               folder.
         *
         * @throws IOException if scanning the folder fails.
         */
        public MicroscopyCompositeFile(AbstractCompositeMicroscopyReader reader, File userRootDataPath) throws IOException {

            // Call base constructor
            super(reader.getFolder(), userRootDataPath);

            // Store the file name
            this.setName(reader.getName());

            // Store the reader
            this.reader = reader;

            // Store the type of the composite reader
            attributes.put("compositeFileType", reader.getType());

            // Store the series indices
            seriesIndices = reader.getSeriesIndices();
            String seriesIndicesStr = StringUtils.join(seriesIndices, ',');
            attributes.put("seriesIndices", seriesIndicesStr);

            // Append the attibute file size.
            long s = reader.totalDatasetSizeInBytes();
            attributes.put("datasetSize", String.format("%d", s));

            // Append the attribute relative folder. Since this
            // will be used by the openBIS dropboxes running on a Unix
            // machine, we make sure to use forward slashes for path
            // separators when we set it as an attribute.
            attributes.put("relativeFolder",
                    this.relativePath.replace("\\", "/"));

        }

        /**
         * Return a String representation of the extracted microscopy file.
         *
         * @return String representation of the microscopy file.
         */
        @Override
        public String toString() {
            return getName();
        }

        /**
         * Return a simplified class name to use in XML.
         *
         * @return simplified class name.
         */
        @Override
        public String getType() {
            return "MicroscopyCompositeFile";
        }

        /**
         * Scans the file and stores the metadata into the attributes
         * String-String map
         */
        @Override
        public Map<String, String> getAttributes() {

            // Return the attributes
            return attributes;
        }

        /**
         * Return the extracted microscopy file series.
         * @return String, MicroscopyFileSeries map
         */
        public Map<String, MicroscopyFileSeries> getSeries() {

            // Initialize the series map
            Map<String, MicroscopyFileSeries> series =
                    new LinkedHashMap<String, MicroscopyFileSeries>();

            // Get the series attributes
            Map<String, HashMap<String, String>> seriesAttr =
                    reader.getAttributes();

            // Get the series indices

            // Process all series
            for (int i = 0; i < seriesAttr.size(); i++) {

                // Series index
                int index = seriesIndices.get(i);

                // Series key
                String keySeries = "series_" + index;

                // Create a new MicroscopyFileSeries descriptor
                MicroscopyFileSeries fileSeries = new MicroscopyFileSeries(
                        index, seriesAttr.get(keySeries));

                // Append it to the MicroscopyFile descriptor
                series.put(keySeries, fileSeries);
            }

            return series;
        }
    }

    /**
     * Descriptor representing a microscopy file series.
     *
     * @author Aaron Ponti
     */
    public class MicroscopyFileSeries extends AbstractDescriptor {

        /**
         * Constructor.
         *
         * @param index
         *            Index of the series in file.
         * @param attr
         *            String-string map of attributes for the series
         */
        public MicroscopyFileSeries(int index, Map<String, String> attr) {

            // Call base constructor
            super();

            // Store the attributes
            attributes = attr;

            // Store the file name
            if (attr.containsKey("name")) {
                this.setName(attr.get("name"));
            } else {
                this.setName("series_" + index);
            }
        }

        /**
         * Return a String representation of the extracted microscopy file.
         *
         * @return String representation of the microscopy file.
         */
        @Override
        public String toString() {
            return getName();
        }

        /**
         * Return a simplified class name to use in XML.
         *
         * @return simplified class name.
         */
        @Override
        public String getType() {
            return "MicroscopyFileSeries";
        }

        /**
         * Scans the file and stores the metadata into the attributes
         * String-String map
         */
        @Override
        public Map<String, String> getAttributes() {

            // Return the attributes
            return attributes;
        }

    }

}
