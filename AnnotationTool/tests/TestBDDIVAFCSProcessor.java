package tests;

import ch.eth.scu.importer.processors.lsrfortessa.BDFACSDIVAFCSProcessor;

public class TestBDDIVAFCSProcessor {

	/**
	 * @param args Application arguments.
	 */
	public static void main(String[] args) {
		
		BDFACSDIVAFCSProcessor processor = 
				new BDFACSDIVAFCSProcessor(
						"/work/data/incoming/pontia/Quality_Control_MD");

		processor.parse();
		
		//System.out.println(processor.treeView());
		
	}
}
