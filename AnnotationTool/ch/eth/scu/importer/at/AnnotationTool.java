package ch.eth.scu.importer.at;

import javax.swing.JOptionPane;

import ch.eth.scu.importer.at.gui.AnnotationToolWindow;
import ch.eth.scu.importer.common.settings.AppSettingsManager;
import ch.eth.scu.importer.common.settings.UserSettingsManager;

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
		
		AppSettingsManager manager = new AppSettingsManager();

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
		
		// Create user setting file
		UserSettingsManager userManager = new UserSettingsManager(
				manager.getSettingsForServer(manager.getServer()));
		if (!userManager.save()) {
			JOptionPane.showMessageDialog(null,
				    "The user settings file could not be created.\n" +
			"Please ask an administrator to check file system permissions.\n\n"
			+ "The application will close now.",
				    "First-time setup",
				    JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}

		
		// Open the main window
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {

				new AnnotationToolWindow();
			}
		
		});
	
	}
	
}
