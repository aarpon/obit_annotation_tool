package ch.eth.scu.importer.nikonnd2.processors;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.eth.scu.importer.nikonnd2.processors.model.ExperimentDescriptor;
import ch.eth.scu.importer.nikonnd2.readers.NikonReader;
import ch.eth.scu.importer.processors.AbstractProcessor;
import ch.eth.scu.importer.processors.model.DatasetDescriptor;
import ch.eth.scu.importer.processors.model.PathAwareDescriptor;
import ch.eth.scu.importer.processors.model.RootDescriptor;
import ch.eth.scu.importer.processors.validator.GenericValidator;

/**
 * NikonProcessor parses Nikon ND2 files (using the bio-formats library).
 * @author Aaron Ponti
 */
public class NikonProcessor extends AbstractProcessor {

	/* Folder to scan (recursively) */
	private File userFolder;

	/* Public instance variables */
	public UserFolder folderDescriptor = null;

	/* BioFormats reader */
	private NikonReader nikonReader;

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

		// Create a descriptor for the user folder
		folderDescriptor = new UserFolder(folder); 		

	}
	
	/**
	 * Return the full path of the processed folder processed.
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
		return "Nikon NIS-Elements ND2";
	}

	@Override
	public String getType() {
		// Return type
		return "NIKONND2";
	}

	/**
	 * Scan the folder recursively and process all ND2 files found
	 * @param dir Full path to the directory to scan
	 * @throws IOException Thrown if an ND2 file could not be processed
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

			// Check the file type.
			String fileName = file.getName();
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
			if (! ext.equalsIgnoreCase(".nd2")) {
				validator.isValid = false;
				validator.invalidFilesOrFolders.put(
						file, "Invalid file type.");
			}

			// Create a new ExperimentDescriptor or reuse an existing one
			// The name of the experiment is the name of the folder that
			// contains current file
			Experiment expDesc;
			String experimentName = dir.getName();
			if (folderDescriptor.experiments.containsKey(experimentName)) {
				expDesc = folderDescriptor.experiments.get(experimentName);
			} else {
				expDesc = new Experiment(dir);
				folderDescriptor.experiments.put(experimentName, expDesc);
			}

			// Store
			ND2File nd2FileDesc;
			String nd2FileName = fileName;
			String nd2FileKey = experimentName + "_" + nd2FileName;
			nd2FileDesc = new ND2File(file);	

			// Store it in the Experiment descriptor
			expDesc.nd2Files.put(nd2FileKey, nd2FileDesc);
			
		}

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

		// Experiment description
		public String description = "";
			
		// Store the ND2 files associated with this Experiment
		public Map<String, ND2File> nd2Files = 
				new LinkedHashMap<String, ND2File>();
		
		/**
		 * Constructor
		 * @param name Name of the experiment.
		 */
		public Experiment(File name) {
			
			// Call base constructor
			super(name);
			
			// Store the experiment name
			this.name = name.getName();

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
			this.name = fullFolder.getName();

		}
		
		@Override
		public String getType() {
			return "Folder";
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
