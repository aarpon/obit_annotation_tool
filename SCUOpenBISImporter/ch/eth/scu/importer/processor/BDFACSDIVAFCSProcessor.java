package ch.eth.scu.importer.processor;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.processor.model.AbstractDescriptor;
import ch.eth.scu.importer.processor.model.FirstLevelDescriptor;

import java.io.*;
import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * BDFACSDIVAXMLProcessor parses folder structures created by the 
 * "BD BioSciences FACSDiva" software when "exporting as FCS".
 * 
 * Please notice that DIVA FCS files generated in this mode are different
 * from files accompanying the XML file generated when exporting as an 
 * experiment.
 * @author Aaron Ponti
 */
public class BDFACSDIVAFCSProcessor extends AbstractProcessor {

	/* Private instance variables */
	protected DefaultMutableTreeNode rootNode;
	private File topFolder;
	private boolean isValid = false;
	private boolean cleanFCSExport = true;

	/* Protected instance variables */
	protected File incomingDir;

	/* Public instance variables */
	public FolderDescriptor folderDescriptor = null;

	/**
	 * Constructor
	 * @param fullFolderName Full path of the folder containing the exported experiment.
	 */
	public BDFACSDIVAFCSProcessor(String fullFolderName, String userName) {

		// Make sure rootFolderName is a valid directory
		File folder = new File(fullFolderName);
		if (!folder.isDirectory()) {
			System.err.println("Expected directory name.");
			return;
		}
		
		// Store the incoming dir (to build relative paths)
		Properties appProperties = AppProperties.readPropertiesFromFile();
		this.incomingDir = new File( 
				appProperties.getProperty("DatamoverIncomingDir") +
				File.separator + userName);

		// Set the root folder
		this.topFolder = folder;

		// Create a RootDescriptor
		folderDescriptor = new FolderDescriptor(folder); 

	}

	/**
	 * Returns true if the dataset is from an "FCS export", false if it is an
	 * "Experiment export".
	 * 
	 * The DIVA software can export FCS files in two modes: FCS export creates
	 * valid FCS 3.0-compliant files. Experiment export creates files that 
	 * cannot be used in subsequent analysis in third-party software like 
	 * FlowJo. In case of Experiment exports, an XML file is saved along with
	 * the series of FCS files. We use the presence of the XML file to 
	 * discriminate between the two export modes.
	 * @return true for an FCS export, false for and Experiment export
	 */
	public boolean isCleanFCSExport() {
		return cleanFCSExport;
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

		// Scan the root folder recursively to reconstruct the experiment
		// structure.
		try {
			recursiveDir(this.topFolder);
		} catch (IOException e) {
			System.err.println("Could not parse the folder.");
			this.isValid = false;
			return this.isValid;
		}
		
		// Success
		this.isValid = true;
		return this.isValid;
		
	}

	/**
	 * Scan the folder recursively and process all fcs files found
	 * @param file Full path to the directory to scan
	 * @throws IOException Thrown if a FCS file could not be processed
	 */
	private void recursiveDir(File dir) throws IOException {

		// Get the directory listing
		String [] files = dir.list();

		// Go over the files and folders
		for (String f : files) {
			
			File file = new File(dir + File.separator + f);

			// Is it a directory? Recurse into it
			if (file.isDirectory() == true) {
				
				// Recurse into the subfolder
				recursiveDir(file);
				
				// Move on to the next file
				continue;
			}
			
			// We ignore any file that is not an fcs file - but we pay 
			// attention to the existance of XML files that indicate an
			// Experiment export!
			String fileName = file.getName();
			int indx = fileName.lastIndexOf(".");
			if (indx == -1) {
				continue;
			}
			String ext = fileName.substring(indx);
			if (ext.equalsIgnoreCase(".xml")) {
				cleanFCSExport = false;
				continue;
			}
			if (! ext.equalsIgnoreCase(".fcs")) {
				continue;
			}
			
			// Is it a file? Scan it and extract the information
			FCSProcessor processor = 
					new FCSProcessor(file.getCanonicalPath(), false);
			if (processor.parse() == false) {
				System.err.println("File " + file.getCanonicalPath() + 
						" could not be parsed! It will be skipped.");
				continue;
			}
		
			// Create a new ExperimentDescriptor or reuse an existing one
			ExperimentDescriptor expDesc;
			String experimentName = getExperimentName(processor);
			if (folderDescriptor.experiments.containsKey(experimentName) ) {
				expDesc = folderDescriptor.experiments.get(experimentName);
			} else {
				expDesc = 
						new ExperimentDescriptor(getExperimentName(processor));
				// Store attributes
				expDesc.setAttributes(getExperimentAttributes(processor));
				folderDescriptor.experiments.put(experimentName, expDesc);
			}
			
			// Is the container a Tray or Specimen?
			if (identifyContainerType(processor).equals("TRAY")) {
				
				// Create a new TrayDescriptor or reuse an existing one
				TrayDescriptor trayDesc;
				String trayName = getTrayName(processor);
				String trayKey = experimentName + "_" + trayName;
				if (expDesc.trays.containsKey(trayKey) ) {
					trayDesc = expDesc.trays.get(trayKey);
				} else {
					trayDesc = 
							new TrayDescriptor(trayName);
					// Store attributes
					trayDesc.setAttributes(getTrayAttributes(processor));
					// Store it in the experiment descriptor
					expDesc.trays.put(trayKey, trayDesc);
				}
				
				// Create a new Specimen or reuse an existing one
				SpecimenDescriptor specDesc;
				String specName = getSpecimenName(processor);
				String specKey = trayKey + "_" + specName;
				if (trayDesc.specimens.containsKey(specKey) ) {
					specDesc = trayDesc.specimens.get(specKey);
				} else {
					specDesc = 
							new SpecimenDescriptor(specName);
					// Store attributes
					specDesc.setAttributes(getSpecimenAttributes(processor));
					// Store it in the tray descriptor
					trayDesc.specimens.put(specKey, specDesc);
				}
				
				// Create a new Tube descriptor or reuse an existing one
				TubeDescriptor tubeDesc;
				String tubeName = getTubeName(processor);
				String tubeKey = specKey + "_" + tubeName;
				if (! specDesc.tubes.containsKey(tubeKey) ) {
					tubeDesc = new TubeDescriptor(tubeName,
							file.getCanonicalPath());
					// Store attributes
					tubeDesc.setAttributes(getTubeAttributes(processor));
					// Store it in the specimen descriptor
					specDesc.tubes.put(tubeKey, tubeDesc);
				}
				
			} else {
				
				// Create a new Specimen or reuse an existing one
				SpecimenDescriptor specDesc;
				String specName = getSpecimenName(processor);
				String specKey = experimentName + "_" + specName;
				if (expDesc.specimens.containsKey(specKey) ) {
					specDesc = expDesc.specimens.get(specKey);
				} else {
					specDesc = 
							new SpecimenDescriptor(specName);	
					// Store attributes
					specDesc.setAttributes(getSpecimenAttributes(processor));
					// Store it in the experiment descriptor
					expDesc.specimens.put(specKey, specDesc);
				}
				
				// Create a new Tube descriptor or reuse an existing one
				TubeDescriptor tubeDesc;
				String tubeName = getTubeName(processor);
				String tubeKey = specKey + "_" + tubeName;
				if (! specDesc.tubes.containsKey(tubeKey) ) {
					tubeDesc = new TubeDescriptor(tubeName,
							file.getCanonicalPath());	
					// Store attributes
					tubeDesc.setAttributes(getTubeAttributes(processor));
					// Store it in the specimen descriptor
					specDesc.tubes.put(tubeKey, tubeDesc);
				}
				
			}
			
		}

	}
	
	/**
	 * Return a String representation of the BDFACSDIVAFCSProcessor.
	 * @return String containing a description of the BDFACSDIVAFCSProcessor. 
	 */
	public String toString() {
		return topFolder.getName();
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
	 * Return a textual tree representation of the BDFACSDIVAFCSProcessor 
	 * @return String containing a tree representation of the 
	 * BDFACSDIVAFCSProcessor
	 */	
	public String treeView() {
		String str = "[Folder] " + toString()+ ".\n|\n";        

		for (String expKey : folderDescriptor.experiments.keySet()) {

			ExperimentDescriptor e = folderDescriptor.experiments.get(expKey);
			
			str +=  "[ Experiment ], name: " + e.getName() + " (" +
					e.attributesToString() + ").\n";

			for (String trayKey : e.trays.keySet()) {

				TrayDescriptor t = e.trays.get(trayKey);
				
				str +=  "|__[ Tray ], name: " + t.getName() + " (" +
						t.attributesToString() + ").\n";

				for (String specimenKey : t.specimens.keySet()) {

					SpecimenDescriptor s = t.specimens.get(specimenKey);
					
					str +=  "|____[ Specimen ], name: " + s.getName() + 
							" (" +	s.attributesToString() + ").\n";

					for (String tubeKey : s.tubes.keySet()) {

						TubeDescriptor tb =  s.tubes.get(tubeKey);
						
						str +=  "|______[ Tube ], name: " + tb.getName() +
								" (" + tb.attributesToString() + ").\n";
						
						str +=  "|________[ File ], name: " + 
								tb.fcsFile.getName() + "\n";
					}
				}

			}

			for (String specimenKey : e.specimens.keySet()) {

				SpecimenDescriptor s = e.specimens.get(specimenKey);
				
				str +=  "|__[ Specimen ], name: " + s.getName() + "(" +
						s.attributesToString() + ").\n";

				for (String tubeKey : s.tubes.keySet()) {

					TubeDescriptor tb = s.tubes.get(tubeKey);
					
					str +=  "|____[ Tube ], name: " + tb.getName() + " (" +
							tb.attributesToString() + ").\n";
					
					str +=  "|________[ File ], name: " + 
							tb.fcsFile.getName() + "\n";

				}
			}

		}
		
		return str;
	}

	/**
	 * Descriptor that represents a folder containing a dataset. 
	 * @author Aaron Ponti
	 */
	public class FolderDescriptor extends FirstLevelDescriptor {
		
		public Map<String, ExperimentDescriptor> experiments = 
				new LinkedHashMap<String, ExperimentDescriptor>();
		
		public FolderDescriptor(File fullFolder) {
			
			// Set the descriptor name
			this.name = fullFolder.getName();

			// Store the relative path
			storeRelativePath(fullFolder);

		}
		
		@Override
		public String getType() {
			return "Folder";
		}

		/**
		 * Return the folder name containing the processed dataset relative 
		 * to the Datamover incoming folder.
		 */
		@Override
		public String getRelativePath() {
			return relativePath;
		}

		/**
		 * Computes and store the folder path relative to the Datamover 
		 * incoming folder.
		 * @param fullFolder Folder with full path.
		 */
		private void storeRelativePath(File fullFolder) {
			String incoming = "";
			try {
				incoming = incomingDir.getCanonicalPath();
			} catch (IOException e) {
				System.err.println("Error with incoming folder path " +
						"("+ incomingDir + ")");
				System.exit(1);
			}
			
			// Extract the path relative to the incoming dir
			String fullFolderStr = "";
			try {
				fullFolderStr = fullFolder.getCanonicalPath();
			} catch (IOException e) {
				System.err.println("Error with dataset folder path " +
						"("+ fullFolder.toString() + ")");
				System.exit(1);
			}
			this.relativePath = fullFolderStr.substring(incoming.length());
			if (this.relativePath.startsWith(File.separator)) {
				this.relativePath = this.relativePath.substring(1);
			}
		}
	}
	
	/**
	 * Descriptor representing an experiment obtained from the FCS file.
     * TODO Add the attributes!
	 * @author Aaron Ponti
	 */
	public class ExperimentDescriptor extends AbstractDescriptor {

		// An Experiment can contain TRAYS that in turn contain SPECIMENs 
		// which contain TUBEs, or directly SPECIMENs containing TUBEs.

		/**
		 * ArrayList of Tray's
		 */
		public Map<String, TrayDescriptor> trays = 
				new LinkedHashMap<String, TrayDescriptor>();

		/**
		 * ArrayList of Specimen's
		 */
		public Map<String, SpecimenDescriptor> specimens = 
				new LinkedHashMap<String, SpecimenDescriptor>();

		/**
		 * Constructor
		 * @param name Name of the experiment.
		 */
		public ExperimentDescriptor(String name) {
			
			this.name = name;

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
	public class FCSFileDescriptor extends AbstractDescriptor {

		private String fullFileName = "";
		private String relativeFileName = "";

		/**
		 * Constructor.
		 * @param fcsFileName FCS file name with full path
		 */
		public FCSFileDescriptor(String fcsFileName) throws IOException {

			// Store the file name
			this.fullFileName = fcsFileName;
			this.name = (new File(fcsFileName)).getName();

			// Store the relative file name (to the incoming dir)
			storeRelativePath(fcsFileName);
			
			// Set the attribute relative file name
			attributes.put("relativeFileName", this.relativeFileName);
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
			return "FCSFile";
		}

		/**
		 * Return the full FCS file name.
		 * @return full FCS file name.
		 */
		public String getFileName() {
			return this.fullFileName;
		}

		
		/**
		 * Return the file name with path relative to the global incoming dir
		 * @return relative file name
		 */
		public String getRelativePathName() {
			return relativeFileName;
		}
		
		private void storeRelativePath(String fcsFileName) {
			String incoming = "";
			try {
				incoming = incomingDir.getCanonicalPath();
			} catch (IOException e) {
				System.err.println("Error with incoming folder path " +
						"("+ incomingDir + ")");
			}
			
			// Return the FCS path relative to the incoming dir
			assert (incoming.length() <= fcsFileName.length()); 
			
			this.relativeFileName = 
					fcsFileName.substring(incoming.length());
			if (this.relativeFileName.startsWith(File.separator)) {
				this.relativeFileName = this.relativeFileName.substring(1);
			}
		}
	}
	
	/**
	 * Descriptor representing a specimen obtained from the FCS file.
	 * A Specimen can be a child of a Tray or directly of an Experiment.

	 * TODO Add the attributes!	
	 * @author Aaron Ponti
	 */
	public class SpecimenDescriptor extends AbstractDescriptor {
	
		/* Public instance variables */
	
		/**
		 * ArrayList of Tube's
		 */			
		public Map<String, TubeDescriptor> tubes =
				new LinkedHashMap<String, TubeDescriptor>();
	
		/**
		 * Constructor.
		 * @param name Name of the specimen
		 */
		public SpecimenDescriptor(String name) {
	
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
	public class TrayDescriptor extends AbstractDescriptor {
	
		/* Public instance variables */
	
		/**
		 * ArrayList of Specimen's
		 */
		public Map<String, SpecimenDescriptor> specimens = 
				new LinkedHashMap<String, SpecimenDescriptor>();
	
		/**
		 * Constructor
		 * @param name name of the Tray.
		 */
		public TrayDescriptor(String name) {
	
			this.name = name;

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
	 * TODO Add the attributes!
	 * @author Aaron Ponti
	 */
	public class TubeDescriptor extends AbstractDescriptor {
		
		public FCSFileDescriptor fcsFile;
		
		/**
		 * Constructor.
		 * @param name Name of the Tube.
		 * @param fcsFullFileName Full file name of the FCS file associated
		 * with the Tube.
		 * @throws IOException 
		 */
		public TubeDescriptor(String name, String fcsFullFileName) 
				throws IOException {
	
			this.name = name;

			// Associate the FCS file to the Tube
			fcsFile = new FCSFileDescriptor(fcsFullFileName);

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
	 * Return the tube name stored in the FCS file
	 * @param processor with already scanned file
	 * @return name of the tube
	 */
	private String getTubeName(FCSProcessor processor) {
		
		// We discriminate here since there is a formatting
		// difference in the value stored in the "TUBE NAME"
		// keyword (which is always found in the file, not 
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
		// Nothing
		return attributes;
	}
		
	/**
	 * Extract and store the Tray attributes
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	private Map<String, String> getTrayAttributes(FCSProcessor processor) {
		Map<String, String> attributes = new HashMap<String, String>();
		// Nothing
		return attributes;
	}
	
	/**
	 * Extract and store the Specimen attributes
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	private Map<String, String> getSpecimenAttributes(FCSProcessor processor) {
		Map<String, String> attributes = new HashMap<String, String>();
		// Nothing
		return attributes;
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
