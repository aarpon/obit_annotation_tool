package ch.ethz.scu.obit.readers;

import java.util.List;

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
     * @throws Exception if parsing failed.
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
     * @return last error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Implement an 'implode' function as in PHP.
     * @param values Array of doubles
     * @return Comma-separated list of values as string.
     */
    protected String implode(double [] values) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i != values.length - 1) {
                sb.append(", ");
            }
        }
        String joined = sb.toString();
        return joined;
    }

    /**
     * Implement an 'implode' function as in PHP.
     * @param values List of Strings.
     * @return Comma-separated list of values as string.
     */
    protected String implode(List<String> values) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            sb.append(values.get(i));
            if (i != values.size() - 1) {
                sb.append(", ");
            }
        }
        String joined = sb.toString();
        return joined;
    }
}
