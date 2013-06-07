package ch.eth.scu.importer.at.gui.viewers.data;

import java.util.Properties;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.ui_elements.leicasp5.gui.viewers.data.LeicaSP5Viewer;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.BDLSRFortessaFCSViewer;
import ch.eth.scu.importer.ui_elements.nikonnd2.gui.viewers.data.NikonViewer;

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
		Properties appProperties = AppProperties.readPropertiesFromFile();

		// Declare an AbstractViewer
		AbstractViewer metadataViewer = null;
		
		// Return the viewer that fits the "AcquisitionStation"
		String acqStation = appProperties.getProperty("AcquisitionStation");	
		if (acqStation.equals("LSRFortessaFCS")) {
			metadataViewer = new BDLSRFortessaFCSViewer();
		} else if (acqStation.equals("Nikon")) {
			metadataViewer = new NikonViewer();
		} else if (acqStation.equals("LeicaSP5")) {
			metadataViewer = new LeicaSP5Viewer();
		} else {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		}
		
		// Return the created viewer
		return metadataViewer;
	}

}
