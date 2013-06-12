package ch.eth.scu.importer.processors.nikonnd2;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.eth.scu.importer.processors.AbstractProcessor;
import ch.eth.scu.importer.processors.model.DatasetDescriptor;
import ch.eth.scu.importer.processors.model.FirstLevelDescriptor;
import ch.eth.scu.importer.processors.nikonnd2.model.ExperimentDescriptor;
import ch.eth.scu.importer.processors.validator.GenericValidator;

/**
 * NikonProcessor parses Nikon ND2 files (using the bio-formats library).
 * @author Aaron Ponti
 */
public class NikonProcessor extends AbstractProcessor {

	/* Folder to scan (recursively) */
	private File topFolder;

	/* Number of valid files found in folder */
	private int numberOfValidFiles = 0;

	/* BioFormats reader */
	private NikonReader nikonReader;

	/* Public instance variables */
	public Map<String, Experiment> experiments;
	
	/**
	 * Constructor
	 */
	/**
	 * Constructor
	 * @param fullFolderName Full path of the folder containing the
	 *        exported experiment.
	 */
	public NikonProcessor(String fullFolderName) {

		// Instantiate the validator
		validator = new GenericValidator();

		// fullFolderName could in principle point to a file. We
		// leave it to the parse() method to take care of it.
		File folder = new File(fullFolderName);
		this.topFolder = folder;
		
		// Set the root folder
		this.topFolder = folder;

		// Create a RootDescriptor
		experiments = new LinkedHashMap<String, Experiment>();

	}
	
	/**
	 * Return the full path of the processed folder processed.
	 * @return path of the processed folder.
	 */
	public File getFolder() {
		return this.topFolder;
	}
	
	@Override
	public boolean parse() {

		// First, make sure this.topFolder is indeed a folder
		if (!this.topFolder.isDirectory()) {
			this.validator.isAnnotated = false;
			this.validator.isValid = false;
			this.validator.errorMessages.add(
					"Expected folder, found file.");
			this.errorMessage = "Only folders allowed at root level.";
			
			// We return true because the parsing succeeded. However,
			// we recognized the file as an invalid dataset and we 
			// dutifully set the appropriate flags in the validator.
			return true;
		}

		// Then, we check if the dataset is already annotated. An  
		// annotated dataset has a file ending in _properties.six
		// at the root of the folder.
		// In this case we set the validator.isAnnotated flag to true
		// (as well as the validator.isValid flag) and return
		// immediately without parsing.
		File[] propertiesFile = this.topFolder.listFiles(
				new FileFilter() {
					public boolean accept(File file) {
						return (file.isFile() && 
							file.getName().endsWith("_properties.six"));
					}
				});
		if (propertiesFile.length > 0) {
			this.validator.isAnnotated = true;
			this.validator.isValid = true;
			return true;
		} else {
			this.validator.isAnnotated = false;
		}

		// Scan the root folder recursively to reconstruct the experiment
		// structure.
		try {
			recursiveDir(this.topFolder, 0);
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
		return "Nikon NIS-Elements ND2";
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "NIKONND2";
	}

	/**
	 * Scan the folder recursively and process all ND2 files found
	 * @param dir Full path to the directory to scan
	 * @throws IOException Thrown if an ND2 file could not be processed
	 */
	private void recursiveDir(File dir, int level) throws IOException {
	
		// Create a descriptor for current level
		
		// Get the directory listing
		String [] files = dir.list();

		// Go over the files and folders
		for (String f : files) {
			
			File file = new File(dir + File.separator + f);
	
			// Is it a directory? Recurse into it
			if (file.isDirectory()) {
	
				// Recurse into the subfolder
				recursiveDir(file, level + 1);
	
				// Move on to the next file
				continue;
			}

			// Check the file type.
			String fileName = file.getName();
			int indx = fileName.lastIndexOf(".");
			if (indx == -1) {
				continue;
			}
			String ext = fileName.substring(indx);
			
			// Do we have an unknown file? If we do, we move on to the next.
			if (! ext.equalsIgnoreCase(".nd2")) {
				continue;
			}
			
			// Add one to the number of valid files found
			numberOfValidFiles++;

			// Create a new ExperimentDescriptor or reuse an existing one
			// The name of the experiment is the name of the folder that
			// contains current file
			Experiment expDesc;
			String experimentName = dir.getName();
			if (experiments.containsKey(experimentName)) {
				expDesc = experiments.get(experimentName);
			} else {
				expDesc = new Experiment(experimentName);
				experiments.put(experimentName, expDesc);
			}

			// Store
			ND2File nd2FileDesc;
			String nd2FileName = fileName;
			String nd2FileKey = experimentName + "_" + nd2FileName;
			nd2FileDesc = new ND2File(file);	

			// Store it in the Experiment descriptor
			expDesc.nd2Files.put(nd2FileKey, nd2FileDesc);
			
		}
	
		// At this point, the dataset is for sure not annotated, but it
		// could still be invalid (if no files were found)
		this.validator.isValid = (numberOfValidFiles > 0);
		this.validator.isAnnotated = false;
		if (numberOfValidFiles == 0) {
			this.validator.errorMessages.add("No ND2 files found.");
		}

	}

	/**
	 * Descriptor that represents a folder containing a dataset. 
	 * @author Aaron Ponti
	 */
	public class Folder extends FirstLevelDescriptor {
		
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

		// Store the ND2 files associated with this Experiment
		public Map<String, ND2File> nd2Files = 
				new LinkedHashMap<String, ND2File>();
		
		/**
		 * Constructor
		 * @param name Name of the experiment.
		 */
		public Experiment(String name) {
			
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
	 * Descriptor representing a Nikon ND2 file.
	 * @author Aaron Ponti
	 */
	public class ND2File extends DatasetDescriptor {

		private boolean fileScanned = false;

		/**
		 * Constructor.
		 * @param fcsFileName FCS file name with full path
		 */
		public ND2File(File nd2FileName) throws IOException {

			// Call base constructor
			super(nd2FileName);

			// Store the file name
			this.name = nd2FileName.getName();

			// Append the attibute file size.
			long s = nd2FileName.length();
			float sMB = s / (1024 * 1024);
			String unit = "MiB";
			if (sMB > 750) {
				sMB = sMB / 1024;
				unit = "GiB";
			}
			attributes.put("fileSize",
					String.format("%.2f", sMB) + " " + unit);

			// Append the attribute relative file name. Since this  
			// will be used by the openBIS dropboxes running on a Unix  
			// machine, we make sure to use forward slashes for path 
			// separators when we set it as an attribute.
			attributes.put("relativeFileName",
					this.relativePath.replace("\\", "/"));
		}

		/**
		 * Return a String representation of the extracted ND2 file.
		 * @return String representation of the ND2 filw.
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
			return "ND2File";
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
		 */
		public Map<String, String> scanFileAndGetAttributes() {
			
			// If the file was already scanned we return the attributes
			if (fileScanned) {
				return attributes;
			}
			
			// First scan
			nikonReader = new NikonReader(this.fullPath);
			attributes.putAll(nikonReader.getAttributes());
			
			// Set the fileScanned attributes to true
			fileScanned = true;
			
			// Return the attributes
			return attributes;
		}
	}
	
}
