package ch.ethz.scu.obit.flow.processors.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.scu.obit.flow.processors.data.model.Experiment;
import ch.ethz.scu.obit.flow.processors.data.model.UserFolder;
import ch.ethz.scu.obit.flow.readers.FCSReader;
import ch.ethz.scu.obit.processors.AbstractProcessor;
import ch.ethz.scu.obit.processors.data.validator.GenericValidator;

/**
 * AbstractFlowProcessor provides an abstract implementation for concrete
 * processors for specific hardware/software configurations.
 * 
 * @author Aaron Ponti
 */
public abstract class AbstractFlowProcessor extends AbstractProcessor {

	/* Private instance variables */
	protected File userFolder;
	protected File userRootFolder;
	protected Experiment currentExperiment;

	/**
	 * A folder descriptor.
	 */
	public UserFolder folderDescriptor = null;

	/**
	 * Constructor
	 * 
	 * @param fullUserFolderName Full path of the user folder containing the
	 *                           exported experiments.
	 */
	public AbstractFlowProcessor(String fullUserFolderName) {

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
		this.userRootFolder = folder.getParentFile();

		// Create a descriptor for the user folder
		folderDescriptor = new UserFolder(folder, userRootFolder);

	}

	/**
	 * Return information regarding the file format.
	 * 
	 * @return descriptive String for the Processor.
	 */
	public String info() {
		return "FCS-3.x based flow cytometry hardware";
	}

	/**
	 * Parse the file to extract data and metadata.
	 * 
	 * @return true if parsing was successful, false otherwise.
	 */
	@Override
	public abstract boolean parse();

	/**
	 * Return a String representation of the AbstractFlowProcessor.
	 * 
	 * @return String containing a description of the AbstractFlowProcessor.
	 */
	public String toString() {
		return userFolder.getName();
	}

	/**
	 * Scan the folder recursively and process all fcs files found
	 * 
	 * @param dir Full path to the directory to scan
	 * @throws IOException Thrown if a FCS file could not be processed
	 */
	protected abstract void recursiveDir(File dir) throws IOException;

	/**
	 * Make sure that the first entry in the file list is an FCS file, if there is
	 * at least one.
	 * 
	 * @param dir Current directory
	 * @return String[] moderately sorted files.
	 */
	protected String[] getSimplySortedList(File dir) {

		// Go over the list, the first FCS file we find we put it in front of
		// the list and return.
		String[] files = dir.list();
		if (files == null) {
			return new String[0];
		}
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
	 * Return the tube name stored in the FCS file (if it is found) or something
	 * else, depending on the hardware.
	 * 
	 * @param processor with already scanned file
	 * @return name of the tube or well
	 */
	protected abstract String getTubeOrWellName(FCSReader processor);

	/**
	 * Return true if the experiment was an indexed sort
	 * 
	 * @param processor with already scanned file
	 * @return true if the experiment was an indexed sort, false otherwise
	 */
	protected boolean isIndexSort(FCSReader processor) {

		// BD FACSAria
		String indexSortLocationCount = processor.getCustomKeyword("INDEX SORTING SORTED LOCATION COUNT");

		// BD Influx Cell Sorter
		String indexSortPositions = processor.getCustomKeyword("INDEXSORTPOSITIONS");

		// If any one of indexSortLocationCount and indexSortPositions is not
		// empty, we have an index sort
		return (!indexSortLocationCount.isEmpty() || !indexSortPositions.isEmpty());

	}

	/**
	 * Return the experiment path the folder name matches the experiment name stored
	 * in the FCS file
	 * 
	 * @param processor   with already scanned file
	 * @param fcsFilePath full file path of the FCS file
	 * @return experiment path
	 */
	protected String getExperimentPath(FCSReader processor, File fcsFilePath) {

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
	 * Return the experiment name stored in the FCS file.
	 *
	 * If the FCS file does not contain an experiment name, the name of the folder
	 * containing the FCS file is returned as experiment name.
	 * 
	 * @param processor with already scanned file
	 * @return name of the experiment
	 */
	protected String getExperimentName(FCSReader processor) {
		String experimentName = processor.getCustomKeyword("EXPERIMENT NAME");
		if (experimentName.isEmpty()) {
			// As experiment name, return the name of the containing folder
			File fcsFile = processor.getFile();
			experimentName = fcsFile.getParentFile().getName();
		}
		return experimentName;
	}

	/**
	 * Return the specimen name stored in the FCS file.
	 *
	 * Before you query for the specimen name, make sure to correctly
	 * identifyContainerType().
	 *
	 * Some hardware does not specify a specimen name for the FCS file. In this case
	 * (and only in this case!), as a used convention, we extract a date in the form
	 * 20160801 from the experiment name and return it. If no date can be found, we
	 * return "UNKNOWN".
	 * 
	 * @param processor with already scanned file
	 * @return name of the specimen
	 */
	protected String getSpecimenName(FCSReader processor) {
		String specimenName = processor.getStandardKeyword("$SRC");
		if (specimenName.isEmpty()) {

			// Extract date information from the experiment name if it
			// exists or return UNKNOWN
			String expName = getExperimentName(processor);
			Pattern p = Pattern.compile("(\\d{8})");
			Matcher m = p.matcher(expName);
			if (m.find()) {
				specimenName = m.group(1);
			} else {
				specimenName = "UNKNOWN";
			}
		}
		return specimenName;
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
	protected String getTrayName(FCSReader processor) {
		String trayName = processor.getCustomKeyword("PLATE NAME");
		if (trayName.isEmpty()) {
			trayName = "UNKNOWN";
		}
		return trayName;
	}

	/**
	 * Identifies the container type of the file (Specimen or Plate or none).
	 *
	 * If the file comes from hardware that does not assign a container to an FCS
	 * file (e.g. the BD Influx), the container type is set to "".
	 *
	 * @param processor FCSProcessor with already scanned file
	 * @return one of "SPECIMEN" or "TRAY"
	 */
	protected String identifyContainerType(FCSReader processor) {

		if (processor.getCustomKeyword("PLATE NAME").isEmpty()) {
			if (!processor.getStandardKeyword("$SRC").isEmpty()) {
				return "SPECIMEN";
			} else {
				return "";
			}
		} else {
			return "TRAY";
		}
	}

	/**
	 * Extract and store the Tray attributes
	 * 
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	protected Map<String, String> getTrayAttributes(FCSReader processor) {
		// Nothing
		return new HashMap<String, String>();
	}

	/**
	 * Extract and store the Specimen attributes
	 * 
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	protected Map<String, String> getSpecimenAttributes(FCSReader processor) {
		// Nothing
		return new HashMap<String, String>();
	}

	/**
	 * Extract and store the Tube attributes
	 * 
	 * @param processor FCSProcessor with already scanned file
	 * @return a key-value map of attributes
	 */
	protected Map<String, String> getTubeOrWellAttributes(FCSReader processor) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("dataFilename", processor.getStandardKeyword("$FIL"));
		attributes.put("indexSort", isIndexSort(processor) ? "true" : "false");
		return attributes;
	}

	/**
	 * Returns true if the passed hardware string (from an FCS file) is a recognized
	 * hardware string for the processor.
	 * 
	 * The classes inheriting from AbstractFlowProcessor must override this method.
	 * 
	 * @param hardwareString Hardware string.
	 * @return true if the string is a valid hardware string for the procesdor,
	 *         false otherwise.
	 */
	public static boolean isValidHardwareString(String hardwareString) {
		throw new UnsupportedOperationException("This method must be overridden!");
	}
}