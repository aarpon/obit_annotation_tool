package ch.eth.scu.importer.processors.validator;

import java.util.ArrayList;

/**
 * A dataset validator. In its simplest implementation, the validator 
 * contains:
 *    - a boolean flag to indicate whether the dataset is valid;
 *    - a boolean flag to indicate whether the dataset is already 
 *      annotated;
 *    - and an array of Strings listing all problems found in case
 *      the dataset is invalid.
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
	 * Boolean flag that indicates whether the has been annotated already.
	 */
	public boolean isAnnotated = false;

	/**
	 * List of Strings containing the reasons why a dataset is invalid.
	 */
	public ArrayList<String> errorMessages = new ArrayList<String>();
	
	/**
	 * Constructor
	 */
	public GenericValidator() { }
}
