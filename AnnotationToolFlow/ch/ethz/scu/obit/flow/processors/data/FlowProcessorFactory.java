package ch.ethz.scu.obit.flow.processors.data;

import java.io.File;
import java.io.IOException;

import ch.ethz.scu.obit.flow.readers.FCSReader;

/**
 * The Composite Microscopy Reader factory returns the
 * AbstractCompositeMicroscopyReader that announces to be able to read and
 * interpret the content of the input folder.
 * 
 * @author Aaron Ponti
 */
public class FlowProcessorFactory {

	private static String hardwareString = "";

	/**
	 * Creates a composite microscope reader viewer depending on the answer of their
	 * canRead() method.
	 * 
	 * @param folder Folder to be processed.
	 * @return a concrete implementation of an AbstractCompositeMicroscopyReader
	 * @throws IOException if the hardware class could not be defined.
	 */
	public static AbstractFlowProcessor createProcessor(String folder) throws IOException {

		hardwareString = "";

		// Is there something in the folder?
		File folderToScan = new File(folder);

		if (folderToScan.list().length == 0) {
			// Default processor
			return new BDLSRFortessaFlowProcessor(folder);
		}

		hardwareString = FlowProcessorFactory.recursiveDir(folderToScan);

		if (BDLSRFortessaFlowProcessor.isValidHardwareString(hardwareString)) {
			return new BDLSRFortessaFlowProcessor(folder);
		} else if (BDFACSAriaFlowProcessor.isValidHardwareString(hardwareString)) {
			return new BDFACSAriaFlowProcessor(folder);
		} else if (BDInfluxFlowProcessor.isValidHardwareString(hardwareString)) {
			return new BDInfluxFlowProcessor(folder);
		} else if (BCMoFloXDPFlowProcessor.isValidHardwareString(hardwareString)) {
			return new BCMoFloXDPFlowProcessor(folder);
		} else if (BIORADS3eFlowProcessor.isValidHardwareString(hardwareString)) {
			return new BIORADS3eFlowProcessor(folder);
		} else if (SONYCellSorterFlowProcessor.isValidHardwareString(hardwareString)) {
			return new SONYCellSorterFlowProcessor(folder);
		} else {
			throw new IOException("Unknown hardware type!");
		}

	}

	private static String recursiveDir(File dir) {

		// Since this function is recursive, we check whether we
		// already found the value.
		if (!hardwareString.equals("")) {

			// We found the string, we can return
			return hardwareString;
		}

		// Get the list of files
		String[] files = dir.list();

		// Empty subfolders are not accepted
		if (files.length == 0) {
			return "";
		}

		// Go over the files and folders
		for (String f : files) {

			File file = new File(dir + File.separator + f);

			// Is it a directory? Recurse into it
			if (file.isDirectory()) {

				// Recurse into the subfolder
				hardwareString = recursiveDir(file);

				if (!hardwareString.equals("")) {

					// We found the string, we can return
					return hardwareString;
				}

				// Move on to the next file
				continue;
			}

			// Is it an FCS file?
			String fileName = file.getName();
			int indx = fileName.lastIndexOf(".");
			if (indx == -1) {
				continue;
			}
			String ext = fileName.substring(indx);
			if (!ext.equalsIgnoreCase(".fcs")) {
				continue;
			}

			// Is it an FCS file? Scan it and extract the information
			FCSReader processor = new FCSReader(file, false);
			try {

				// Parse the file
				processor.parse();

				// Return the hardware string
				return processor.getStandardKeyword("$CYT");

			} catch (IOException e) {
				continue;
			}

		}

		// We haven't found any FCS file!
		return "";

	}

}
