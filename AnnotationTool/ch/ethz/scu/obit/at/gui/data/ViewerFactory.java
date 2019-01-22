package ch.ethz.scu.obit.at.gui.data;

import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;
import ch.ethz.scu.obit.flow.gui.data.FlowViewer;
import ch.ethz.scu.obit.microscopy.gui.data.MicroscopyViewer;
import ch.ethz.scu.obit.processors.openbis.OpenBISProcessor;

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
    public static AbstractViewer createViewer(GlobalSettingsManager globalSettingsManager,
            OpenBISProcessor openBISProcessor) {

        // Declare an AbstractViewer
        AbstractViewer metadataViewer = null;

        // Return the viewer that fits the "AcquisitionStation"
        String acqStation = globalSettingsManager.getAcquisitionStation();
        if (acqStation.equals("Flow cytometry")) {
            metadataViewer = new FlowViewer(globalSettingsManager,
                    openBISProcessor);
        } else if (acqStation.equals("Microscopy")) {
            metadataViewer = new MicroscopyViewer(globalSettingsManager,
                    openBISProcessor);
        } else {
            System.err.println("Unknown acquisition station type! Aborting.");
            System.exit(1);
        }

        // Return the created viewer
        return metadataViewer;
    }

}
