package ch.eth.scu.scuimporter;
import java.io.IOException;

import ch.eth.scu.scuimporter.processor.*;

public class SCUImporter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String filename = "/work/fcs/Kymb 090512 Experiment Export/108725.fcs";
		//String filename = "/work/fcs/Kymb 090512 FCS EXport/96 Well - V bottom/Specimen_001_A10_A10.fcs";
		
		FCSProcessor fcsProcessor = new FCSProcessor(filename);
		fcsProcessor.parse();
		System.out.println(fcsProcessor);
	}

}
