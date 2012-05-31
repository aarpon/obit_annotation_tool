package ch.eth.scu.scuimporter;
import java.io.IOException;
import java.nio.*;

import javax.xml.parsers.ParserConfigurationException;

import ch.eth.scu.scuimporter.processor.*;

public class SCUImporter {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException {
		// TODO Auto-generated method stub

		int test = 2;
		String filename;
		
		switch (test) {
		
		case 1:
			
			// Test the FCSProcessor
			
			filename = "/work/fcs/Kymb 090512 Experiment Export/108725.fcs";
			//String filename = "/work/fcs/Kymb 090512 FCS EXport/96 Well - V bottom/Specimen_001_A10_A10.fcs";
			
			FCSProcessor fcsProcessor = new FCSProcessor(filename, true);
			System.out.println(fcsProcessor.info() );
			System.out.println("");

			fcsProcessor.parse();
			System.out.println(fcsProcessor);
			
			System.out.println("First data value for the first two parameters (row-major order):");
			System.out.println(((FloatBuffer) fcsProcessor.DATA).get());
			System.out.println(((FloatBuffer) fcsProcessor.DATA).get());
			break;
			
		case 2:
			
			// Test the BDDIVAXMLProcessor
			
			filename = "/work/fcs/Kymb 090512 Experiment Export/Kymb 090512.xml";
			
			BDDIVAXMLProcessor fcsxmlProcessor = new BDDIVAXMLProcessor(filename);
			System.out.println(fcsxmlProcessor.info());
			System.out.println("");

			fcsxmlProcessor.parse();
			System.out.println(fcsxmlProcessor.treeView());
			break;

		default:
		
			System.out.println("Choose a test.");
			
		}
	}

}
