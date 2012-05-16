package ch.eth.scu.scuimporter;
import java.io.IOException;

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
			
			FCSProcessor fcsProcessor = new FCSProcessor(filename);
			System.out.println(fcsProcessor.info() );
			System.out.println("");

			fcsProcessor.parse();
			System.out.println(fcsProcessor);
			break;
			
		case 2:
			
			// Test the FCSXMLProcessor
			
			filename = "/work/fcs/Kymb 090512 Experiment Export/Kymb 090512.xml";
			
			FCSXMLProcessor fcsxmlProcessor = new FCSXMLProcessor(filename);
			System.out.println(fcsxmlProcessor.info());
			System.out.println("");

			fcsxmlProcessor.parse();
			System.out.println(fcsxmlProcessor);
			break;

		default:
		
			System.out.println("Choose a test.");
			
		}
	}

}
