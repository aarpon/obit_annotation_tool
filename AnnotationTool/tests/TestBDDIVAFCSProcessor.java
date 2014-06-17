package tests;

import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.BDFACSDIVAFCSProcessor;

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
