package ch.ethz.scu.obit.flow.processors.data.model;

import java.io.File;
import java.io.IOException;

/**
 * Descriptor representing a well obtained from the FCS file.
 * A Well is always a child of a Specimen.
 *
 * @author Aaron Ponti
 */
public class Well extends Tube {

	/**
	 * Constructor.
	 * @param name Name of the Well.
	 * @param fcsFullFileName Full file name of the FCS file associated
	 * with the Well.
	 * @throws IOException if parsing the FCS file failed.
	 */
	public Well(String name, File fcsFullFileName, File userRootFolder) 
			throws IOException {

		// Call base constructor
		super(name, fcsFullFileName, userRootFolder);

	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override
	public String getType() {
		return "Well";
	}

}
