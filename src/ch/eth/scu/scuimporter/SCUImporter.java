package ch.eth.scu.scuimporter;
import java.io.IOException;
import java.nio.*;

import javax.xml.parsers.ParserConfigurationException;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;

import ch.eth.scu.scuimporter.gui.OpenBISImporter;
import ch.eth.scu.scuimporter.processor.*;

public class SCUImporter {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException {
		// TODO Auto-generated method stub

		String filename;

		int test = 0;
		
		switch (test) {
		
		case 0:
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new OpenBISImporter("LSRFortessa");
				}
			});
			
			break;
			
		case 1:
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new OpenBISImporter("LeicaSP5");
				}
			});
			
			break;
			
		case 10:
			
			// Test the FCSProcessor
			
			filename = "/work/fcs/Kymb 090512 Experiment Export/108725.fcs";
			//String filename = "/work/fcs/Kymb 090512 FCS EXport/96 Well - V bottom/Specimen_001_A10_A10.fcs";
			
			FCSProcessor fcsProcessor = new FCSProcessor(filename, true);
			System.out.println(fcsProcessor.info() );
			System.out.println("");

			fcsProcessor.parse();
			System.out.println(fcsProcessor);
			
			System.out.println("First data value for the first two parameters (row-major order):");
			System.out.println(((FloatBuffer) fcsProcessor.DATA).get());
			System.out.println(((FloatBuffer) fcsProcessor.DATA).get());
			break;
			
		case 11:
			
			// Test the BDDIVAXMLProcessor
			
			filename = "/work/fcs/Kymb 090512 Experiment Export/Kymb 090512.xml";

			BDFACSDIVAXMLProcessor fcsxmlProcessor = new BDFACSDIVAXMLProcessor(filename);
			System.out.println(fcsxmlProcessor.info());
			System.out.println("");

			fcsxmlProcessor.parse();
			System.out.println(fcsxmlProcessor.treeView());
			break;
			
		case 12:
			
			// Test the LeicaLifProcessor
			filename = "/work/lif/Experiment.lif";
			
			LeicaLifProcessor leicaLifProcessor = new LeicaLifProcessor(filename);
			System.out.println(leicaLifProcessor.info());
			System.out.println("");

			try {
				leicaLifProcessor.parse();
			} catch (DependencyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println(leicaLifProcessor);
			
			break;

				
		default:
		
			System.out.println("Choose a test.");
			
		}
	}

}
