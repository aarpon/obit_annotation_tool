package ch.ethz.scu.obit.at.datamover;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;

/**
 * Moves the user folder to the Datamover incoming folder.
 * 
 * @author Aaron Ponti
 *
 */
public class ATDataMover {

	// Source and target directories
	File sourceDir;
	File targetDir;

	boolean createMarkerDir = false;

	// Secure random number generator
	private SecureRandom random;

	/**
	 * Constructor
	 * 
	 * @param globalSettingsManager The application global settings manager.
	 * @param userName              openBIS user name
	 */
	public ATDataMover(GlobalSettingsManager globalSettingsManager, String userName) {

		// Initialize random number generator
		random = new SecureRandom();

		// Get the user data dir
		sourceDir = new File(globalSettingsManager.getUserDataRootDir() + File.separator + userName);
		if (!sourceDir.isDirectory()) {
			throw new IllegalArgumentException("sourceDir must be a directory!");
		}

		// Get the datamover incoming dir
		targetDir = new File(globalSettingsManager.getDatamoverIncomingDir());
		if (!targetDir.isDirectory()) {
			throw new IllegalArgumentException("sourceDir must be a directory!");
		}

		if (globalSettingsManager.createMarkerFileInDatamoverIncomingFolder().equals("yes")) {
			createMarkerDir = true;
		} else {
			createMarkerDir = false;
		}
	}

	/**
	 * Move sourceDir to targetDir/{uniqueID}/
	 * 
	 * Please notice that this method currently asks the user to decide what to do
	 * in case the move fails with a dialog and allows him to exit the application.
	 *
	 */
	public void move() {

		// Get a unique ID
		String uniqueID = new BigInteger(130, random).toString(32);

		// Create a subfolder in targetDir with the uniqueID as name
		File subFolder = new File(targetDir + File.separator + uniqueID);
		boolean retry = true;
		while (retry) {
			if (!createDir(subFolder)) {
				Object[] options = { "Retry", "Exit" };
				int n = JOptionPane.showOptionDialog(null, "Failed creating " + subFolder + "!\n", "Error",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == 0) {
					continue;
				} else {
					System.exit(1);
				}
			} else {
				retry = false;
			}
		}

		// Full target path
		File fullTarget = new File(subFolder + File.separator + sourceDir.getName());

		// Move
		retry = true;
		while (retry) {

			// Try to move over
			boolean success = true;
			try {
				FileUtils.moveDirectory(sourceDir, fullTarget);
			} catch (IOException e) {
				// Mark failure
				success = false;
			}

			if (success) {
				retry = false;
			} else {

				// Check that moving worked
				if (!fullTarget.exists()) {

					// Make sure the source folder is still there
					if (sourceDir.exists()) {
						Object[] options = { "Retry", "Exit" };
						int n = JOptionPane.showOptionDialog(null,
								"Failed transferring " + sourceDir.getAbsolutePath() + "!\n\n"
										+ "Please make sure that the folder is not open in\n"
										+ "the file manager or that any of the contained\n"
										+ "files are not open in some application.",
								"Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);
						if (n == 0) {
							continue;
						} else {
							System.exit(1);
						}

					} else {

						// Something very BAD happened here. Neither source
						// nor target folders exist!
						JOptionPane.showMessageDialog(null,
								"Failed copying " + sourceDir + " to " + fullTarget + "!\n"
										+ "Please contact your administrator. The application will now exit!",
								"Fatal error!", JOptionPane.ERROR_MESSAGE);
						System.exit(1);

					}
				}

			}
		}

		// If requested, create the .MARKER_is_finished file
		if (this.createMarkerDir == true) {
			File markerFileName = new File(targetDir + File.separator + ".MARKER_is_finished_" + uniqueID);
			boolean success;
			try {
				success = markerFileName.createNewFile();
			} catch (IOException ioe) {
				success = false;
			}

			if (success == false) {
				JOptionPane.showMessageDialog(null,
						"Could not create the marker file to trigger registration!\n"
								+ "Please contact your administrator. The application will now exit!",
						"Fatal error!", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}

		// Re-create user folder
		if (!createDir(sourceDir)) {
			JOptionPane
					.showMessageDialog(
							null, "Failed re-creating user folder!\n"
									+ "Please contact your administrator. The application\n" + "will now exit!",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);

		}
	}

	/**
	 * Create specified directory
	 * 
	 * @param dir full directory path
	 * @return true if the directory could be created successfully (or existed
	 *         already), false otherwise
	 */
	private boolean createDir(File dir) {

		// Does the directory exist already?
		if (dir.exists()) {
			return true;
		}

		// Try creating it
		boolean success = true;
		try {
			if (!dir.mkdirs()) {
				success = false;
			}
		} catch (Exception e) {
			success = false;
		}

		return success;
	}

	/**
	 * Static function to delete a folder recursively.
	 * 
	 * @param folder Folder to be deleted recursively. It folder is a file, it will
	 *               be deleted and the function will return immediately.
	 * @return true if the folder could be deleted successfully, false otherwise.
	 */
	public static boolean deleteRecursively(File folder) {

		// Global state
		boolean state = true;

		// This capture the case that the first call has a
		// file as an argument. In all successive recursive
		// calls the argument is guaranteed to be a folder.
		if (!folder.isDirectory()) {
			return (state && folder.delete());
		}

		// Get a list of files/folders in folder
		File[] files = folder.listFiles();

		// Some virtual machines return null instead of an empty list
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					state = state && deleteRecursively(f);
				} else {
					state = state && f.delete();
				}
			}
		}

		// Now delete the folder itself
		return (state && folder.delete());

	}

}
