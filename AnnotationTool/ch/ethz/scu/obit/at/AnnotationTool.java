package ch.ethz.scu.obit.at;

import javax.swing.JOptionPane;

import ch.ethz.scu.obit.at.gui.AnnotationToolWindow;
import ch.ethz.scu.obit.common.settings.AppSettingsManager;


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

		// Check whether the application has been set up already.
		// If not, we inform the user and quit.
		if (!AppSettingsManager.settingsFileExists()) {
			JOptionPane.showMessageDialog(null,
				    "The application has not ben configured yet.\n" +
			"Please ask an administrator to do it for you.\n\n"
			+ "The application will close now.",
				    "First-time setup",
				    JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
		
		// Read the application settings
		AppSettingsManager manager = new AppSettingsManager();

		// Check
		if (!manager.isFileRead()) {
			JOptionPane.showMessageDialog(null,
				    "The application settings could not be read.\n" +
			"Please ask an administrator to re-configure the "
			+ "application.\n\n"
			+ "The application will close now.",
				    "Obsolete settings",
				    JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}		
		
		if (!manager.isFileCurrent()) {
			JOptionPane.showMessageDialog(null,
				    "The application settings are obsolete.\n" +
			"Please ask an administrator to re-configure the "
			+ "application.\n\n"
			+ "The application will close now.",
				    "Obsolete settings",
				    JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}		

		if (!manager.isFileValid()) {
			JOptionPane.showMessageDialog(null,
				    "The application settings are not valid.\n" +
			"Please ask an administrator to reconfigure the application.\n\n"
			+ "The application will close now.",
				    "First-time setup",
				    JOptionPane.WARNING_MESSAGE);
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
