package ch.eth.scu.scuimporter.processor;

/**
 * Parses "BD BioSciences FACSDivaª Software" XML files with the associated
 * "Data File Standard for Flow Cytometry, Version FCS3.0" files generated 
 * by the BD LSRFortessa cell analyzer.
 * (http://www.bdbiosciences.com/instruments/lsr/index.jsp).
 * @author Aaron Ponti
 */
public class BDLSRFortessa extends Processor {

	String filename;
	
	public BDLSRFortessa(String filename) {
	this.filename = filename;
	}
	
	@Override
	public boolean parse() throws Exception {
		
		// Parse the XML file
		BDDIVAXMLProcessor divaXML = new BDDIVAXMLProcessor(filename);
		
		// Now go over the experiment structure and parse each of the referenced files
		// by passing them to the FCSProcessor constructor
		// TODO
		
		return true;
	}

	@Override
	public String info() {
		// TODO Auto-generated method stub
		return "Parses 'BD BioSciences FACSDivaª Software' XML files with the associated " +
				"'Data File Standard for Flow Cytometry, Version FCS3.0' files generated " +
				"by the BD LSRFortessa cell analyzer.";
	}

}
