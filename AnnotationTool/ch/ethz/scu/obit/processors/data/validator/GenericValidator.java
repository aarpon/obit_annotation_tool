package ch.ethz.scu.obit.processors.data.validator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A validator for the user directory. In its simplest implementation, 
 * the validator contains:
 *    - a boolean flag to indicate whether the user folder contains
 *    	valid datasets and is ready to be annotated and moved;
 *    - a Map of invalid Files (or Folders) and the corresponding 
 *   	String with the reason/error for being invalid.
 *
 * In this implementation, the actual validation is performed by the 
 * processor. The validator is just used to keep track of the state. 
 * 
 * AbstractProcessor holds a public reference to the GenericValidator.
 * It is the task of the concrete Processors to instantiate the
 * GenericValidator or any specialization of it.
 *  
 * Extend this class for more specific use.
 * @author Aaron Ponti
 */
public class GenericValidator {

	/**
	 * Boolean flag that indicates whether the dataset is valid.
	 */
	public boolean isValid = true;

	/**
	 * Map of invalid Files (or Folders) that and the corresponding 
	 * List of Strings with all reasons/errors for being invalid.
	 */
	public Map<File, String> invalidFilesOrFolders = 
			new HashMap<File, String>();

	/**
	 * Constructor
	 */
	public GenericValidator() { }
}
