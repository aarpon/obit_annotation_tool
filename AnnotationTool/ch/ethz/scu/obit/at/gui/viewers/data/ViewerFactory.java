package ch.ethz.scu.obit.at.gui.viewers.data;

import ch.ethz.scu.obit.flow.gui.viewers.data.FlowViewer;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;
import ch.ethz.scu.obit.microscopy.gui.viewers.data.MicroscopyViewer;

/**
 * The Viewer factory creates a viewer based on the application properties
 * @author Aaron Ponti
 */
public class ViewerFactory {

	/**
	 * Creates a data viewer depending on the "AcquisitionStation" value in the
	 * application properties.
	 * @param globalSettingsManager global settings manager.
	 * @return a concrete implementation of an AbstractViewer
	 */
	public static AbstractViewer createViewer(GlobalSettingsManager globalSettingsManager) {

		// Declare an AbstractViewer
		AbstractViewer metadataViewer = null;
		
		// Return the viewer that fits the "AcquisitionStation"
		String acqStation = globalSettingsManager.getAcquisitionStation();	
		if (acqStation.equals("Flow cytometry")) {
			metadataViewer = new FlowViewer(globalSettingsManager);
		} else if (acqStation.equals("Microscopy")) {
			metadataViewer = new MicroscopyViewer(globalSettingsManager);
		} else {
			System.err.println("Unknown acquisition station type! Aborting.");
			System.exit(1);
		}
		
		// Return the created viewer
		return metadataViewer;
	}

}
