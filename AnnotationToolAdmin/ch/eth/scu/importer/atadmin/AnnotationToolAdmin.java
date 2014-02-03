package ch.eth.scu.importer.atadmin;

import ch.eth.scu.importer.atadmin.gui.dialogs.AnnotationToolAdminDialog;

/**
 * SCUImporter is an application to drive the import of data from the 
 * acquisition stations into openBIS.
 * @author Aaron Ponti
 */
public class AnnotationToolAdmin {

	/**
	 * @param args Ignored 
	 */
	public static void main(String[] args) {
		
		// Open the main window
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {

				new AnnotationToolAdminDialog();
			}
		
		});
	
	}
	
}
