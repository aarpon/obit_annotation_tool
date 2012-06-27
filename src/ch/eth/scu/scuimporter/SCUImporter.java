package ch.eth.scu.scuimporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import ch.eth.scu.scuimporter.gui.OpenBISImporterWindow;
import ch.eth.scu.scuimporter.gui.dialogs.openBISImporterAdminDialog;
import ch.eth.scu.scuimporter.properties.AppProperties;

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

		// Initialize boolean mode to false
		boolean adminMode = false;
		
		// Check whether the user asked to start in administrator mode
		if (args.length > 0) {
			if (args[0].equals("--admin")) {
				adminMode = true;
				System.out.println("Starting in admin mode.");
			}
		}

		// Check whether we need to set up the application (first-time run)
		// TODO Do not run this automatically!
		if (AppProperties.propertiesFileExists() == false) {
			adminMode = true;
		}

		// If needed, start the administrator dialog (modal)
		if (adminMode == true) {
			JOptionPane.showMessageDialog(null,
				    "The administrator panel will now launch. " +
			"Please make sure to have administrator rights.",
				    "First-time setup",
				    JOptionPane.WARNING_MESSAGE);
			new openBISImporterAdminDialog();
		}
		
		// Open the main window
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {

				new OpenBISImporterWindow();
			}
		
		});
	
	}
	
}
