package ch.ethz.scu.obit.flow.processors.data;

import java.util.HashMap;
import java.util.Map;

/**
 * BDLSRFortessaFlowProcessor parses folder structures created by the following
 * software and hardware combination:
 *
 * 1. BD FACSDiva software (6.1, 7.0, 8.0) on BD LSRFortessa and BD FACSAriaIII
 *
 * 2. BD FACS™ Sortware (1.2) on BD Influx Cell Sorter
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
	 * Returns true if the passed hardware string (from an FCS file) is a recognized
	 * hardware string for the BD FACS Aria.
	 * @param hardwareString Hardware string.
	 * @return true if the string is a valid hardware string for the BD FACS Aria, false otherwise.
	 */
	public static boolean isValidHardwareString(String hardwareString) {

		return knownHardwareStrings.containsKey(hardwareString);
	}

}