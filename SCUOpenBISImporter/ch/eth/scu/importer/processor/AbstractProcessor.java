package ch.eth.scu.importer.processor;

/**
 * Abstract base class for file processors. Actual implementations must extend this class.
 * @author Aaron Ponti
 */
abstract public class AbstractProcessor {

	/**
	 * Parses the file to extract data and metadata. 
	 * The file name should be passed to the constructor.
	 * @return true if parsing was successful, false otherwise.
	 * @throws Exception
	 */
	abstract public boolean parse() throws Exception;
	
	/**
	 * Information regarding the file format handled by the Processor.
	 * @return descriptive String for the Processor.
	 */
	abstract public String info();
	
}
