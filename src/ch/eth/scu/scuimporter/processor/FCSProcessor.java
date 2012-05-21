package ch.eth.scu.scuimporter.processor;

import java.io.*;
import java.util.*;

/**
 * FCSProcessor parses "Data File Standard for Flow Cytometry, Version FCS3.0" files.
 * 
 * Parsing is currently not complete. Only the information needed to add the file to
 * OpenBIS is extracted.
 * 
 * @author Aaron Ponti
 */
public class FCSProcessor extends Processor {

	/* Instance variables */
	private String filename;
	RandomAccessFile in = null;
	long TEXTbegin = 0L;
	long TEXTend   = 0L;
	long DATAbegin = 0L;
	long DATAend = 0L;
	long ANALYSISbegin = 0L;
	long ANALYSISend = 0L;
	long OTHERbegin = 0L;
	boolean valid = false;
	char DELIMITER;
	private ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	
	Map<String, String> TEXTMapStandard = new LinkedHashMap<String, String>();
	Map<String, String> TEXTMapCustom = new LinkedHashMap<String, String>();
	Map<String, String> DATAMap = new LinkedHashMap<String, String>();

	/**
	 * Constructor 
	 * @param filename
	 */
	public FCSProcessor(String filename) {
		this.filename = filename;
	}

	/**
	 * Return information about the Processor
	 */
	public String info() {
		return "Data File Standard for Flow Cytometry, Version FCS3.0.";
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean parse() throws IOException {

		try {

			// We use a RandomAccessFile to be able to seek around freely
			in = new RandomAccessFile(new File(filename),"r");
			
			// Read the HEADER
			parseHeader();
			
			// Read the main TEXT
			parseText();
			
			// Process the parameters
			processParameters();

			// Read the DATA (events)
			parseData();

			// Read the ANALYSIS
			parseAnalysis();

			// Read the ANALYSIS
			parseOther();
			
		} finally {

			// Always close the stream
			if (in != null) { 
				try { 
					in.close();
					System.out.println("File closed.");
				} catch (Exception e) {

				}
			}

		}

		valid = true;
		return true;

	}
	
	/**
	 * 
	 */
	public String toString() {
		if ( valid == true ) {
			String str = 
			"Valid FCS3.0 file with TEXT: "     + 
					TEXTbegin     + " - " + TEXTend     + ", DATA: " +
					DATAbegin     + " - " + DATAend     + ", ANALYSIS: " +
					ANALYSISbegin + " - " + ANALYSISend + ", OTHER: " +
					OTHERbegin    + ".\n" +
					"DELIMITER: (char) "  + (int)DELIMITER + "\n\n";  

			// Output the list of standard key-value pairs
			Set<String> keySet = TEXTMapStandard.keySet();
			
			str += "Standard TEXT keyword-value pairs (" + keySet.size() + "):\n\n";

			for ( String key : keySet ) {
				str += ( key + ": " + TEXTMapStandard.get(key) + "\n" );  
			}

			// Output the list of custom key-value pairs 
			keySet = TEXTMapCustom.keySet();
			
			str += "\n\n";
			str += "Custom TEXT keyword-value pairs (" + keySet.size() + "):\n\n";

			for ( String key : keySet ) {
				str += ( key + ": " + TEXTMapCustom.get(key) + "\n" );  
			}
			
			// Output the list of parameters (and their attributes)
			str += "\n\n";
			str += "Parameters and their attributes:\n\n";

			for (Parameter p : parameters ) {
				str += ( "Parameter: " + p.name + ", range: " + p.range + 
						", bits: " + p.bits + ", decade: " + p.decade + ", " +
						"log: " + p.log + ", logzero: " + p.logzero + 
						", gain: " + p.gain + "\n");
			}

			return str;

		} else {
			return ( "Error: invalid FCS3.0 file.");
		}
	}
	
	/**
	 * Parse the header 
	 * @return true if the file header could be parsed successfully, false otherwise 
	 * @throws IOException
	 */
	private boolean parseHeader() throws IOException {
		
		// Read and check the version
		in.seek(0);
		byte[] VERSION = new byte[6];
		in.read(VERSION);
		if (!(new String(VERSION).equals("FCS3.0"))) {
			System.out.println(filename + " is not a valid FCS3.0 file!");
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
		// This can be a valid offset, or 0: if it is 0, it means that the segment
		// is larger than 99,999,999 bytes
		in.seek(26);
		in.read(eightByteArray);
		DATAbegin = Integer.parseInt((new String(eightByteArray)).trim());
		
		// ASCII-encoded offset to last byte of DATA segment (bytes 34 - 41)
		// This can be a valid offset, or 0: if it is 0, it means that the segment
		// is larger than 99,999,999 bytes
		in.seek(34);
		in.read(eightByteArray);
		DATAend = Integer.parseInt((new String(eightByteArray)).trim());
		
		// ASCII-encoded offset to first byte of ANALYSIS segment (bytes 42 - 49)
		// This can be a valid offset, 0, or even blank. If 0, $BEGINANALYSIS must 
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

		// ASCII-encoded offset to user defined OTHER segments (bytes 58 - beginning of next segment)
		in.seek(58);
		tmp = (new String(eightByteArray)).trim();
		if (tmp.length() == 0) {
			OTHERbegin = 0;
		} else {
			OTHERbegin = Integer.parseInt(tmp);
		}
		
		// The TEXT and DATA offsets can theoretically be swapped in the header segment
		swapOffsetsIfNeeded();
		
		return true;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean parseText() throws IOException  {
		// Read the TEXT segment
		in.seek(TEXTbegin);
		int LEN = (int)(TEXTend - TEXTbegin + 1); // TEXT cannot be longer than 99,999,999 bytes
		byte[] bText = new byte[LEN];
		in.read(bText);
		
		// Get the delimiter character
		DELIMITER = (char)bText[0];

		// Get the keyword-value pairs and store them in the hash map
		storeKeyValuePairs(new String(bText));
		
		return true;
	}

	/**
	 * Parse the DATA (events) segment.
	 * @return true if the parsing was successful (or no data was present), false otherwise
	 * @throws IOException
	 */
	private boolean parseData() throws IOException  {

		// Seek to the DATA segment
		long dataOffset;
		try {
			dataOffset = Long.parseLong( TEXTMapStandard.get("$BEGINDATA").trim() ); 
		} catch ( NumberFormatException e ) {
			System.out.println("Invalid offset for the DATA segment! " +
					"This is a bug! Please report it.");
			return false;
		}
		if (dataOffset == 0) {
			System.out.println("No DATA present.");
			return true;
		}
		
		// Seek to the beginning of the data offset
		in.seek(dataOffset);
		
		// To read the data in the correct format we need to know the
		// number of parameters, the number of events, the datatype and
		// the endianity.
		int nParameters   = numParameters();
		int nEvents       = numEvents();
		String datatype   = datatype();
		String endianity  = endianity();
		
		return true;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean parseAnalysis() throws IOException  {
		return true;		
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean parseOther() throws IOException {
		return true;		
	}

	/**
	 * The TEXT and DATA offsets can theoretically be swapped in the header segment.
	 * We make sure that TEXT is assigned the lower and DATA the higher offset.
	 */
	private void swapOffsetsIfNeeded() {
		if (TEXTbegin > DATAbegin) {
			long tmp;
			tmp = TEXTbegin;
			TEXTbegin = DATAbegin;
			DATAbegin = tmp;
			tmp = TEXTend;
			TEXTend = DATAend;
			DATAend = tmp;			
		}
	}

	/**
	 * Extracts and returns the value in a segment for a given key
	 * @param segment String containing the full segment (e.g. TEXT)
	 */
	private void storeKeyValuePairs(String segment) {
		assert(segment.charAt(0) == DELIMITER);
		assert(segment.charAt(segment.length() - 1) == DELIMITER);
		
		// Process all 
		int beginIndex = 0;
		int interIndex = 0;
		int endIndex   = 0;
		
		// Parse the full segment
		while (true) {
			interIndex   = segment.indexOf(DELIMITER, beginIndex + 1);
			if (interIndex == -1) {
					break;
			}
			endIndex     = segment.indexOf(DELIMITER, interIndex + 1);
			String key   = segment.substring(beginIndex + 1, interIndex).trim();
			String value = segment.substring(interIndex + 1, endIndex).trim();
			
			// If the key starts with a $ sign, we found a standard FCS keyword and
			// we store it in the TEXTMapStandard map; otherwise, we have a custom 
			// keyword we store it in the TEXTMapCustom map 
			if (key.charAt(0) == '$') {
				TEXTMapStandard.put(key, value);
			} else {
				TEXTMapCustom.put(key, value);
			}
			beginIndex = endIndex;
		}
	}
	
	/**
	 * Process the parameters.
	 * @return 	true if there were parameters and they could be processed successfully,
	 * 			false otherwise.
	 */
	private boolean processParameters() {

		// Number of parameters
		int numParameters = numParameters();
		if (numParameters == 0) {
			return false;
		}

		// Now go over the parameters and extract all info.
		// Mind that parameter count starts at 1.
		for (int i = 1; i <= numParameters; i++) {
			
			// New Parameter
			Parameter param = new Parameter();

			// Name
			param.name  = TEXTMapStandard.get("$P" + i + "N");
			
			// Range
			param.range = Integer.parseInt(TEXTMapStandard.get("$P" + i + "R"));
			
			// Bits
			param.bits  = Integer.parseInt(TEXTMapStandard.get("$P" + i + "B"));
			
			// Linear or logarithmic amplifiers?
			String decade = TEXTMapStandard.get("$P" + i + "E");
			String decadeParts[] = decade.split(",");
			param.decade = Float.parseFloat(decadeParts[0]);
			float value  = Float.parseFloat(decadeParts[1]);
			if (param.decade == 0.0) {
				// Amplification is linear or undefined
				param.log = 0.0f;
				param.logzero = 0.0f;
			} else {
				param.log = 1.0f;
				if (value == 0.0) {
					param.logzero = 1.0f;
				} else {
					param.logzero = value;
				}
			}
			
			// Gain
			param.gain = Float.parseFloat(TEXTMapStandard.get("$P" + i + "G"));

			// Add the Parameter
			parameters.add(param);
		}
		
		return true;
	}

	/**
	 * Return the number of parameters in the dataset.
	 * @return number of parameters.
	 */
	private int numParameters() {
		int numParameters = 0;
		if (TEXTMapStandard.containsKey("$PAR")) {
			numParameters = Integer.parseInt(TEXTMapStandard.get("$PAR"));
		}
		return numParameters;
	}

	/**
	 * Return the number of events in the dataset.
	 * @return number of events.
	 */
	private int numEvents() {
		int numEvents = 0;
		if (TEXTMapStandard.containsKey("$TOT")) {
			numEvents = Integer.parseInt(TEXTMapStandard.get("$TOT"));
		}
		return numEvents;
	}

	/**
	 * Return the datatype of the data bytes, one of "I", "F", "D", or "A".
	 * I: unsigned integer;
	 * F: single-precision IEEE floating point;
	 * D: double-precision IEEE floating point;
	 * A: ASCII
	 * @return datatype of the measurements ("I", "F", "D", "A"), or "N" if not defined
	 */
	private String datatype() {
		String datatype = "N";
		if (TEXTMapStandard.containsKey("$DATATYPE")) {
			datatype = TEXTMapStandard.get("$DATATYPE");
		}
		return datatype;
	}
	
	/**
	 * Return the endianity of the data bytes, one of "L", "B", "U"
	 * L: little endian (1,2,3,4);
	 * B: big endian (4,3,2,1)
	 * U: unsupported (3,4,1,2) 
	 * N: not defined
	 * @return endianity of the data bytes ("L", "B", "U"), or "N" if not defined
	 */
	private String endianity() {
		String datatype = "N";
		if (TEXTMapStandard.containsKey("$BYTEORD")) {
			String byteOrd = TEXTMapStandard.get("$BYTEORD");
			if ( byteOrd.equals("1,2,3,4") ) {
				datatype = "L";
			} else if ( byteOrd.equals("4,3,2,1") ) {
				datatype = "B";
			} else {
				datatype = "U";
			}
		}
		return datatype;
	}

	// Parameter class to store parameter attributes
	public class Parameter {
		public String name;
		public int range;
		public int bits;
		public float decade;
		public float log;
		public float logzero;
		public float gain;
		
		public Parameter() {
		}
	}
	
}

