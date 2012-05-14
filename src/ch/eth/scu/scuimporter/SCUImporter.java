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

		FCSProcessor fcsProcessor = new FCSProcessor("/work/fcs/Kymb 090512 Experiment Export/108725.fcs");
		fcsProcessor.parse();
		System.out.println(fcsProcessor);
	}

}
