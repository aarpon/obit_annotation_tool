package ch.eth.scu.importer.processors;

import ch.eth.scu.importer.processors.validator.GenericValidator;

/**
 * Abstract base class for file processors. Actual implementations must extend this class.
 * @author Aaron Ponti
 */
abstract public class AbstractProcessor {

	/**
	 * Validator
	 */
	public GenericValidator validator;

	/**
	 * Parses the file to extract data and metadata. 
	 * The file name should be passed to the constructor.
	 * 
	 * The parse() method MUST set the isValid and isAnnotated properties
	 * in the Validator.
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
	
	
}