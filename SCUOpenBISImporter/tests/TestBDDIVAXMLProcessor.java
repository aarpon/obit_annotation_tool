package tests;

import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor;

public class TestBDDIVAXMLProcessor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		BDFACSDIVAXMLProcessor processor = 
				new BDFACSDIVAXMLProcessor(
						"/work/openbis/openbis_data/Kymb 090512 Experiment Export/Kymb 090512.xml",
						"pontia");

		processor.parse();
		
		System.out.println(processor.treeView());
		
	}
}
