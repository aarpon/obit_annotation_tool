package ch.ethz.scu.obit.flow.processors.data;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.scu.obit.flow.readers.FCSReader;

/**
 * BDLSRFortessaFlowProcessor parses folder structures created by the following
 * software and hardware combination:
 *
 * BD FACSDiva software (6.1, 7.0, 8.0) on BD FACS Aria
 * 
 * Please notice that when exporting data from the FACSDiva software, there are
 * two options:
 *
 * 1) export as FCS files (supported)
 * 2) export as experiment (not supported)
 *
 * When exporting as experiment, the experiment metadata is written to an
 * additional XML file and not correctly stored in the FCS files. These FCS
 * files can not correctly be processed by flow analysis software like FlowJo.
 * For this reason, data exported as experiment from FACSDiva (i.e. with the
 * accompanying XML file) are flagged as invalid and are not processed.
 *
 * @author Aaron Ponti
 */
public final class BDFACSAriaFlowProcessor extends BDLSRFortessaFlowProcessor {

	/* Map of known hardware strings to supported hardware */
	private static final Map<String, String> knownHardwareStrings;
    static
    {
    	knownHardwareStrings = new HashMap<String, String>();

    	// FD FACSAria III
    	knownHardwareStrings.put("FACSAriaIII", "BD FACSAria III");
    }

    /**
     * Constructor.
     * @param fullUserFolderName Full path of the user folder containing the exported experiments.
     */
	public BDFACSAriaFlowProcessor(String fullUserFolderName) {
		super(fullUserFolderName);
	}
	
	/**
	 * Extract and store the Experiment attributes
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
			validator.invalidFilesOrFolders.put(
					processor.getFile(),
					"Wrong hardware string: " + acqHardwareString);
		}
		attributes.put("acq_hardware", acqHardwareString);

		// Software string
		String acqSoftwareString = processor.getCustomKeyword("CREATOR");
		if (acqSoftwareString.isEmpty()) {
		    acqSoftwareString = processor.getCustomKeyword("APPLICATION");
		}
		
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

		// Acquisition software
		attributes.put("acq_software", acqSoftwareString);

		// Acquisition date
		attributes.put("date", processor.getStandardKeyword("$DATE"));

		return attributes;
	}

	/**
	 * Returns true if the passed hardware string (from an FCS file) is a recognized
	 * hardware string for the BD FACS Aria.
	 * @param hardwareString Hardware string.
	 * @return true if the string is a valid hardware string for the BD FACS Aria, false otherwise.
	 */
	public static boolean isValidHardwareString(String hardwareString) {

		return knownHardwareStrings.containsKey(hardwareString);
	}

}