package ch.ethz.scu.obit.flow.processors.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.scu.obit.flow.processors.data.model.AnalyzerExperiment;
import ch.ethz.scu.obit.flow.processors.data.model.FCSFileParameterList;
import ch.ethz.scu.obit.flow.processors.data.model.Specimen;
import ch.ethz.scu.obit.flow.processors.data.model.Tray;
import ch.ethz.scu.obit.flow.processors.data.model.Tube;
import ch.ethz.scu.obit.flow.processors.data.model.Well;
import ch.ethz.scu.obit.flow.readers.FCSReader;
import ch.ethz.scu.obit.processors.data.model.ExperimentDescriptor;

/**
 * BCCytoFLEXSFlowProcessor parses folder structures created by the following
 * software and hardware combination:
 *
 * BC CytExpert software (2.x) on BC CytoFLEX S
 *
 * @author Aaron Ponti
 */
public class BCCytoFLEXSFlowProcessor extends AbstractFlowProcessor {

    /* Map of known hardware strings to supported hardware */
    private static final Map<String, String> knownHardwareStrings;
    static {
        knownHardwareStrings = new HashMap<String, String>();

        // BD LSR Fortessa
        knownHardwareStrings.put("CytoFLEX S", "CytoFLEX S");
    }

    /**
     * Constructor
     *
     * @param fullUserFolderName Full path of the user folder containing the
     *                           exported experiments.
     */
    public BCCytoFLEXSFlowProcessor(String fullUserFolderName) {

        // Call base constructor
        super(fullUserFolderName);

    }

    /**
     * Return information regarding the file format.
     *
     * @return descriptive String for the Processor.
     */
    @Override
    public String info() {
        return "FCS-3.x based blow cytometry hardware";
    }

    /**
     * Parse the file to extract data and metadata.
     *
     * @return true if parsing was successful, false otherwise.
     */
    @Override
    public boolean parse() {

        // The constructor already made sure that this.userFolder is
        // indeed a folder. So we can scan it recursively to find and
        // reconstruct the structure of all contained experiments.
        try {
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

    /**
     * Return a String representation of the BDLSRFortessaFlowProcessor.
     *
     * @return String containing a description of the
     *         BDLSRFortessaFlowProcessor.
     */
    @Override
    public String toString() {
        return userFolder.getName();
    }

    /**
     * Return a simplified class name to use in XML.
     *
     * @return simplified class name.
     */
    @Override
    public String getType() {
        return "FLOW";
    }

    /**
     * Extract and store the Experiment attributes
     *
     * @param processor FCSProcessor with already scanned file
     * @return a key-value map of attributes
     */
    protected Map<String, String> getExperimentAttributes(FCSReader processor) {
        Map<String, String> attributes = new HashMap<String, String>();

        // Owner name
        attributes.put("owner_name", processor.getStandardKeyword("$OP"));

        // Hardware string
        String acqHardwareString = processor.getStandardKeyword("$CYT");
        if (knownHardwareStrings.containsKey(acqHardwareString)) {
            // Standardize the hardware string
            acqHardwareString = knownHardwareStrings.get(acqHardwareString);
        } else {
            validator.isValid = false;
            validator.invalidFilesOrFolders.put(processor.getFile(),
                    "Wrong hardware string: " + acqHardwareString);
        }
        attributes.put("acq_hardware", acqHardwareString);

        // Software string
        String acqSoftwareString = processor.getCustomKeyword("SWVER");
        if (acqSoftwareString.isEmpty()) {
            acqSoftwareString = "UNKNOWN";
        }

        // Check major and minor version (we ignore the patch)
        Pattern p = Pattern.compile(
                "(.*?)(\\d{1,2})\\.(\\d{1,2})(\\.\\d{1,2})?(\\.\\d{1,2})?");
        Matcher m = p.matcher(acqSoftwareString);
        if (!m.matches()) {
            validator.isValid = false;
            validator.invalidFilesOrFolders.put(processor.getFile(),
                    "Unknown software version.");
        } else {
            int major;
            try {
                major = Integer.parseInt(m.group(2));
                // Known valid version is 2.3. The assumption is that 2.x and
                // anything younger are fine.
                if (major < 2) {
                    validator.isValid = false;
                    validator.invalidFilesOrFolders.put(processor.getFile(),
                            "Unsupported software version: " + m.group(0));
                }
            } catch (NumberFormatException n) {
                validator.isValid = false;
                validator.invalidFilesOrFolders.put(processor.getFile(),
                        "Unknown software version.");
            }
        }

        // Acquisition software
        attributes.put("acq_software", acqSoftwareString);

        // Acquisition date
        attributes.put("date", processor.getStandardKeyword("$DATE"));

        return attributes;
    }

    /**
     * Scan the folder recursively and process all fcs files found
     *
     * @param dir Full path to the directory to scan
     * @throws IOException Thrown if a FCS file could not be processed
     */
    @Override
    protected void recursiveDir(File dir) throws IOException {

        // To make things simple and robust, we make sure that the first
        // thing we process at any sub-folder level is an FCS file.
        String[] files = getSimplySortedList(dir);

        // Empty subfolders are not accepted
        if (files.length == 0 && !dir.equals(this.userFolder)) {
            validator.isValid = false;
            validator.invalidFilesOrFolders.put(dir, "Empty folder");
            return;
        }

        // Go over the files and folders
        for (String f : files) {

            File file = new File(dir + File.separator + f);

            // Is it a directory? Recurse into it
            if (file.isDirectory()) {

                // Recurse into the subfolder
                recursiveDir(file);

                // Move on to the next file
                continue;
            }

            // No files are allowed in the root
            if (dir.equals(this.userFolder)) {
                validator.isValid = false;
                validator.invalidFilesOrFolders.put(file,
                        "Files must be in sub-folders.");
                return;
            }

            // Delete some known garbage
            if (deleteIfKnownUselessFile(file)) {
                continue;
            }

            // A file 'ExpSummaryForAPI.xml' is exported along with the FCS
            // that contains metadata that *might* be used to complement the
            // information extracted from the FCS files.
            // @TODO: Process the file
            String fileName = file.getName();
            int indx = fileName.lastIndexOf(".");
            if (indx == -1) {
                continue;
            }
            String ext = fileName.substring(indx);
            if (ext.equalsIgnoreCase(".xml")) {
                // @TODO: Parse!
                continue;
            }

            // Check whether the file is a valid attachment
            if (ExperimentDescriptor.isValidAttachment(file)) {

                // By design, when we find an attachment, the corresponding
                // Experiment must exist
                if (currentExperiment == null) {
                    validator.isValid = false;
                    validator.invalidFilesOrFolders.put(file,
                            "This attachment does not seem to be assigned"
                                    + " to any experiment!");
                    continue;
                }

                // Attach the files
                if (!currentExperiment.addAttachment(file)) {
                    validator.isValid = false;
                    validator.invalidFilesOrFolders.put(file,
                            "Could not assign attachments to esperiment!");
                    continue;
                }

                continue;
            }

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
            if (fileName.contains("_properties.oix")) {
                validator.isValid = false;
                validator.invalidFilesOrFolders.put(file,
                        "Experiment already annotated");
                return;
            }

            // Do we have an unknown file? If we do, we move on to the next.
            if (!ext.equalsIgnoreCase(".fcs")) {
                validator.isValid = false;
                validator.invalidFilesOrFolders.put(file,
                        "Unsupported file format");
                continue;
            }

            // Is it an FCS file? Scan it and extract the information
            FCSReader processor = new FCSReader(file, false);
            if (!processor.parse()) {
                System.err.println("File " + file.getCanonicalPath()
                        + " could not be parsed!");
                validator.isValid = false;
                validator.invalidFilesOrFolders.put(file, "Parsing failed");
                continue;
            }

            // Create a new ExperimentDescriptor or reuse an existing one
            AnalyzerExperiment expDesc = null;
            String experimentName = getExperimentName(processor);
            String experimentPath = getExperimentPath(processor, file);
            if (experimentPath.equals("")) {
                validator.isValid = false;
                validator.invalidFilesOrFolders.put(file,
                        "Containing folder name does not match experiment "
                                + "name (" + experimentName + ").");
                continue;
            }
            if (folderDescriptor.experiments.containsKey(experimentPath)) {
                expDesc = (AnalyzerExperiment) folderDescriptor.experiments
                        .get(experimentPath);
            } else {
                expDesc = new AnalyzerExperiment(new File(experimentPath),
                        experimentName, userRootFolder);
                // Store attributes
                expDesc.addAttributes(getExperimentAttributes(processor));
                folderDescriptor.experiments.put(experimentPath, expDesc);
            }

            // Keep track of current experiment
            currentExperiment = expDesc;

            // Is the container a Tray or Specimen?
            if (identifyContainerType(processor).equals("TRAY")) {

                // Create a new TrayDescriptor or reuse an existing one
                Tray trayDesc;
                String trayName = getTrayName(processor);
                String trayKey = experimentName + "_" + trayName;
                if (expDesc.trays.containsKey(trayKey)) {
                    trayDesc = expDesc.trays.get(trayKey);
                } else {
                    trayDesc = new Tray(trayName);
                    // Store attributes
                    trayDesc.addAttributes(getTrayAttributes(processor));
                    // Store it in the experiment descriptor
                    expDesc.trays.put(trayKey, trayDesc);
                }

                // Create a new Specimen or reuse an existing one
                Specimen specDesc;
                String specName = getSpecimenName(processor);
                String specKey = trayKey + "_" + specName;
                if (trayDesc.specimens.containsKey(specKey)) {
                    specDesc = trayDesc.specimens.get(specKey);
                } else {
                    specDesc = new Specimen(specName);
                    // Store attributes
                    specDesc.addAttributes(getSpecimenAttributes(processor));
                    // Store it in the tray descriptor
                    trayDesc.specimens.put(specKey, specDesc);
                }

                // Create a new Well descriptor or reuse an existing one
                Well wellDesc;
                String wellName = getTubeOrWellName(processor);
                String wellKey = specKey + "_" + wellName;
                if (!specDesc.tubes.containsKey(wellKey)) {
                    wellDesc = new Well(wellName, file, userRootFolder);
                    // Store attributes
                    wellDesc.addAttributes(getTubeOrWellAttributes(processor));
                    // Store events and parameter attributes
                    wellDesc.fcsFile.parameterList = new FCSFileParameterList(
                            processor.numEvents(), processor.numParameters(),
                            processor.parametersAttr);
                    // Store it in the specimen descriptor
                    specDesc.tubes.put(wellKey, wellDesc);
                }

            } else {

                // Create a new Specimen or reuse an existing one
                Specimen specDesc;
                String specName = getSpecimenName(processor);
                String specKey = experimentName + "_" + specName;
                if (expDesc.specimens.containsKey(specKey)) {
                    specDesc = expDesc.specimens.get(specKey);
                } else {
                    specDesc = new Specimen(specName);
                    // Store attributes
                    specDesc.addAttributes(getSpecimenAttributes(processor));
                    // Store it in the experiment descriptor
                    expDesc.specimens.put(specKey, specDesc);
                }

                // Create a new Tube descriptor or reuse an existing one
                Tube tubeDesc;
                String tubeName = getTubeOrWellName(processor);
                String tubeKey = specKey + "_" + tubeName;
                if (!specDesc.tubes.containsKey(tubeKey)) {
                    tubeDesc = new Tube(tubeName, file, userRootFolder);
                    // Store attributes
                    tubeDesc.addAttributes(getTubeOrWellAttributes(processor));
                    // Store events and parameter attributes
                    tubeDesc.fcsFile.parameterList = new FCSFileParameterList(
                            processor.numEvents(), processor.numParameters(),
                            processor.parametersAttr);
                    // Store it in the specimen descriptor
                    specDesc.tubes.put(tubeKey, tubeDesc);
                }

            }

        }

    }

    /**
     * Return the experiment name stored in the FCS file.
     *
     * If the FCS file does not contain an experiment name, the name of the
     * folder containing the FCS file is returned as experiment name.
     *
     * @param processor with already scanned file
     * @return name of the experiment
     */
    @Override
    protected String getExperimentName(FCSReader processor) {
        System.err.println(
                "@TODO Investigate whether the FCS files from CytoFLEX S store the experiment name somewhere!");

        // As experiment name, return the name of the containing folder
        File fcsFile = processor.getFile();
        String experimentName = fcsFile.getParentFile().getName();
        return experimentName;
    }

    /**
     * Return the tray (plate) name stored in the FCS file.
     *
     * Before you query for the tray (plate) name, make sure to correctly
     * identifyContainerType().
     *
     * @param processor with already scanned file
     * @return name of the experiment
     */
    @Override
    protected String getTrayName(FCSReader processor) {
        System.err.println(
                "@TODO Investigate whether the FCS files from CytoFLEX S alway name their plates with a number!");
        String trayName = processor.getCustomKeyword("PLTNO");
        if (trayName.isEmpty()) {
            trayName = "UNKNOWN";
        }
        return trayName;
    }

    /**
     * Return the specimen name stored in the FCS file.
     *
     * @TODO Figure out whether the specimen info is stored somewhere!
     *
     * @param processor with already scanned file
     * @return name of the specimen
     */
    @Override
    protected String getSpecimenName(FCSReader processor) {
        System.err.println(
                "@TODO Investigate whether FCS files from CytoFLEX S store the specimen name somewhere!");
        String specimenName = "UNKNOWN";
        return specimenName;
    }

    /**
     * Return the tube name stored in the FCS file
     *
     * @param processor with already scanned file
     * @return name of the tube or well
     */
    @Override
    protected String getTubeOrWellName(FCSReader processor) {
        System.err.println(
                "@TODO Investigate whether the FCS files from CytoFLEX S differentiate between tube and well names!");

        // There are two candidate keywords:

        // 1. $SMNO
        // 2. TBNM (apparently corresponds to the file name)

        // Do we need to differentiate between containers?

        // String name;
        // if (identifyContainerType(processor).equals("TRAY")) {
        // name = processor.getCustomKeyword("$SMNO");
        // } else if (identifyContainerType(processor).equals("SPECIMEN")) {
        // name = processor.getCustomKeyword("$SMNO");
        // } else {
        // String fcsFileName = processor.getFile().getName();
        // name = fcsFileName.substring(0,
        // fcsFileName.toLowerCase().lastIndexOf(".fcs"));
        // }

        String name;
        name = processor.getCustomKeyword("$SMNO");
        if (name.contentEquals("")) {
            name = processor.getCustomKeyword("TBNM");
            if (name.contentEquals("")) {
                String fcsFileName = processor.getFile().getName();
                name = fcsFileName.substring(0,
                        fcsFileName.toLowerCase().lastIndexOf(".fcs"));
            }
        }
        return name;
    }

    /**
     * Returns true if the passed hardware string (from an FCS file) is a
     * recognized hardware string for the BD LSR Fortessa.
     *
     * @param hardwareString Hardware string.
     * @return true if the string is a valid hardware string for the BD LSR
     *         Fortessa, false otherwise.
     */
    public static boolean isValidHardwareString(String hardwareString) {

        return knownHardwareStrings.containsKey(hardwareString);
    }

}