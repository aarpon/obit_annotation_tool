package tests;

import ch.eth.scu.importer.proc.processor.BDFACSDIVAFCSProcessor;

public class TestBDDIVAFCSProcessor {

	/**
	 * @param args Application arguments.
	 */
	public static void main(String[] args) {
		
		BDFACSDIVAFCSProcessor processor = 
				new BDFACSDIVAFCSProcessor(
						"/work/openbis/openbis_data/Quality Control MD",
						"pontia");

		processor.parse();
		
		System.out.println(processor.treeView());
		
	}
}
