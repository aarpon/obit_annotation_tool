package ch.ethz.scu.obit.flow.processors.data.model;

import java.io.File;
import java.io.IOException;

import ch.ethz.scu.obit.processors.data.model.DatasetDescriptor;

/**
 * Descriptor representing an FCS file associated to a Tube.
 * An FCS File is always a child of a Tube.
 * @author Aaron Ponti
 */
public class FCSFile extends DatasetDescriptor {

	/**
	 * List of parameters with their attributes
	 */
	public FCSFileParameterList parameterList = null;

	/**
	 * Constructor.
	 * @param fcsFileName FCS file name with full path
	 * @throws IOException If parsing the file failed.
	 */
	public FCSFile(File fcsFileName, File userRootFolder) throws IOException {

		// Call base constructor
		super(fcsFileName, userRootFolder);

		// Store the file name
		this.setName(fcsFileName.getName());

		// Set the attribute relative file name. Since this will be 
		// used by the openBIS dropboxes running on a Unix machine, 
		// we make sure to use forward slashes for path separators 
		// when we set it as an attribute.
		attributes.put("relativeFileName",
				this.relativePath.replace("\\", "/"));
	}

	/**
	 * Return a String representation of the extracted FCS file.
	 * @return String representation of the FCS file.
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "FCSFile";
	}

}
