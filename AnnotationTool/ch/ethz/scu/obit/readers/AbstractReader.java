package ch.ethz.scu.obit.readers;

/**
 * Abstract base class for file readers. Actual implementations must
 * extend this class.
 * @author Aaron Ponti
 */
abstract public class AbstractReader {

	/**
	 * In case of error, put an explanation in errorMessage.
	 */
	protected String errorMessage = "";

	/**
	 * Parses the file to extract data and metadata. 
	 * 
	 * @return true if parsing was successful, false otherwise.
	 * @throws Exception
	 */
	abstract public boolean parse() throws Exception;

	/**
	 * Information regarding the file format handled by the Reader.
	 * @return descriptive String for the Reader.
	 */
	abstract public String info();

	/**
	 * Type of the reader
	 * @return String type of the reader.
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