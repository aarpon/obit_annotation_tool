package ch.eth.scu.importer.at.gui.viewers.data;

import javax.swing.JOptionPane;

import ch.eth.scu.importer.bdfacsdivafcs.gui.viewers.data.BDFACSDIVAFCSViewer;
import ch.eth.scu.importer.common.settings.UserSettingsManager;
import ch.eth.scu.importer.microscopy.gui.viewers.data.MicroscopyViewer;

/**
 * The Viewer factory creates a viewer based on the application properties
 * @author Aaron Ponti
 */
public class ViewerFactory {

	/**
	 * Creates a data viewer depending on the "AcquisitionStation" value in the
	 * application properties.
	 * @return a concrete implementation of an AbstractViewer
	 */
	public static AbstractViewer createViewer() {

		// Get the application properties
		UserSettingsManager manager = new UserSettingsManager();
		if (! manager.load()) {
			JOptionPane.showMessageDialog(null,
					"Could not read application settings!\n" +
			"Please contact your administrator. The application\n" +
			"will now exit!",
			"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		// Declare an AbstractViewer
		AbstractViewer metadataViewer = null;
		
		// Return the viewer that fits the "AcquisitionStation"
		String acqStation = manager.getSettingValue("AcquisitionStation");	
		if (acqStation.equals("BD Biosciences Cell Analyzers and Sorters")) {
			metadataViewer = new BDFACSDIVAFCSViewer();
		} else if (acqStation.equals("Generic light microscopes")) {
			metadataViewer = new MicroscopyViewer();
		} else {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		}
		
		// Return the created viewer
		return metadataViewer;
	}

}
