package tests;

import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor;

public class TestBDDIVAFCSProcessor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		BDFACSDIVAFCSProcessor processor = new BDFACSDIVAFCSProcessor("/work/openbis/openbis_data/Quality Control MD");

		processor.parse();
		
		System.out.println(processor.treeView());
		
	}
}
