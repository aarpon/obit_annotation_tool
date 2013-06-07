package ch.eth.scu.importer.processors;

import ch.eth.scu.importer.processors.validator.GenericValidator;

/**
 * Abstract base class for file processors. Actual implementations must
 * extend this class.
 * @author Aaron Ponti
 */
abstract public class AbstractProcessor {

	/**
	 * In case of error, put an explanation in errorMessage.
	 */
	protected String errorMessage = "";
	
	/**
	 * Validator
	 */
	public GenericValidator validator;

	/**
	 * Parses the file to extract data and metadata. 
	 * The file/folder name should be passed to the constructor.
	 * 
	 * The parse() method MUST set the isValid and isAnnotated properties
	 * in the Validator.
	 * 
	 * Please notice that parse() should return false only if the 
	 * parsing really failed because of major problems. If a file or
	 * folder is recognized to be an invalid dataset, this must be
	 * notified by setting the validator property isValid to true; 
	 * the parse() should still return true as long as the parsing 
	 * could be completed.  
	 * 
	 * @return true if parsing was successful, false otherwise.
	 * @throws Exception
	 */
	abstract public boolean parse() throws Exception;
	
	/**
	 * Information regarding the file format handled by the Processor.
	 * @return descriptive String for the Processor.
	 */
	abstract public String info();
	
	/**
	 * Type of the processor (to be used in the JTree)
	 * @return String type of the processor.
	 */
	abstract public String getType();
	
	/**
	 * Returns last errorMessage.
	 * 
	 * After every operation, the state must be updated. In case 
	 * something goes wrong, the state is set to false and the
	 * errorMessage String is set property. 
	 * 
	 *  This function returns last error message.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
}
