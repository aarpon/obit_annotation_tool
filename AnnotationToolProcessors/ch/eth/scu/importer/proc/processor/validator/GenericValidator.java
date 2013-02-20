package ch.eth.scu.importer.proc.processor.validator;

import java.util.ArrayList;

/**
 * A dataset validator. In its simplest implementation, the validator contains
 * just a flag to indicate whether the dataset is valid or not and an array of
 * Strings listing all problems found is in case it is not: in this 
 * implementaion, the actual validation is performed by the processor. 
 * Extend this class for more specific use.
 * @author Aaron Ponti
 */
public class GenericValidator {

	/**
	 * Boolean flag that indicates where the dataset is valid or not.
	 */
	public boolean isValid = true;
	
	/**
	 * String containing the reason why a dataset is invalid.
	 */
	public ArrayList<String> errorMessages = new ArrayList<String>();
	
	/**
	 * Constructor
	 */
	public GenericValidator() { }
}
