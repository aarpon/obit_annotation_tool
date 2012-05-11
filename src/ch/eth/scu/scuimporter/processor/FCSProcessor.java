package ch.eth.scu.scuimporter.processor;

import java.io.*;

/**
 * 
 * @author Aaron Ponti
 *
 */
public class FCSProcessor extends Processor {

	/**
	 * 
	 * @param filename
	 */
	public FCSProcessor(String filename) {
		this.filename = filename;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean parse() throws IOException {

		try {

			// Read in the data line by line
			in = new RandomAccessFile(new File(filename),"r");
			
			// Read the HEADER
			parseHeader();
			
			// Read the main TEXT
			parseText();

			// Read the DATA
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
			return ("Valid FCS3.0 file with TEXT: "     + 
					TEXTbegin     + " - " + TEXTend     + ", DATA: " +
					DATAbegin     + " - " + DATAend     + ", ANALYSIS: " +
					ANALYSISbegin + " - " + ANALYSISend + ", OTHER: " +
					OTHERbegin    + " - " + OTHERend    + "." );
		} else {
			return ( "Invalid FCS3.0 file.");
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
		if (new String(VERSION).equals("FCS3.0")) {
			System.out.println(filename + " is not a valid FCS3.0 file!");
			return false;
		}
		
		// ASCII-encoded offset to first byte of TEXT segment (bytes 10 - 17)
		in.seek(10);
		// @TODO
		
		// ASCII-encoded offset to last byte of TEXT segment (bytes 18 - 25)
		in.seek(18);
		// @TODO
		
		// ASCII-encoded offset to first byte of DATA segment (bytes 26 - 33)
		in.seek(26);
		// @TODO
		
		// ASCII-encoded offset to last byte of DATA segment (bytes 34 - 41)
		in.seek(34);
		// @TODO
		
		// ASCII-encoded offset to first byte of ANALYSIS segment (bytes 42 - 49)
		in.seek(42);
		// @TODO
		
		// ASCII-encoded offset to last byte of ANALYSIS segment (bytes 50 - 57)
		in.seek(50);
		// @TODO
		
		// ASCII-encoded offset to user defined OTHER segments (bytes 58 - beginning of next segment)
		in.seek(58);
		// @TODO
		
		return true;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean parseText() throws IOException  {
		return true;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean parseData() throws IOException  {
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
	long OTHERend = 0L;
	boolean valid = false;

}
