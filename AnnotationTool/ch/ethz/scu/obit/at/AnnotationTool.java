package ch.ethz.scu.obit.at;

import javax.swing.JOptionPane;

import ch.ethz.scu.obit.at.gui.AnnotationToolWindow;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;


/**
 * AnnotationTool is an application to drive the import of data from the 
 * acquisition stations into openBIS.
 * @author Aaron Ponti
 */
public class AnnotationTool {

	/**
	 * @param args Ignored 
	 */
	public static void main(String[] args) {

		
		// Check whether the application has been set up properly and it is ready
		// to be used.
		try {
			GlobalSettingsManager.isConfigurationValid();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), 
					"Initialization error", JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}

		// Open the main window and run the scan in the background
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {

				// Create the Window and scan user's data folder and
				// openBIS structure.
				final AnnotationToolWindow w = new AnnotationToolWindow();

				// Scan data folder and openBIS
				w.scan();

			}
		});

	}
	
}
