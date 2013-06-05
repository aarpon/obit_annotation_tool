package ch.eth.scu.importer.at;

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
		if (!AppProperties.propertiesFileExists()) {
			JOptionPane.showMessageDialog(null,
				    "The application has not ben configured yet. " +
			"Please ask an administrator to do it for you.",
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
