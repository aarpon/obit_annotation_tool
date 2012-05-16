package ch.eth.scu.scuimporter.processor;

abstract public class Processor {

	/**
	 * Method to be invoked to parse the file. 
	 * The file name should be passed to the constructor.
	 * @return true if parsing was successful, false otherwise.
	 * @throws Exception from the XML parser
	 */
	abstract public boolean parse() throws Exception;
	
	/**
	 * Information regarding the file format handled by the Processor.
	 * @return descriptive String for the Processor.
	 */
	abstract public String info();
	
}
