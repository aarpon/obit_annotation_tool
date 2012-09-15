package ch.eth.scu.importer.gui.editors.data;

import java.util.Properties;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.gui.viewers.openbis.OpenBISSpaceViewer;

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
			OpenBISSpaceViewer openBISViewer) {

		// Get the application properties
		Properties appProperties = AppProperties.readPropertiesFromFile();

		// Declare an AbstractEditor
		AbstractEditor metadataEditor = null;

		// Create the concrete editor
		String acqStation = appProperties.getProperty("AcquisitionStation");	
		if (acqStation.equals("LSRFortessaFCS")) {
			metadataEditor = new BDLSRFortessaFCSEditor(dataViewer, openBISViewer);
		} else if (acqStation.equals("LeicaSP5")) {
			metadataEditor = new LeicaSP5Editor(dataViewer, openBISViewer);
		} else {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		}
		
		// Return the created editor
		return metadataEditor;
	}

}
