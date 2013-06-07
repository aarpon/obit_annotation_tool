package ch.eth.scu.importer.processors.nikonnd2;

import java.io.File;
import java.util.Properties;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.processors.AbstractProcessor;
import ch.eth.scu.importer.processors.lsrfortessa.BDFACSDIVAFCSProcessor.Folder;
import ch.eth.scu.importer.processors.validator.GenericValidator;

/**
 * NikonProcessor parses Nikon ND2 files (using the bio-formats library).
 * @author Aaron Ponti
 */
public class NikonProcessor extends AbstractProcessor {

	/**
	 * Constructor
	 */
	/**
	 * Constructor
	 * @param fullFolderName Full path of the folder containing the exported experiment.
	 */
	public NikonProcessor(String fullFolderName, String userName) {

		// Instantiate the validator
		validator = new GenericValidator();

		// Make sure rootFolderName is a valid directory
		File folder = new File(fullFolderName);
		if (!folder.isDirectory()) {
			System.err.println("Expected directory name.");
			return;
		}

	}
	
	@Override
	public boolean parse() throws Exception {
		// TODO Auto-generated method stub
		return false;
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

}
