package ch.eth.scu.importer.at.gui.viewers.data;

import java.util.Properties;

import ch.eth.scu.importer.bdfacsdivafcs.gui.viewers.data.BDFACSDIVAFCSViewer;
import ch.eth.scu.importer.common.settings.AppSettingsManager;
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
		Properties appProperties = AppSettingsManager.readSettingsFromFile();

		// Declare an AbstractViewer
		AbstractViewer metadataViewer = null;
		
		// Return the viewer that fits the "AcquisitionStation"
		String acqStation = appProperties.getProperty("AcquisitionStation");	
		if (acqStation.equals("BD LSRFortessa cell analyzer") ||
				acqStation.equals("BD FACSAria III cell sorter")) {
			metadataViewer = new BDFACSDIVAFCSViewer();
		} else if (acqStation.equals("Generic light microscope")) {
			metadataViewer = new MicroscopyViewer();
		} else {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		}
		
		// Return the created viewer
		return metadataViewer;
	}

}
