package ch.ethz.scu.obit.common.help;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * Simple class to display an HTML help file in the system browser.
 * 
 * From the calling class, use:
 * 
 * 		URI fileURI = this.getClass().getResource("html/helpFile.html").toURI();
 *      HelpViewer.show(fileURI);
 *      
 * provided "helpFile.html" is in a subfolder 'html' within the folder
 * containing the calling java class file. 
 * 
 * @author Aaron Ponti
 *
 */
public class HelpViewer {

	/**
	 * Static method to display the html file in the system browser.
	 * @param fileURI URI of the html file to be displayed.
	 */
	public static void show(URI fileURI) {

		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(fileURI);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
