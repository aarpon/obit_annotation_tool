package tests;

import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor;

public class TestBDDIVAFCSProcessor {

	/**
	 * @param args Application arguments.
	 */
	public static void main(String[] args) {
		
		BDLSRFortessaFCSProcessor processor = 
				new BDLSRFortessaFCSProcessor(
						"/work/data/incoming/pontia/Quality_Control_MD");

		processor.parse();
		
		//System.out.println(processor.treeView());
		
	}
}
