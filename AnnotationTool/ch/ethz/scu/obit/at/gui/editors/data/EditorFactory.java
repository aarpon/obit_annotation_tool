package ch.ethz.scu.obit.at.gui.editors.data;

import ch.ethz.scu.obit.at.gui.viewers.data.AbstractViewer;
import ch.ethz.scu.obit.at.gui.viewers.openbis.OpenBISViewer;
import ch.ethz.scu.obit.bdfacsdivafcs.gui.editors.data.BDFACSDIVAFCSEditor;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;
import ch.ethz.scu.obit.microscopy.gui.editors.data.MicroscopyEditor;

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
	public static AbstractEditor createEditor(
			GlobalSettingsManager globalSettingsManager, 
			AbstractViewer dataViewer, OpenBISViewer openBISViewer) {

		// Declare an AbstractEditor
		AbstractEditor metadataEditor = null;

		// Create the concrete editor
		String acqStation = globalSettingsManager.getAcquisitionStation();	
		if (acqStation.equals("BD Biosciences Cell Analyzers and Sorters")) {
			metadataEditor = new BDFACSDIVAFCSEditor(
					dataViewer, openBISViewer, globalSettingsManager);
		} else if (acqStation.equals("Generic light microscopes")) {
			metadataEditor = new MicroscopyEditor(
					dataViewer, openBISViewer, globalSettingsManager);
		} else {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		}
		
		// Return the created editor
		return metadataEditor;
	}

}
