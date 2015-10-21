package tests;

import java.io.File;
import java.io.IOException;

import ch.ethz.scu.obit.bdfacsdivafcs.readers.FCSReader;

/**
 * @author pontia
 *
 */
public class TestFCSProcessor {

	/**
	 * @param args  Application arguments.
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// Exported FCS
		//FCSProcessor processor = new FCSProcessor("/work/openbis/openbis_data/Quality Control MD/Kymb 090512/488 and 405 excitation_Tube_001.fcs", false);
		
		// Exported experiment
		//FCSProcessor processor = new FCSProcessor("/work/openbis/openbis_data/Kymb 090512 Experiment Export/108725.fcs", false);
		
		// Exported RCS - Specimen
		//FCSProcessor processor = new FCSProcessor("/work/openbis/openbis_data/Quality Control MD/Kymb 090512/488 and 405 excitation_Tube_001.fcs", false);
		
		// Exported FCS - Tray
		FCSReader processor = new FCSReader(new File("F:/Data/oBIT/March_2015_20150324_001.fcs"), false);
		
		try {
			processor.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(processor.metadataDump());
	
		processor.exportDataToCSV(new File("F:/Data/oBIT/March_2015_20150324_001.cvs"));
		
	}

}
