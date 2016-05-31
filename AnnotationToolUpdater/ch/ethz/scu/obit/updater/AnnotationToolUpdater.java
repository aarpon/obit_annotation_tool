package ch.ethz.scu.obit.updater;

import java.awt.SystemTray;

import ch.ethz.scu.obit.updater.gui.AnnotationToolUpdaterTray;

/**
 * Little tray application to check for AnnotationTool updates.
 * @author Aaron Ponti
 *
 */
public class AnnotationToolUpdater {

	/**
	 * Program entry point
	 * @param args Ignored
	 * @throws Exception if the system tray is not supported.
	 */
	public static void main(String[] args) throws Exception {

        // Check that the system tray is supported
        if (!SystemTray.isSupported()) {
            throw new Exception("SystemTray is not supported");
        }

		// Open the main window and run the scan in the background
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {

				// Launch the UI.
				new AnnotationToolUpdaterTray();

			}
		});
	}
}
