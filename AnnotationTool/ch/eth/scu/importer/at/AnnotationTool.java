package ch.eth.scu.importer.at;

import java.util.Properties;

import javax.swing.JOptionPane;

import ch.eth.scu.importer.at.gui.AnnotationToolWindow;
import ch.eth.scu.importer.common.properties.AppProperties;

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

		Properties appProperties = AppProperties.readPropertiesFromFile();

		if (appProperties == null) {
			JOptionPane.showMessageDialog(null,
				    "The application has not ben configured yet.\n" +
			"Please ask an administrator to do it for you.\n\n"
			+ "The application will close now.",
				    "First-time setup",
				    JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
		
		if (!AppProperties.isPropertiesFileVersionCurrent(appProperties)) {
			JOptionPane.showMessageDialog(null,
				    "The application settings are obsolete.\n" +
			"Please ask an administrator to re-configure the "
			+ "application.\n\n"
			+ "The application will close now.",
				    "Obsolete settings",
				    JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}		

		if (!AppProperties.areAllPropertiesSet(appProperties)) {
			JOptionPane.showMessageDialog(null,
				    "The application has not ben configured yet.\n" +
			"Please ask an administrator to do it for you.\n\n"
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
