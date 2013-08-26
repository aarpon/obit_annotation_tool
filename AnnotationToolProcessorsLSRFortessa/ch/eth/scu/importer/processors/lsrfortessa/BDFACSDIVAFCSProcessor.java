package ch.eth.scu.importer.processors.lsrfortessa;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.eth.scu.importer.processors.AbstractProcessor;
import ch.eth.scu.importer.processors.lsrfortessa.model.ExperimentDescriptor;
import ch.eth.scu.importer.processors.lsrfortessa.model.SampleDescriptor;
import ch.eth.scu.importer.processors.model.DatasetDescriptor;
import ch.eth.scu.importer.processors.model.PathAwareDescriptor;
import ch.eth.scu.importer.processors.validator.GenericValidator;

/**
 * BDFACSDIVAXMLProcessor parses folder structures created by the 
 * "BD BioSciences FACSDiva" software when "exporting as FCS".
 * 
 * Please notice that DIVA FCS files generated in this mode are different
 * from files accompanying the XML file generated when exporting as an 
 * experiment.
 * 
 * Datasets exported as XML are flagged as invalid and are not processed.
 * 
 * @author Aaron Ponti
 */
public class BDFACSDIVAFCSProcessor extends AbstractProcessor {

	/* Private instance variables */
	private File userFolder;

	/* Private instance variables */
	private int numberOfValidFiles = 0;
	
	/* Public instance variables */
	public Folder folderDescriptor = null;

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

		// Create a folderDescriptor
		folderDescriptor = new Folder(folder); 

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
	 * Descriptor that represents a folder containing a dataset. 
	 * @author Aaron Ponti
	 */
	public class Folder extends PathAwareDescriptor {
		
		public Map<String, Experiment> experiments = 
				new LinkedHashMap<String, Experiment>();
		
		public Folder(File fullFolder) {
			
			// Invoke parent constructor
			super(fullFolder);
			
			// Set the descriptor name
			this.name = fullFolder.getName();

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
		 * @param name Name of the experiment.
		 */
		public Experiment(File fullPath) {

			super(fullPath);
			this.name = fullPath.getName();

		}

		/**
		 * Return a simplified class name to use in XML.
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "Experiment";
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
			this.name = fcsFileName.getName();

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
			return this.name;
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
	
			this.name = name;
	
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
	
			this.name = name;

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
	
			this.name = name;

			// Associate the FCS file to the Tube
			fcsFile = new FCSFile(fcsFullFileName);

		}
	
		/**
		 * Return a String representation of the extracted Tube node.
		 * @return String representation of the Tube node.
		 */
		@Override
		public String toString() {
			return this.name;
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
	 * Scan the folder recursively and process all fcs files found
	 * @param dir Full path to the directory to scan
	 * @throws IOException Thrown if a FCS file could not be processed
	 */
	private void recursiveDir(File dir) throws IOException {
	
		// Get the directory listing
		String [] files = dir.list();

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
			
			// Delete some known garbage
			if (deleteIfKnownUselessFile(file)) {
				continue;
			}

			// We ignore any file that is not an fcs file - but we pay 
			// attention to the existence of XML files that indicate an
			// Experiment export!
			//
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
				validator.isAnnotated = false;
				validator.errorMessages.add("Experiment export");
				return;
			}

			// Do we have an unknown file? If we do, we move on to the next.
			if (! ext.equalsIgnoreCase(".fcs")) {
				continue;
			}
			
			// Is it an FCS file? Scan it and extract the information
			FCSProcessor processor = 
					new FCSProcessor(file.getCanonicalPath(), false);
			if (!processor.parse()) {
				System.err.println("File " + file.getCanonicalPath() + 
						" could not be parsed! It will be skipped.");
				continue;
			} else {
				// Add one to the number of valid files found
				numberOfValidFiles++;
			}

			// Create a new ExperimentDescriptor or reuse an existing one
			Experiment expDesc;
			String experimentName = getExperimentName(processor);
			File experimentPath = getExperimentPath(processor, file);
			String experimentKey = experimentPath.getCanonicalPath();
			if (folderDescriptor.experiments.containsKey(experimentKey)) {
				expDesc = folderDescriptor.experiments.get(experimentKey);
			} else {
				expDesc = 
						new Experiment(experimentPath);
				// Store attributes
				expDesc.setAttributes(getExperimentAttributes(processor));
				folderDescriptor.experiments.put(experimentKey, expDesc);
			}
	
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
					trayDesc.setAttributes(getTrayAttributes(processor));
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
					specDesc.setAttributes(getSpecimenAttributes(processor));
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
					wellDesc.setAttributes(getTubeAttributes(processor));
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
					specDesc.setAttributes(getSpecimenAttributes(processor));
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
					tubeDesc.setAttributes(getTubeAttributes(processor));
					// Store it in the specimen descriptor
					specDesc.tubes.put(tubeKey, tubeDesc);
				}
	
			}
	
		}

		// At this point, the dataset is for sure not annotated, but it
		// could still be invalid (if no files were found)
		this.validator.isValid = (numberOfValidFiles > 0);
		this.validator.isAnnotated = false;
		if (numberOfValidFiles == 0) {
			this.validator.errorMessages.add("No FCS files found.");
		}

	}

	/**
	 * Return the tube name stored in the FCS file
	 * @param processor with already scanned file
	 * @return name of the tube
	 */
	private String getTubeOrWellName(FCSProcessor processor) {
		
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
	 * Return the experiment name stored in the FCS file
	 * @param processor with already scanned file
	 * @return name of the experiment
	 */
	private File getExperimentPath(FCSProcessor processor, 
			File fcsFilePath) {
		String fcsFilePathStr = "";
		try {
			fcsFilePathStr = fcsFilePath.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String experimentName = getExperimentName(processor);
		int lastIndex = fcsFilePathStr.indexOf(experimentName);
		if (lastIndex == -1) {
			return new File("");
		} else {
			return new File(fcsFilePathStr.substring(0, 
					lastIndex + experimentName.length()));
		}
	}
	
	/**
	 * Return the experiment name stored in the FCS file
	 * @param processor with already scanned file
	 * @return name of the experiment
	 */
	private String getExperimentName(FCSProcessor processor) {
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
	private String getSpecimenName(FCSProcessor processor) {
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
	private String getTrayName(FCSProcessor processor) {
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
	private String identifyContainerType(FCSProcessor processor) {

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
	private Map<String, String> getExperimentAttributes(FCSProcessor processor) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("owner_name", processor.getStandardKeyword("$OP"));
		String acqHardwareString = processor.getStandardKeyword("$CYT");
		if (acqHardwareString.equals("LSRII")) {
			// LSRII is generic. We replace it here with "BD LSR Fortessa"
			acqHardwareString = "BD LSR Fortessa";
		} else {
			validator.isValid = false;
			validator.errorMessages.add(
					"Wrong hardware string: " + acqHardwareString);
		}
		attributes.put("acq_hardware", acqHardwareString);
		String acqSoftwareString = processor.getCustomKeyword("CREATOR");
		if (!acqSoftwareString.contains("BD FACSDiva Software")) {
			validator.isValid = false;
			validator.errorMessages.add(
					"Wrong software string: " + acqHardwareString);
		} else {
			// Check major and minor version (we ignore the patch)
			Pattern p = Pattern.compile(
					"(.*?)(\\d{1,2})\\.(\\d{1,2})(\\.\\d{1,2})?");
			Matcher m = p.matcher(acqSoftwareString);
			if (!m.matches()) {
				validator.isValid = false;
				validator.errorMessages.add("Unknown software version.");
			} else {
				if (!m.group(2).equals("6") || !m.group(3).equals("1")) {
					validator.isValid = false;
					validator.errorMessages.add(
					"Unsupported software version: " + m.group(2) + "." +
							 m.group(3));
				}
			}
		}
		attributes.put("acq_software", acqSoftwareString);
		attributes.put("date", processor.getStandardKeyword("$DATE"));
		return attributes;
	}

	/**
	 * Extract and store the Tray attributes
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	private Map<String, String> getTrayAttributes(FCSProcessor processor) {
        // Nothing
		return new HashMap<String, String>();
	}
	
	/**
	 * Extract and store the Specimen attributes
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	private Map<String, String> getSpecimenAttributes(FCSProcessor processor) {
        // Nothing
		return new HashMap<String, String>();
	}
	
	/**
	 * Extract and store the Tube attributes
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	private Map<String, String> getTubeAttributes(FCSProcessor processor) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("dataFilename", processor.getStandardKeyword("$FIL"));
		return attributes;
	}
}