package ch.ethz.scu.obit.microscopy.processors.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.ethz.scu.obit.microscopy.readers.MicroscopyReader;
import ch.ethz.scu.obit.processors.AbstractProcessor;
import ch.ethz.scu.obit.processors.data.model.AbstractDescriptor;
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

	/* Folder to scan (recursively) */
	private File userFolder;

	/* List of supported file formats */
	private ArrayList<String> supportedFormats = new ArrayList<String>(
			Arrays.asList(".1sc", ".2", ".2fl", ".3", ".4", ".5", ".acff",
					".afm", ".aim", ".al3d", ".am", ".amiramesh", ".apl",
					".arf", ".avi", ".bip", ".bmp", ".c01", ".cfg", ".cr2",
					".crw", ".cxd", ".czi", ".dat", ".dcm", ".dicom", ".dm2",
					".dm3", ".dti", ".dv", ".eps", ".epsi", ".exp", ".fdf",
					".fff", ".ffr", ".fits", ".flex", ".fli", ".frm", ".gel",
					".gif", ".grey", ".hdr", ".hed", ".his", ".htd", ".html",
					".hx", ".ics", ".ids", ".img", ".ims", ".inr", ".ipl",
					".ipm", ".ipw", ".jp2", ".jpg", ".jpk", ".jpx", ".l2d",
					".labels", ".lei", ".lif", ".liff", ".lim", ".lsm", ".mdb",
					".mea", ".mnc", ".mng", ".mod", ".mov", ".mrc", ".mrw",
					".mtb", ".mvd2", ".naf", ".nd", ".nd2", ".ndpi", ".nef",
					".nhdr", ".nrrd", ".obsep", ".oib", ".oif", ".ome", ".par",
					".pcx", ".pds", ".pgm", ".pic", ".pict", ".png", ".pnl",
					".pr3", ".ps", ".psd", ".r3d", ".raw", ".res", ".scn",
					".sdt", ".seq", ".sld", ".sm2", ".sm3", ".spi", ".stk",
					".stp", ".svs", ".sxm", ".tfr", ".tga", ".tif", ".tiff",
					".tnb", ".top", ".txt", ".v", ".vms", ".vsi", ".vws",
					".wat", ".xdce", ".xml", ".xqd", ".xqf", ".xv", ".xys",
					".zfp", ".zfr", ".zvi"));

	/* Public instance variables */
	public UserFolder folderDescriptor = null;

	/**
	 * Constructor
	 */
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

		// Create a descriptor for the user folder
		folderDescriptor = new UserFolder(folder);

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

		// Get the directory listing
		String[] files = dir.list();

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

			// A microscopy file cannot be at the user folder root!
			if (file.getParent().equals(this.userFolder.toString())) {
				validator.isValid = false;
				validator.invalidFilesOrFolders.put(file,
						"File must be in subfolder.");
				continue;
			}

			// Do we have an unknown file? If we do, we move on to the next.
			if (!supportedFormats.contains(ext)) {
				validator.isValid = false;
				validator.invalidFilesOrFolders.put(file, "Invalid file type.");
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
			MicroscopyFile microscopyFileDesc;
			String microscopyFileName = fileName;
			String microscopyFileKey = experimentName + "_"
					+ microscopyFileName;
			microscopyFileDesc = new MicroscopyFile(file);

			// Store it in the Experiment descriptor
			expDesc.microscopyFiles.put(microscopyFileKey, microscopyFileDesc);

		}

	}

	/**
	 * Descriptor that represents a folder containing a dataset.
	 * 
	 * @author Aaron Ponti
	 */
	public class Folder extends PathAwareDescriptor {

		public Map<String, Experiment> experiments = new LinkedHashMap<String, Experiment>();

		public Folder(File fullFolder) {

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
	 * 
	 * @author Aaron Ponti
	 */
	public class Experiment extends ExperimentDescriptor {

		// Experiment description
		public String description = "";

		// Store the microscopy files associated with this Experiment
		public Map<String, MicroscopyFile> microscopyFiles =
				new LinkedHashMap<String, MicroscopyFile>();

		/**
		 * Constructor
		 * 
		 * @param name
		 *            Name of the experiment.
		 */
		public Experiment(File name) {

			// Call base constructor
			super(name);

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
	 * Descriptor representing a microscopy file.
	 * 
	 * @author Aaron Ponti
	 */
	public class MicroscopyFile extends DatasetDescriptor {

		private boolean fileScanned = false;
		
		public String description = "";

		public Map<String, MicroscopyFileSeries> series = new LinkedHashMap<String, MicroscopyFileSeries>();

		/**
		 * Constructor.
		 * 
		 * @param microscopyFileName
		 *            Microscopy file name with full path
		 */
		public MicroscopyFile(File microscopyFileName) throws IOException {

			// Call base constructor
			super(microscopyFileName);

			// Store the file name
			this.setName(microscopyFileName.getName());

			// Append the attibute file size.
			long s = microscopyFileName.length();
			float sMB = s / (1024 * 1024);
			String unit = "MiB";
			if (sMB > 750) {
				sMB = sMB / 1024;
				unit = "GiB";
			}
			attributes.put("fileSize", String.format("%.2f", sMB) + " " + unit);

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
			if (! success) {
				
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
				MicroscopyFileSeries fileSeries = new MicroscopyFileSeries(
						i, seriesAttr.get(keySeries));

				// Append it to the MicroscopyFile descriptor
				series.put(keySeries, fileSeries);
			}
			
			// Retun success 
			return true;

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
		 * @param index Index of the series in file.
		 * @param attr  String-string map of attributes for the series
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

			}
			this.setName("series_" + index);
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
