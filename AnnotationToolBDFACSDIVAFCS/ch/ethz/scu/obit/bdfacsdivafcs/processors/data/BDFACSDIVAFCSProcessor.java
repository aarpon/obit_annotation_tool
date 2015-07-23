package ch.ethz.scu.obit.bdfacsdivafcs.processors.data;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.model.SampleDescriptor;
import ch.ethz.scu.obit.bdfacsdivafcs.readers.FCSReader;
import ch.ethz.scu.obit.processors.AbstractProcessor;
import ch.ethz.scu.obit.processors.data.model.DatasetDescriptor;
import ch.ethz.scu.obit.processors.data.model.ExperimentDescriptor;
import ch.ethz.scu.obit.processors.data.model.RootDescriptor;
import ch.ethz.scu.obit.processors.data.validator.GenericValidator;

/**
 * BDFACSDIVAXMLProcessor parses folder structures created by the 
 * "BD BioSciences FACSDiva" software when "exporting as FCS".
 * 
 * 
 * Please notice that DIVA FCS files generated in this mode are different
 * from files accompanying the XML file generated when exporting as an 
 * experiment.
 * 
 * Datasets exported as XML are flagged as invalid and are not processed.
 * 
 * @author Aaron Ponti
 */
public final class BDFACSDIVAFCSProcessor extends AbstractProcessor {

	/* Private instance variables */
	private File userFolder;
	private Experiment currentExperiment;

	/* List of accepted attachment file extensions */
	private final String[] validAttachmentExtensions = 
		{".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"};
	
	/* Map of known hardware strings to supported hardware */
	private static final Map<String, String> knownHardwareStrings;
    static
    {
    	knownHardwareStrings = new HashMap<String, String>();
    	
    	// BD LSR Fortessa
    	knownHardwareStrings.put("LSRII", "BD LSR Fortessa");
    	knownHardwareStrings.put("BD LSR Fortessa SORP (LSRII)", "BD LSR Fortessa");
    	knownHardwareStrings.put("LSRFortessa", "BD LSR Fortessa");
    	
    	// FD FACSAria III
    	knownHardwareStrings.put("FACSAriaIII", "BD FACSAria III");
    }
    
	/* Public instance variables */
	public UserFolder folderDescriptor = null;

	/**
	 * Constructor
	 * @param fullUserFolderName Full path of the user folder containing the exported experiments.
	 */
	public BDFACSDIVAFCSProcessor(String fullUserFolderName) {

		// Instantiate the validator
		validator = new GenericValidator();

		// fullFolderName MUST be a folder! If it is not, there is 
		// a major problem with the software setup!
		File folder = new File(fullUserFolderName);
		if (!folder.isDirectory()) {
			// TODO Proper handling of this case.
			System.err.println("Expected user folder, found file!");
			System.exit(1);
		}

		// Set the root folder
		this.userFolder = folder;

		// Create a descriptor for the user folder
		folderDescriptor = new UserFolder(folder); 

	}

	/**
	 * Return information regarding the file format.
	 * @return descriptive String for the Processor.
	 */
	public String info() {
		return "BD BioSciences FACSDiva\u2122 Software: FCS export";
	}

	/**
	 * Parse the file to extract data and metadata.
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
	 * Return a String representation of the BDFACSDIVAFCSProcessor.
	 * @return String containing a description of the BDFACSDIVAFCSProcessor. 
	 */
	public String toString() {
		return userFolder.getName();
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override
	public String getType() {
		return "BDFACSDIVAFCS";
	}	
	
	/**
	 * Descriptor that represents a the user top folder. 
	 * @author Aaron Ponti
	 */
	public class UserFolder extends RootDescriptor {
		
		public Map<String, Experiment> experiments = 
				new LinkedHashMap<String, Experiment>();
		
		public UserFolder(File fullFolder) {
			
			// Invoke parent constructor
			super(fullFolder);
			
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
	 * @author Aaron Ponti
	 */
	public class Experiment extends ExperimentDescriptor {

		// An Experiment can contain TRAYS that in turn contain SPECIMENs 
		// which contain TUBEs, or directly SPECIMENs containing TUBEs.

		// Experiment description
		public String description = "";
		
		// Experiment tags (comma-separated list)
		public String tags = "";
		
		/**
		 * ArrayList of Tray's
		 */
		public Map<String, Tray> trays = 
				new LinkedHashMap<String, Tray>();

		/**
		 * ArrayList of Specimen's
		 */
		public Map<String, Specimen> specimens = 
				new LinkedHashMap<String, Specimen>();
		
		/**
		 * Constructor
		 * @param fullPath Full path of the experiment.
		 */
		public Experiment(File fullPath) {

			super(fullPath);
			this.setName(fullPath.getName());
			
			// Set the attribute relative path. Since this will be 
			// used by the openBIS dropboxes running on a Unix machine, 
			// we make sure to use forward slashes for path separators 
			// when we set it as an attribute.
			attributes.put("relativePath",
					this.relativePath.replace("\\", "/"));

		}

		/**
		 * Alternative constructor
		 * @param fullPath Full path of the experiment.
		 * @param name Name of the experiment.
		 */
		public Experiment(File fullPath, String name) {

			super(fullPath);
			this.setName(name);
			
			// Set the attribute relative path. Since this will be 
			// used by the openBIS dropboxes running on a Unix machine, 
			// we make sure to use forward slashes for path separators 
			// when we set it as an attribute.
			attributes.put("relativePath",
					this.relativePath.replace("\\", "/"));

		}
		
		/**
		 * Return a simplified class name to use in XML.
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "Experiment";
		}
		
		/**
		 * Add the relative file path of the attachment to the attributes.
		 * @param attachment A file object.
		 * @return true if the attachment could be added; false otherwise.
		 */
		public boolean addAttachment(File attachment) {
			
			// Create attachment string
			String attachmentAttr = "";
			
			// Get current attachments
			if (attributes.containsKey("attachments")) {
				attachmentAttr = attributes.get("attachments");
			}
			
			// Build the attachment string
			if (attachment.getAbsolutePath().startsWith(fullPath.getAbsolutePath())) {
				
				// Check that the attachment is contained in the Experiment
				String filePath = attachment.getAbsolutePath().replace("\\", "/");
				int indx = filePath.lastIndexOf(
						relativePath.replace("\\", "/"));
				if (indx == -1) {
					return false;
				}
					
				// Append the relative attachment path to the semicolon-
				// separated path string
				String relAttachmentPath = filePath.substring(indx); 
				if (attachmentAttr.equals("")) {
					attachmentAttr = relAttachmentPath;
				} else {
					attachmentAttr += ";" + relAttachmentPath; 
				}
					
			}
			
			// Store the attachments as attributes
			attributes.put("attachments", attachmentAttr);
			
			// Return success
			return true;
		}

	} 


	/**
	 * Descriptor representing an FCS file associated to a Tube.
	 * An FCS File is always a child of a Tube.
	 * @author Aaron Ponti
	 */
	public class FCSFile extends DatasetDescriptor {

		/**
		 * Constructor.
		 * @param fcsFileName FCS file name with full path
		 */
		public FCSFile(File fcsFileName) throws IOException {

			// Call base constructor
			super(fcsFileName);

			// Store the file name
			this.setName(fcsFileName.getName());

			// Set the attribute relative file name. Since this will be 
			// used by the openBIS dropboxes running on a Unix machine, 
			// we make sure to use forward slashes for path separators 
			// when we set it as an attribute.
			attributes.put("relativeFileName",
					this.relativePath.replace("\\", "/"));
		}

		/**
		 * Return a String representation of the extracted FCS file.
		 * @return String representation of the FCS file.
		 */
		@Override
		public String toString() {
			return getName();
		}

		/**
		 * Return a simplified class name to use in XML.
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "FCSFile";
		}

	}
	
	/**
	 * Descriptor representing a specimen obtained from the FCS file.
	 * A Specimen can be a child of a Tray or directly of an Experiment.
	 * @author Aaron Ponti
	 */
	public class Specimen extends SampleDescriptor {
	
		/* Public instance variables */
	
		/**
		 * ArrayList of Tube's
		 */			
		public Map<String, Tube> tubes =
				new LinkedHashMap<String, Tube>();
	
		/**
		 * Constructor.
		 * @param name Name of the specimen
		 */
		public Specimen(String name) {
	
			this.setName(name);
	
		}
	
		/**
		 * Return a simplified class name to use in XML.
		 * @return simplidied class name.
		 */
		@Override		
		public String getType() {
			return "Specimen";
		}

	}

	/**
	 * Descriptor representing a tray obtained from the FCS file.
	 * @author Aaron Ponti
	 */
	public class Tray extends SampleDescriptor {

		// Define the supported tray geometries as a static list
		public final List<String> supportedTrayGeometries =
				Arrays.asList("96_WELLS_8X12", "384_WELLS_16x24");
	
		/**
		 * ArrayList of Specimen's
		 */
		public Map<String, Specimen> specimens = 
				new LinkedHashMap<String, Specimen>();
	
		/**
		 * Geometry associated to the plate
		 */
		public String geometry;
		
		/**
		 * Constructor
		 * @param name name of the Tray.
		 */
		public Tray(String name) {
	
			this.setName(name);

			// Initialize geometry
			this.geometry = this.supportedTrayGeometries.get(0);
		}
	
		/**
		 * Return a simplified class name to use in XML.
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "Tray";
		}

	}

	/**
	 * Descriptor representing a tube obtained from the FCS file.
	 * A Tube is always a child of a Specimen.
     *
	 * @author Aaron Ponti
	 */
	public class Tube extends SampleDescriptor {
		
		public FCSFile fcsFile;
		
		/**
		 * Constructor.
		 * @param name Name of the Tube.
		 * @param fcsFullFileName Full file name of the FCS file associated
		 * with the Tube.
		 * @throws IOException 
		 */
		public Tube(String name, File fcsFullFileName) 
				throws IOException {
	
			this.setName(name);

			// Associate the FCS file to the Tube
			fcsFile = new FCSFile(fcsFullFileName);

		}
	
		/**
		 * Return a String representation of the extracted Tube node.
		 * @return String representation of the Tube node.
		 */
		@Override
		public String toString() {
			return getName();
		}
	
		/**
		 * Return a simplified class name to use in XML.
		 * @return simplified class name.
		 */
		@Override
		public String getType() {
			return "Tube";
		}

	}

	/**
	 * Descriptor representing a well obtained from the FCS file.
	 * A Well is always a child of a Specimen.
     *
	 * @author Aaron Ponti
	 */
	public class Well extends Tube {
		
		/**
		 * Constructor.
		 * @param name Name of the Well.
		 * @param fcsFullFileName Full file name of the FCS file associated
		 * with the Well.
		 * @throws IOException 
		 */
		public Well(String name, File fcsFullFileName) 
				throws IOException {
			
			// Call base constructor
			super(name, fcsFullFileName);

		}
	
		/**
		 * Return a simplified class name to use in XML.
		 * @return simplified class name.
		 */
		@Override
		public String getType() {
			return "Well";
		}

	}

	/**
	 * Checks whether the passed file can be attached.
	 * @param file File to be checked.
	 * @return always false! The classes that inherit from AbstractProcessor 
	 * (and support attachments) should override this method. 
	 */
	protected boolean isValidAttachment(File file) {

		// Extract the file extension (lower case)
		String fileName = file.getName();
		int indx = fileName.lastIndexOf(".");
		if (indx == -1) {
			return false;
		}
		String ext = fileName.substring(indx).toLowerCase();

		// Check whether the file is a valid attachment
		return Arrays.asList(validAttachmentExtensions).contains(ext);

	}

	/**
	 * Scan the folder recursively and process all fcs files found
	 * @param dir Full path to the directory to scan
	 * @throws IOException Thrown if a FCS file could not be processed
	 */
	private void recursiveDir(File dir) throws IOException {
	
		// To make things simple and robust, we make sure that the first
		// thing we process at any sub-folder level is an FCS file.
		String [] files = getSimplySortedList(dir);
		
		// Empty subfolders are not accepted
		if (files.length == 0 && !dir.equals(this.userFolder)) {
			validator.isValid = false;
			validator.invalidFilesOrFolders.put(
					dir, "Empty folder");
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
				validator.invalidFilesOrFolders.put(
						file, "Files must be in sub-folders.");
				return;
			}
			
			// Delete some known garbage
			if (deleteIfKnownUselessFile(file)) {
				continue;
			}

			// The DIVA software can export FCS files in two modes: FCS export
			// creates valid FCS 3.0-compliant files. Experiment export creates 
			// files that cannot be used in subsequent analysis in third-party
			// software like FlowJo. In case of Experiment exports, an XML file
			// is saved along with the series of FCS files. We use the presence
			// of the XML file to discriminate between the two export modes.
			String fileName = file.getName();
			int indx = fileName.lastIndexOf(".");
			if (indx == -1) {
				continue;
			}
			String ext = fileName.substring(indx);
			if (ext.equalsIgnoreCase(".xml")) {
				validator.isValid = false;
				validator.invalidFilesOrFolders.put(
						file, "Experiment export");
				continue;
			}

			// Check whether the file is a valid attachment
			if (isValidAttachment(file)) {

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
				if (! currentExperiment.addAttachment(file)) {
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
				validator.invalidFilesOrFolders.put(
						file, "Failed registration to openBIS!");
				return;
			}

			// Check whether an experiment is already annotated. Please
			// mind that at this stage we do not know WHICH experiment
			// was annotated. We just react to the fact that at least
			// one has been annotated, somewhere.
			if (fileName.contains("_properties.oix")) {
				validator.isValid = false;
				validator.invalidFilesOrFolders.put(
						file, "Experiment already annotated");
				return;				
			}

			// Do we have an unknown file? If we do, we move on to the next.
			if (! ext.equalsIgnoreCase(".fcs")) {
				validator.isValid = false;
				validator.invalidFilesOrFolders.put(
						file, "Unsupported file format");
				continue;
			}
	
			// Is it an FCS file? Scan it and extract the information
			FCSReader processor = new FCSReader(file, false);
			if (!processor.parse()) {
				System.err.println("File " + file.getCanonicalPath() + 
						" could not be parsed!");
				validator.isValid = false;
				validator.invalidFilesOrFolders.put(
						file, "Parsing failed");
				continue;
			}

			// Create a new ExperimentDescriptor or reuse an existing one
			Experiment expDesc = null;
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
				expDesc = folderDescriptor.experiments.get(experimentPath);
			} else {
				expDesc = new Experiment(new File(experimentPath),
						experimentName);
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
					trayDesc = 
							new Tray(trayName);
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
					specDesc = 
							new Specimen(specName);
					// Store attributes
					specDesc.addAttributes(getSpecimenAttributes(processor));
					// Store it in the tray descriptor
					trayDesc.specimens.put(specKey, specDesc);
				}
	
				// Create a new Well descriptor or reuse an existing one
				Well wellDesc;
				String wellName = getTubeOrWellName(processor);
				String wellKey = specKey + "_" + wellName;
				if (! specDesc.tubes.containsKey(wellKey)) {
					wellDesc = new Well(wellName, file);
					// Store attributes
					wellDesc.addAttributes(getTubeOrWellAttributes(processor));
					// Store parameter attributes
					wellDesc.fcsFile.addAttributes(processor.parametersAttr);
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
					specDesc = 
							new Specimen(specName);	
					// Store attributes
					specDesc.addAttributes(getSpecimenAttributes(processor));
					// Store it in the experiment descriptor
					expDesc.specimens.put(specKey, specDesc);
				}
	
				// Create a new Tube descriptor or reuse an existing one
				Tube tubeDesc;
				String tubeName = getTubeOrWellName(processor);
				String tubeKey = specKey + "_" + tubeName;
				if (! specDesc.tubes.containsKey(tubeKey)) {
					tubeDesc = new Tube(tubeName, file);	
					// Store attributes
					tubeDesc.addAttributes(getTubeOrWellAttributes(processor));
					// Store parameter attributes
					tubeDesc.fcsFile.addAttributes(processor.parametersAttr);
					// Store it in the specimen descriptor
					specDesc.tubes.put(tubeKey, tubeDesc);
				}
	
			}
	
		}

	}

	/**
	 * Make sure that the first entry in the file list is an FCS file,
	 * if there is at least one. 
	 * @param dir	Current directory
	 * @param files Array of file (and folder) names at current level
	 * @return String[] moderately sorted files.
	 */
	private String[] getSimplySortedList(File dir) {
		
		// Go over the list, the first FCS file we find we put it in front of
		// the list and return.
		String [] files = dir.list();
		int foundIndx = -1;
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i];
			int indx = fileName.lastIndexOf(".");
			if (indx == -1) {
				continue;
			}
			String ext = fileName.substring(indx).toLowerCase();
			if (ext.equals(".fcs")) {
				foundIndx = i;
				break;
			}
		}
		// Swap the first entry with this one.
		if (foundIndx > 0) {
			String tmp = files[foundIndx];
			files[foundIndx] = files[0];
			files[0] = tmp;
		}
		return files;
	}

	/**
	 * Return the tube name stored in the FCS file
	 * @param processor with already scanned file
	 * @return name of the tube
	 */
	private String getTubeOrWellName(FCSReader processor) {
		
		// We discriminate here since there is a formatting
		// difference in the value stored in the "TUBE NAME"
		// keyword (which is always found in the file, no 
		// matter whether the container is a Specimen or a
		// Tray) and the one stored in the "WELL ID" (which 
		// is found only in Trays). A "TUBE NAME" value like 
		// A1 becomes a WELL ID like A01.
		String name;
		if (identifyContainerType(processor).equals("TRAY")) {
			name = processor.getCustomKeyword("WELL ID");
		} else {
			name = processor.getCustomKeyword("TUBE NAME");
		}
		return name;
	}

	/**
	 * Return true if the experiment was an indexed sort
	 * @param processor with already scanned file
	 * @return true if the experiment was an indexed sort, false 
	 * otherwise 
	 */
	private boolean isIndexSort(FCSReader processor) {
		String indexSortLocationCount = processor.getCustomKeyword(
				"INDEX SORTING SORTED LOCATION COUNT");
		return (!indexSortLocationCount.isEmpty());
	}

	/**
	 * Return the experiment path the folder name matches the experiment
	 * name stored in the FCS file
	 * @param processor with already scanned file
	 * @param fcsFilePath full file path of the FCS file
	 * @return experiment path
	 */
	private String getExperimentPath(FCSReader processor, 
			File fcsFilePath) {

		String experimentName = getExperimentName(processor);
		String experimentPath = "";
		while (fcsFilePath != null) {
			
			String expNameFromPath = fcsFilePath.getName();
			
			if (experimentName.equals(expNameFromPath)) {
				try {
					experimentPath = fcsFilePath.getCanonicalPath();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fcsFilePath = null;
			} else {
				fcsFilePath = fcsFilePath.getParentFile();
			}

		}
		
		return experimentPath;
	}
	
	/**
	 * Return the experiment name stored in the FCS file
	 * @param processor with already scanned file
	 * @return name of the experiment
	 */
	private String getExperimentName(FCSReader processor) {
		String experimentName = processor.getCustomKeyword("EXPERIMENT NAME");
		if (experimentName.isEmpty()) {
			experimentName = "UNKNOWN";
		}
		return experimentName;
	}

	/**
	 * Return the specimen name stored in the FCS file.
	 * 
	 * Before you query for the specimen name, make sure to
	 * correctly identifyContainerType().
	 * @param processor with already scanned file
	 * @return name of the specimen
	 */
	private String getSpecimenName(FCSReader processor) {
		String specimenName = processor.getStandardKeyword("$SRC");
		if (specimenName.isEmpty()) {
			specimenName = "UNKNOWN";
		}
		return specimenName;
	}

	/**
	 * Return the tray (plate) name stored in the FCS file.
	 * 
	 * Before you query for the tray (plate) name, make sure to
	 * correctly identifyContainerType().
	 * @param processor with already scanned file
	 * @return name of the experiment
	 */
	private String getTrayName(FCSReader processor) {
		String trayName = processor.getCustomKeyword("PLATE NAME");
		if (trayName.isEmpty()) {
			trayName = "UNKNOWN";
		}
		return trayName;
	}
	
	/**
	 * Identifies the container type of the file (Specimen or Plate)
	 * @param processor FCSProcessor with already scanned file
	 * @return one of "SPECIMEN" or "TRAY"
	 */
	private String identifyContainerType(FCSReader processor) {

		if (processor.getCustomKeyword("PLATE NAME").isEmpty()) {
			return "SPECIMEN";
		} else {
			return "TRAY";
		}
	}

	/**
	 * Extract and store the Experiment attributes
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	private Map<String, String> getExperimentAttributes(FCSReader processor) {
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
			validator.invalidFilesOrFolders.put(
					processor.getFile(),
					"Wrong hardware string: " + acqHardwareString);
		}
		attributes.put("acq_hardware", acqHardwareString);

		// Software string
		String acqSoftwareString = processor.getCustomKeyword("CREATOR");
		if (!acqSoftwareString.contains("BD FACSDiva Software")) {
			validator.isValid = false;
			validator.invalidFilesOrFolders.put(
					processor.getFile(),
					"Wrong software string: " + acqSoftwareString);
		} else {
			// Check major and minor version (we ignore the patch)
			Pattern p = Pattern.compile(
					"(.*?)(\\d{1,2})\\.(\\d{1,2})(\\.\\d{1,2})?");
			Matcher m = p.matcher(acqSoftwareString);
			if (!m.matches()) {
				validator.isValid = false;
				validator.invalidFilesOrFolders.put(
						processor.getFile(),
						"Unknown software version.");
			} else {
				int major;
				int minor;
				try {
					major = Integer.parseInt(m.group(2));
					minor = Integer.parseInt(m.group(3));
					// Known valid versions are 6.1 and 7.0
					if (!((major == 6 && minor == 1) ||
							(major == 7 && minor == 0) || 
							(major == 8 && minor == 0))) {
						validator.isValid = false;
						validator.invalidFilesOrFolders.put(
								processor.getFile(),
								"Unsupported software version: " + 
								m.group(2) + "." +
								m.group(3));
					}
				} catch (NumberFormatException n) {
					validator.isValid = false;
					validator.invalidFilesOrFolders.put(
							processor.getFile(),
							"Unknown software version.");
				}
			}
		}
		
		// Acquisition software
		attributes.put("acq_software", acqSoftwareString);
		
		// Acquisition date
		attributes.put("date", processor.getStandardKeyword("$DATE"));
		
		return attributes;
	}

	/**
	 * Extract and store the Tray attributes
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	private Map<String, String> getTrayAttributes(FCSReader processor) {
        // Nothing
		return new HashMap<String, String>();
	}
	
	/**
	 * Extract and store the Specimen attributes
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	private Map<String, String> getSpecimenAttributes(FCSReader processor) {
        // Nothing
		return new HashMap<String, String>();
	}
	
	/**
	 * Extract and store the Tube attributes
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	private Map<String, String> getTubeOrWellAttributes(FCSReader processor) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("dataFilename", processor.getStandardKeyword("$FIL"));
		attributes.put("indexSort", isIndexSort(processor) ? "true" : "false");
		return attributes;
	}

}