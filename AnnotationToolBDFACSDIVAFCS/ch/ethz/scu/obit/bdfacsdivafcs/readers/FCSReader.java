package ch.ethz.scu.obit.bdfacsdivafcs.readers;

import java.io.*;
import java.nio.*;
import java.util.*;

import ch.ethz.scu.obit.readers.AbstractReader;

/**
 * FCSReader parses "Data File Standard for Flow Cytometry, Version FCS3.0 or
 * FCS3.1" files.
 * 
 * Parsing is currently not complete: - additional byte buffer manipulation
 * needed for datatype "A" (ASCII) - only one DATA segment per file is processed
 * (since apparently no vendor makes use of the possibility to store more than
 * experiment per file) - ANALYSIS segment is not parsed - OTHER text segment is
 * not parsed
 * 
 * @author Aaron Ponti
 */
public final class FCSReader extends AbstractReader {

	/* Private instance variables */
	private File filename;
	private boolean enableDataParsing;
	private RandomAccessFile in = null;
	private String fcsVersion = "";
	private int TEXTbegin = 0;
	private int TEXTend = 0;
	private int DATAbegin = 0;
	private int DATAend = 0;
	private int ANALYSISbegin = 0;
	private int ANALYSISend = 0;
	private int OTHERbegin = 0;
	private char DELIMITER;
	private boolean isFileParsed = false;
	private boolean isDataLoaded = false;

	/* Public instance variables */

	/**
	 * String-string map of parametersAttr attributes
	 */
	public Map<String, String> parametersAttr = new HashMap<String, String>();

	/**
	 * String-to-string map of key-value pairs for the standard FCS 3.0/3.1
	 * keywords
	 */
	public Map<String, String> TEXTMapStandard = new LinkedHashMap<String, String>();

	/**
	 * String-to-string map of key-value pairs for custom FCS 3.0/3.1 keywords
	 */
	public Map<String, String> TEXTMapCustom = new LinkedHashMap<String, String>();

	/**
	 * DATA segment (linear array of bytes)
	 */
	public ByteBuffer DATA = null;

	/**
	 * Constructor
	 * 
	 * @param filename Name with full path of the file to be opened.
	 * @param parseData True if the data should be read, false if only metadata
	 *        should be parsed. 
	 */
	public FCSReader(File filename, boolean parseData) {
		this.filename = filename;
		this.enableDataParsing = parseData;
	}

	/**
	 * Destructor.
	 */
	public void finalize() throws Throwable {

		// Make sure the file is closed
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				System.err.println("Could not close FCS file on garbage collection!");
			}
		}

		// Call the parent finalize()
		super.finalize();
	}

	/**
	 * Return information regarding the file format.
	 * 
	 * @return descriptive String for the Processor.
	 */
	public String info() {
		return "Data File Standard for Flow Cytometry, Version FCS3.0 and FCS3.1.";
	}

	/**
	 * Return the FCS file version
	 * 
	 * @return a String containing the file version (FCS3.0 or FCS3.1).
	 */
	public String getFCSVersion() {
		return fcsVersion;
	}

	/**
	 * Parses the file to extract data and metadata.
	 * 
	 * @return true if parsing was successful, false otherwise.
	 * @throws IOException if parsing the FCS file failed.
	 */
	@Override
	public boolean parse() throws IOException {

		try {

			// We use a RandomAccessFile to be able to seek around freely
			in = new RandomAccessFile(filename, "r");

			// Read the HEADER
			parseHeader();

			// Read the main TEXT
			parseText();

			// Process the parameters
			processParameters();

			if (enableDataParsing) {

				// Read the DATA (events)
				parseData();

				// Read the ANALYSIS (if present)
				parseAnalysis();
			}

			// Read the OTHER text segment (if present)
			parseOther();

		} catch (IOException e) {

			errorMessage = "Could not open file.";
			System.err.println(errorMessage);

		} finally {

			// Always close the stream
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					// Silent
				}
			}

		}

		// Reset the error message
		errorMessage = "";

		// Store and return state
		isFileParsed = (in != null);
		return isFileParsed;

	}

	/**
	 * Return the file name of associated to the FCSReader.
	 * 
	 * @return String containing the file name associated to the FCSReader.
	 */
	public String toString() {
		return filename.getName();
	}

	/**
	 * Return a simplified class name to use in XML.
	 * 
	 * @return simplified class name.
	 */
	@Override
	public String getType() {
		return "fcs";
	}

	/**
	 * Returns the stored File object (pointing to the FCS file being processed)
	 * 
	 * @return File object pointing to the FCS file
	 */
	public File getFile() {
		return filename;
	}

	/**
	 * Return the parsed FCSReader metadata information.
	 * 
	 * @return String containing the metadata of the FCSReader.
	 */
	public String metadataDump() {

		if (!isFileParsed) {
			return "File could not be parsed.";
		}

		String str = "Valid " + fcsVersion + " file with TEXT: " + TEXTbegin + " - " + TEXTend + ", DATA: " + DATAbegin
				+ " - " + DATAend + ", ANALYSIS: " + ANALYSISbegin + " - " + ANALYSISend + ", OTHER: " + OTHERbegin
				+ ".\n" + "DELIMITER: (char) " + (int) DELIMITER + "\n\n";

		// Output the list of standard key-value pairs
		Set<String> keySet = TEXTMapStandard.keySet();

		str += "Standard TEXT keyword-value pairs (" + keySet.size() + "):\n\n";

		for (String key : keySet) {
			str += (key + ": " + TEXTMapStandard.get(key) + "\n");
		}

		// Output the list of custom key-value pairs
		keySet = TEXTMapCustom.keySet();

		str += "\n\n";
		str += "Custom TEXT keyword-value pairs (" + keySet.size() + "):\n\n";

		for (String key : keySet) {
			str += (key + ": " + TEXTMapCustom.get(key) + "\n");
		}

		// Output the list of parameters (and their attributes)
		str += "\n\n";
		str += "Parameters and their attributes:\n\n";

		for (String key : parametersAttr.keySet()) {
			str += (key + ": " + parametersAttr.get(key) + "\n");
		}

		return str;

	}

	/**
	 * Returns all parameter names.
	 * 
	 * @return The String array of parameter names.
	 */
	public ArrayList<String> getParameterNames() {
		ArrayList<String> paramNames = new ArrayList<String>(numParameters());
		for (int i = 1; i <= numParameters(); i++) {
			paramNames.add(parametersAttr.get("P" + i + "N"));
		}
		return paramNames;
	}

	/**
	 * Returns all standard FCS 3.0/3.1 keywords as a String - String map
	 * 
	 * @return The String - String Map of all standard FCS 3.0/3.1 keywords
	 */
	public Map<String, String> getStandardKeywords() {
		return TEXTMapStandard;
	}

	/**
	 * Return the value associated to a standard FCS 3.0/3.1 keyword or empty
	 * 
	 * @param key
	 *            One of the standard keywords (staring with "$")
	 * @return The value associated with the passed keyword or empty String if
	 *         not found
	 */
	public String getStandardKeyword(String key) {
		if (TEXTMapStandard.containsKey(key)) {
			return TEXTMapStandard.get(key);
		} else {
			return "";
		}
	}

	/**
	 * Returns all custom, non FCS 3.0/3.1-compliant keywords as a String -
	 * String map
	 * 
	 * @return The String - String Map of all custom, non FCS 3.0/3.1-compliant
	 *         keywords
	 */
	public Map<String, String> getCustomKeywords() {
		return TEXTMapStandard;
	}

	/**
	 * Return the value associated with a custom, non FCS 3.0/3.1-compliant
	 * keyword or empty
	 * 
	 * @param key
	 *            A custom keywords (without $ at the beginning)
	 * @return The value associated with the passed keyword or empty String if
	 *         not found
	 */
	public String getCustomKeyword(String key) {
		if (TEXTMapCustom.containsKey(key)) {
			return TEXTMapCustom.get(key);
		} else {
			return "";
		}
	}

	/**
	 * Returns all keywords as a String - String map (FCS 3.0/3.1-compliant and
	 * custom)
	 * 
	 * @return The String - String Map of all keywords
	 */
	public Map<String, String> getAllKeywords() {
		Map<String, String> allMap = new HashMap<String, String>();
		allMap.putAll(TEXTMapStandard);
		allMap.putAll(TEXTMapCustom);
		return allMap;
	}

	/**
	 * Return the number of events in the dataset.
	 * 
	 * @return number of events.
	 */
	public int numEvents() {
		int numEvents = 0;
		if (TEXTMapStandard.containsKey("$TOT")) {
			numEvents = Integer.parseInt(TEXTMapStandard.get("$TOT"));
		}
		return numEvents;
	}

	/**
	 * Return the number of parameters in the dataset.
	 * 
	 * @return number of parameters.
	 */
	public int numParameters() {
		int numParameters = 0;
		if (TEXTMapStandard.containsKey("$PAR")) {
			numParameters = Integer.parseInt(TEXTMapStandard.get("$PAR"));
		}
		return numParameters;
	}

	/**
	 * Return subset of measurements with optional stride for parameter with 
	 * given column index in double precision.
	 * @param columnIndex Index of the measurement column.
	 * @param nValues number of values to be read. Set to 0 to read them all.
	 * @param sampled True if the nValues must be sampled with constant stride
	 *                throughout the total number of rows, false if the first 
	 *                nValues rows must simply be returned. 
	 * @return array of measurements.
	 * @throws IOException If the datatype is not recognized.
	 */
	public double[] getDataPerColumnIndex(int columnIndex, int nValues, 
			boolean sampled) throws IOException {

	    // Make sure that he data was loaded
        if (!isDataLoaded) {
            return new double[0];
        }

	    // Some constants
		int nParams = numParameters();
		int nEvents = numEvents();
		String datatype = datatype();

		int step;
		// If all values must be read, the step is 1.
		if (nValues == 0 || nValues > nEvents) {
			nValues = nEvents;
			step = 1;
		} else {
			if (sampled) {
				step = (int) (((float) nEvents) / nValues);
				if (step == 0) {
					step = 1;
				}
			} else {
				step = 1;
			}
		}

		// Allocate space for the events
		double[] m = new double[nValues];

		// Go through the buffer and return the values for the requested column
		DATA.rewind();

		int c = 0; // Global measurement counter
		int n = 0; // Row counter
		int t = 0; // Accepted value counter
		while (DATA.hasRemaining()) {

			// Get the value, convert it to double
			double tmp;
			if (datatype.equals("F")) {
				tmp = (double) DATA.getFloat();
			} else if (datatype.equals("I")) {
			    // The integer format is 2 bytes only and unsigned!
			    tmp = (double) ((short) (DATA.getShort()) & 0xffff);
			} else if (datatype.equals("D")) {
			    tmp = (double) DATA.getDouble();
			} else if (datatype.equals("A")) {
			    tmp = (double) DATA.get();
			} else {
				throw new IOException("Unknown data type!");
			}

			// If we are at the right column, we store it
			if (c % nParams == columnIndex) {
				if (n % step == 0) {
					m[t] = tmp;
					t++;

					// Are we done?
					if (t >= nValues) {
						break;
					}
				}
				n++;
			}
			c++;

		}

		// Return the array
		return m;
	}

	/**
	 * Export the read data to a CSV file
	 * 
	 * @param csvFile
	 *            Full path of the CSV file
	 * @return true if the CSV file could be saved, false otherwise.
	 */
	public boolean exportDataToCSV(File csvFile) {

	    // Make sure that he data was loaded
        if (!isDataLoaded) {
            return false;
        }

		FileWriter fw;
		try {
			fw = new FileWriter(csvFile);
		} catch (IOException e) {
			return false;
		}
		BufferedWriter writer = new BufferedWriter(fw);

		// Write the parameters names as header line
		for (int i = 1; i <= numParameters(); i++) {
			try {
				writer.write(parametersAttr.get("P" + i + "N") + ",");
			} catch (IOException e) {
				// Close writer
				try {
					writer.close();
				} catch (IOException e1) {
					return false;
				}
				return false;
			}
		}

		// Next line
		try {
			writer.write("\n");
		} catch (IOException e) {
			// Close writer
			try {
				writer.close();
			} catch (IOException e1) {
				return false;
			}
			return false;
		}

		// Store some constants
		int numParameters = numParameters();
		String datatype = datatype();

		// Make sure to rewind the buffer
		DATA.rewind();

		// Write the values
		int nParameter = 0;
		while (DATA.hasRemaining()) {
			try {
				if (datatype.equals("F")) {
					writer.write((float) DATA.getFloat() + ",");
				} else if (datatype.equals("I")) {
				    // The integer format is 2 bytes only and unsigned!
					writer.write(((short) (DATA.getShort()) & 0xffff) + ",");
				} else if (datatype.equals("D")) {
					writer.write((double) DATA.getDouble() + ",");
				} else if (datatype.equals("A")) {
				    // Get a single byte
					writer.write((char) DATA.get() + ",");
				} else {
					throw new IOException("Unknown data type!");
				}

				// New line
				nParameter++;
				if (nParameter == numParameters) {
					writer.write("\n");
					nParameter = 0;
				}
			} catch (IOException e) {
				// Close writer
				try {
					writer.close();
				} catch (IOException e1) {
					return false;
				}
				return false;
			}
		}

		// Close writer
		try {
			writer.close();
		} catch (IOException e) {
			return false;
		}

		return true;

	}

	/**
	 * Parse the header.
	 * 
	 * @return true if the file header could be parsed successfully, false
	 *         otherwise.
	 * @throws IOException
	 */
	private boolean parseHeader() throws IOException {

		// Read and check the version
		in.seek(0);
		byte[] VERSION = new byte[6];
		in.read(VERSION);
		fcsVersion = new String(VERSION);
		if (!(fcsVersion.equals("FCS3.0") || fcsVersion.equals("FCS3.1"))) {
			errorMessage = filename + " is not a valid FCS version 3.0 or 3.1 file!";
			System.out.println(errorMessage);
			return false;
		}

		// We will use an 8-byte array several times in the following
		byte[] eightByteArray = new byte[8];

		// ASCII-encoded offset to first byte of TEXT segment (bytes 10 - 17)
		in.seek(10);
		in.read(eightByteArray);
		TEXTbegin = Integer.parseInt((new String(eightByteArray)).trim());

		// ASCII-encoded offset to last byte of TEXT segment (bytes 18 - 25)
		in.seek(18);
		in.read(eightByteArray);
		TEXTend = Integer.parseInt((new String(eightByteArray)).trim());

		// ASCII-encoded offset to first byte of DATA segment (bytes 26 - 33)
		// This can be a valid offset, or 0: if it is 0, it means that the
		// segment
		// is larger than 99,999,999 bytes
		in.seek(26);
		in.read(eightByteArray);
		DATAbegin = Integer.parseInt((new String(eightByteArray)).trim());

		// ASCII-encoded offset to last byte of DATA segment (bytes 34 - 41)
		// This can be a valid offset, or 0: if it is 0, it means that the
		// segment
		// is larger than 99,999,999 bytes
		in.seek(34);
		in.read(eightByteArray);
		DATAend = Integer.parseInt((new String(eightByteArray)).trim());

		// ASCII-encoded offset to first byte of ANALYSIS segment (bytes 42 -
		// 49)
		// This can be a valid offset, 0, or even blank. If 0, $BEGINANALYSIS
		// must
		// be checked
		in.seek(42);
		in.read(eightByteArray);
		String tmp = (new String(eightByteArray)).trim();
		if (tmp.length() == 0) {
			ANALYSISbegin = 0;
		} else {
			ANALYSISbegin = Integer.parseInt(tmp);
		}

		// ASCII-encoded offset to last byte of ANALYSIS segment (bytes 50 - 57)
		// This can be a valid offset, 0, or even blank. If 0, $ENDANALYSIS must
		// be checked
		in.seek(50);
		tmp = (new String(eightByteArray)).trim();
		if (tmp.length() == 0) {
			ANALYSISend = 0;
		} else {
			ANALYSISend = Integer.parseInt(tmp);
		}

		// ASCII-encoded offset to user defined OTHER segments (bytes 58 -
		// beginning of next segment)
		in.seek(58);
		tmp = (new String(eightByteArray)).trim();
		if (tmp.length() == 0) {
			OTHERbegin = 0;
		} else {
			OTHERbegin = Integer.parseInt(tmp);
		}

		// The TEXT and DATA offsets can theoretically be swapped in the header
		// segment
		swapOffsetsIfNeeded();

		return true;
	}

	/**
	 * Parse the TEXT segment.
	 * 
	 * @return true if parsing was successful, false otherwise.
	 * @throws IOException
	 */
	private boolean parseText() throws IOException {
		// Read the TEXT segment
		in.seek(TEXTbegin);
		int LEN = (TEXTend - TEXTbegin + 1); // TEXT cannot be longer than
												// 99,999,999 bytes
		byte[] bText = new byte[LEN];
		in.read(bText);

		// Get the delimiter character
		DELIMITER = (char) bText[0];

		// Get the keyword-value pairs and store them in the hash map
		storeKeyValuePairs(new String(bText));

		return true;
	}

	/**
	 * Parse the DATA (events) segment.
	 * 
	 * @return true if the parsing was successful (or no data was present),
	 *         false otherwise.
	 * @throws IOException
	 *             TODO Add support for multiple DATA segments.
	 */
	private boolean parseData() throws IOException {

		// Seek to the DATA segment
		long dataOffset;
		try {
			dataOffset = Long.parseLong(TEXTMapStandard.get("$BEGINDATA").trim());
		} catch (NumberFormatException e) {
			errorMessage = "Invalid offset for the DATA segment! " + "This is a bug! Please report it.";
			System.out.println(errorMessage);
			return false;
		}
		if (dataOffset == 0) {
			System.out.println("No DATA present.");
			return true;
		}

		// Seek to the beginning of the data offset
		in.seek(dataOffset);

		// Read and store the data
		return readDataBlock();

	}

	/**
	 * Parse the ANALYSIS segment.
	 * 
	 * @return true if parsing was successful, false otherwise.
	 * @throws IOException
	 *             TODO Implement
	 */
	private boolean parseAnalysis() throws IOException {
		return true;
	}

	/**
	 * Parse the OTHER segment.
	 * 
	 * @return true if parsing was successful, false otherwise.
	 * @throws IOException
	 *             TODO Implement
	 */
	private boolean parseOther() throws IOException {
		return true;
	}

	/**
	 * The TEXT and DATA offsets can theoretically be swapped in the header
	 * segment. We make sure that TEXT is assigned the lower and DATA the higher
	 * offset.
	 */
	private void swapOffsetsIfNeeded() {
		// If the data block is larger than 99,999,999 bytes,
		// DATAbegin will be 0.
		// In this case we do NOT want to swap!
		if (DATAbegin == 0) {
			return;
		}
		if (TEXTbegin > DATAbegin) {
			int tmp;
			tmp = TEXTbegin;
			TEXTbegin = DATAbegin;
			DATAbegin = tmp;
			tmp = TEXTend;
			TEXTend = DATAend;
			DATAend = tmp;
		}
	}

	/**
	 * Extracts and returns the value in a segment for a given key.
	 * 
	 * @param segment
	 *            String containing the full segment (e.g. TEXT).
	 */
	private void storeKeyValuePairs(String segment) {
		assert(segment.charAt(0) == DELIMITER);
		assert(segment.charAt(segment.length() - 1) == DELIMITER);

		// Process all
		int beginIndex = 0;
		int interIndex = 0;
		int endIndex = 0;

		// Parse the full segment
		while (true) {
			interIndex = segment.indexOf(DELIMITER, beginIndex + 1);
			if (interIndex == -1) {
				break;
			}
			endIndex = segment.indexOf(DELIMITER, interIndex + 1);
			String key = segment.substring(beginIndex + 1, interIndex).trim();
			String value = segment.substring(interIndex + 1, endIndex).trim();

			// If the key starts with a $ sign, we found a standard FCS keyword
			// and we store it in the TEXTMapStandard map; otherwise, we have a
			// custom keyword we store it in the TEXTMapCustom map
			if (key.charAt(0) == '$') {
				TEXTMapStandard.put(key, value);
			} else {
				TEXTMapCustom.put(key, value);
			}
			beginIndex = endIndex;
		}
	}

	/**
	 * Process the extracted parameters from the standard TEXT map.
	 * 
	 * @return true if there were parameters and they could be processed
	 *         successfully, false otherwise.
	 */
	private boolean processParameters() {

		// Number of events
		int numEvents = numEvents();

		// Number of parameters
		int numParameters = numParameters();

		// Store the number of events
		parametersAttr.put("numEvents", Integer.toString(numEvents));

		// Store the number of parameters
		parametersAttr.put("numParameters", Integer.toString(numParameters));

		// If there are no parameters, we leave.
		if (numParameters == 0) {
			return false;
		}

		// Now go over the parameters and extract all info.
		// Mind that parameter count starts at 1.
		String key = "";
		for (int i = 1; i <= numParameters; i++) {

			// Name
			key = "P" + i + "N";
			String name = TEXTMapStandard.get("$" + key);
			if (name == null) {
				name = "<not set>";
			}
			parametersAttr.put(key, name);

			// Label
			key = "P" + i + "S";
			String label = TEXTMapStandard.get("$" + key);
			if (label == null) {
				label = "";
			}
			parametersAttr.put(key, label);

			// Range
			key = "P" + i + "R";
			String range = TEXTMapStandard.get("$" + key);
			if (range == null) {
				range = "NaN";
			}
			parametersAttr.put(key, range);

			// Bits
			key = "P" + i + "B";
			String bits = TEXTMapStandard.get("$" + key);
			if (bits == null) {
				bits = "NaN";
			}
			parametersAttr.put(key, bits);

			// Linear or logarithmic amplifiers?
			float log = 0.0f;
			float log_zero = 0.0f;
			key = "P" + i + "E";
			String decade = TEXTMapStandard.get("$" + key);
			if (decade != null) {
				String decadeParts[] = decade.split(",");
				float f_decade = Float.parseFloat(decadeParts[0]);
				float f_value = Float.parseFloat(decadeParts[1]);
				if (f_decade == 0.0) {
					// Amplification is linear or undefined
					log = 0.0f;
					log_zero = 0.0f;
				} else {
					log = 1.0f;
					if (f_value == 0.0) {
						log_zero = 1.0f;
					} else {
						log_zero = f_value;
					}
				}
			}
			parametersAttr.put(key + "_LOG", Float.toString(log));
			parametersAttr.put(key + "_LOGZERO", Float.toString(log_zero));

			// Gain
			key = "P" + i + "G";
			String gain = TEXTMapStandard.get("$" + key);
			if (gain == null) {
				gain = "NaN";
			}
			parametersAttr.put(key, gain);

			// Voltage
			key = "P" + i + "V";
			String voltage = TEXTMapStandard.get("$" + key);
			if (voltage == null) {
				voltage = "NaN";
			}
			parametersAttr.put(key, voltage);

			// Log or linear
			key = "P" + i + "DISPLAY";
			String display = TEXTMapCustom.get(key);
			if (display == null) {
				display = "LIN";
			}
			parametersAttr.put(key, display);
		}

		return true;
	}

	/**
	 * Return the datatype of the data bytes, one of "I", "F", "D", or "A". I:
	 * unsigned integer; F: single-precision IEEE floating point; D:
	 * double-precision IEEE floating point; A: ASCII.
	 * 
	 * @return datatype of the measurements ("I", "F", "D", "A"), or "N" if not
	 *         defined.
	 */
	private String datatype() {
		String datatype = "N";
		if (TEXTMapStandard.containsKey("$DATATYPE")) {
			datatype = TEXTMapStandard.get("$DATATYPE");
		}
		return datatype;
	}

	/**
	 * Return the endianity of the data bytes, one of "L", "B", "U". L: little
	 * endian (1,2,3,4); B: big endian (4,3,2,1); U: unsupported (3,4,1,2); N:
	 * not defined.
	 * 
	 * @return endianity of the data bytes ("L", "B", "U"), or "N" if not
	 *         defined.
	 */
	private String endianity() {
		String datatype = "N";
		if (TEXTMapStandard.containsKey("$BYTEORD")) {
			String byteOrd = TEXTMapStandard.get("$BYTEORD");
			if (byteOrd.equals("1,2,3,4")) {
				datatype = "L";
			} else if (byteOrd.equals("4,3,2,1")) {
				datatype = "B";
			} else {
				datatype = "U";
			}
		}
		return datatype;
	}

	/**
	 * Return the data acquisition mode, one of "C", "L", "U". C: One correlated
	 * multivariate histogram stored as a multidimensional array; L: list mode:
	 * for each event, the value of each parameter is stored in the order in
	 * which the parameters are described. U: Uncorrelated univariate
	 * histograms: there can be more than one univariate histogram per data set.
	 * N: not defined.
	 * 
	 * @return acquisition mode of the data ("C", "L", "U"), or "N" if not
	 *         defined.
	 */
	private String mode() {
		if (TEXTMapStandard.containsKey("$MODE")) {
			String mode = TEXTMapStandard.get("$MODE");
			if (mode.equals("C") || mode.equals("L") || mode.equals("U")) {
				return mode;
			} else {
				return "N";
			}
		}
		return "N";
	}

	/**
	 * Reads and stores the data segment
	 * 
	 * @return true if reading the data segment was successful, false otherwise
	 *         TODO Use the information about the type of data
	 */
	private boolean readDataBlock() {

	    // Reset the isDataLoaded flag
        isDataLoaded = false;

		// To read the data in the correct format we need to know the endianity.
		String endianity = endianity();

		// Endianity
		ByteOrder endian;
		if (endianity.equals("L")) {
			endian = ByteOrder.LITTLE_ENDIAN;
		} else if (endianity.equals("B")) {
			endian = ByteOrder.BIG_ENDIAN;
		} else {
			errorMessage = "Unknown endianity!";
			System.err.println(errorMessage);
			return false;
		}

		// Allocate a (byte) buffer to hold the data segment
		int size = (DATAend - DATAbegin + 1);
		byte[] recordBuffer = new byte[size];

		// Create a ByteBuffer wrapped around the byte array that
		// reads with the desired endianity
		DATA = ByteBuffer.wrap(recordBuffer);
		DATA.order(endian);

		// Read
		try {
			in.read(recordBuffer);
		} catch (IOException e) {
			errorMessage = "Could not read the data segment from file!";
			System.out.println(errorMessage);
			return false;
		}

		// Make sure to be at the beginning of the buffer
		DATA.rewind();

		// Reset error message
		errorMessage = "";

		// Set the isDataLoaded flag
		isDataLoaded = true;

		// Return success
		return true;
	}
}
