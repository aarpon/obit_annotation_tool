package ch.eth.scu.scuimporter;

import ch.eth.scu.scuimporter.gui.OpenBISImporterWindow;

/**
 * SCUImporter is an application to drive the import of data from the 
 * acquisition stations into openBIS.
 * @author Aaron Ponti
 *
 */
public class SCUImporter {

	/**
	 * @param args Set to --admin to start in administrator mode 
	 */
	public static void main(String[] args) {

		// Check whether we should start in administrator mode
		if (args.length > 0) {
			if (args[0].equals("--admin")) {
				System.out.println("Startin in administration mode. "
						+ " Just kidding.");
			}
		}
		
		// Open the main window			
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {

				// One of "LSRFortessa" and "LeicaSP5". This will later
				// be obtained from the application properties set in
				// the --admin mode.
				String acqStation = "LSRFortessa";

				new OpenBISImporterWindow(acqStation);
			}
		
		});
	
	}

}
