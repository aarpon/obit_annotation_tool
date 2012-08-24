package ch.eth.scu.importer.processor;

import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.SpecimenDescriptor;
import ch.eth.scu.importer.processor.model.AbstractDescriptor;
import ch.eth.scu.importer.processor.model.RootDescriptor;

import java.io.*;
import java.util.*;

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
	private File rootFolder;
	private boolean isValid = false;
	
	/* Public instance variables */
	public RootFolderDescriptor rootDescriptor = null;
	
	/**
	 * Constructor
	 * @param rootFolderName Full path of the folder containing the exported experiment.
	 */
	public BDFACSDIVAFCSProcessor(String rootFolderName) {

		// Make sure rootFolderName is a valid directory
		File rootFolder = new File(rootFolderName);
		if (!rootFolder.isDirectory()) {
			System.err.println("Expected directory name.");
			return;
		}
		
		// Set the root folder
		this.rootFolder = rootFolder;
		
		// Create a RootDescriptor
		rootDescriptor = new RootFolderDescriptor(rootFolder); 
		
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
			recursiveDir(this.rootFolder);
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
			if (rootDescriptor.experiments.containsKey(experimentName) ) {
				expDesc = rootDescriptor.experiments.get(experimentName);
			} else {
				expDesc = 
						new ExperimentDescriptor(getExperimentName(processor));
				// Store attributes
				expDesc.setAttributes(getExperimentAttributes(processor));
				rootDescriptor.experiments.put(experimentName, expDesc);
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
				if (specDesc.tubes.containsKey(tubeKey) ) {
					tubeDesc = specDesc.tubes.get(tubeKey);
				} else {
					tubeDesc = new TubeDescriptor(tubeName,
							file.getCanonicalPath());
					// Store attributes
					specDesc.setAttributes(getTubeAttributes(processor));
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
				if (specDesc.tubes.containsKey(tubeKey) ) {
					tubeDesc = specDesc.tubes.get(tubeKey);
				} else {
					tubeDesc = new TubeDescriptor(tubeName,
							file.getCanonicalPath());	
					// Store attributes
					specDesc.setAttributes(getTubeAttributes(processor));
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
		return rootFolder.getName();
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

		for (String expKey : rootDescriptor.experiments.keySet()) {

			ExperimentDescriptor e = rootDescriptor.experiments.get(expKey);
			
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
	 * Class that specialized the RootDescriptor and represented the root folder. 
	 * @author Aaron Ponti
	 */
	public class RootFolderDescriptor extends RootDescriptor {
		
		public Map<String, ExperimentDescriptor> experiments = 
				new LinkedHashMap<String, ExperimentDescriptor>();
		
		public RootFolderDescriptor(File name) {
			
			// Call the base constructor
			super(name.getName());

		}
		
		@Override
		public String getType() {
			return "Root";
		}

	}
	
	/**
	 * Class that represents an experiment obtained from the FCS file.
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
	 * Class that represents an FCS file associated to a Tube.
	 * An FCS File is always a child of a Tube.
	 * @author Aaron Ponti
	 */
	public class FCSFileDescriptor extends AbstractDescriptor {

		private String fullFileName = "";
		
		/**
		 * Constructor.
		 * @param fcsFileName FCS file name with full path
		 */
		public FCSFileDescriptor(String fcsFileName) {

			// Store the file name
			this.fullFileName = fcsFileName; 
			this.name = (new File( fcsFileName )).getName();

		}

		/**
		 * Return a String representation of the extracted Tube node.
		 * @return String representation of the Tube node.
		 */
		@Override
		public String toString() {
			String str = this.name;
			return str;
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
		
	}
	
	/**
	 * Class that represents a specimen obtained from the FCS file.
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
	 * Class that represents a tray obtained from the FCS file.
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
		 * @param trayNode DOM node that refers to a Tray.
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
	 * Class that represents a tube obtained from the FCS file.
	 * A Tube is always a child of a Specimen.
     *
	 * TODO Add the attributes!
	 * @author Aaron Ponti
	 */
	public class TubeDescriptor extends AbstractDescriptor {
		
		public FCSFileDescriptor fcsFile;
		
		/**
		 * Constructor.
		 * @param tubeNode DOM node that refers to a Tube.
		 */
		public TubeDescriptor(String name, String fcsFullFileName) {
	
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
