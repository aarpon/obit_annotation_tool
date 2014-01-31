package ch.eth.scu.importer.at.gui.editors.data;

import javax.swing.JOptionPane;

import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.openbis.OpenBISViewer;
import ch.eth.scu.importer.bdfacsdivafcs.gui.editors.data.BDFACSDIVAFCSEditor;
import ch.eth.scu.importer.common.settings.UserSettingsManager;
import ch.eth.scu.importer.microscopy.gui.editors.data.MicroscopyEditor;

/**
 * The Viewer factory creates a viewer based on the application properties
 * @author Aaron Ponti
 */
public class EditorFactory {

	/**
	 * Creates a data editor depending on the "AcquisitionStation" value in the
	 * application properties.
	 * @param dataViewer reference to a metadata viewer
	 * @param openBISViewer reference to the openBIS viewer
	 * @return a concrete implementation of an AbstractViewer
	 */
	public static AbstractEditor createEditor(AbstractViewer dataViewer,
			OpenBISViewer openBISViewer) {

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

		// Declare an AbstractEditor
		AbstractEditor metadataEditor = null;

		// Create the concrete editor
		String acqStation = manager.getSettingValue("AcquisitionStation");	
		if (acqStation.equals("BD Biosciences Cell Analyzers and Sorters")) {
			metadataEditor = new BDFACSDIVAFCSEditor(dataViewer, openBISViewer);
		} else if (acqStation.equals("Generic light microscopes")) {
			metadataEditor = new MicroscopyEditor(dataViewer, openBISViewer);
		} else {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		}
		
		// Return the created editor
		return metadataEditor;
	}

}
