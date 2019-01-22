package ch.ethz.scu.obit.at.gui.data;

import ch.ethz.scu.obit.at.gui.openbis.OpenBISViewer;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;
import ch.ethz.scu.obit.flow.gui.data.FlowMetadataManager;
import ch.ethz.scu.obit.microscopy.gui.data.MicroscopyMetadataManager;
import ch.ethz.scu.obit.processors.openbis.OpenBISProcessor;

/**
 * The Viewer factory creates a viewer based on the application properties
 * @author Aaron Ponti
 */
public class MetadataManagerFactory {

    /**
     * Creates a data editor depending on the "AcquisitionStation" value in the
     * application properties.
     * @param globalSettingsManager The global settings manager.
     * @param dataViewer reference to a metadata viewer
     * @param openBISViewer reference to the openBIS viewer
     * @return a concrete implementation of an AbstractViewer
     */
    public static AbstractMetadataManager<? extends AbstractMetadataMapper> createEditor(
            GlobalSettingsManager globalSettingsManager,
            AbstractViewer dataViewer, OpenBISViewer openBISViewer,
            OpenBISProcessor openBISProcessor) {

        // Declare an AbstractMetadataManager
        AbstractMetadataManager<? extends AbstractMetadataMapper> metadataEditor = null;

        // Create the concrete editor
        String acqStation = globalSettingsManager.getAcquisitionStation();
        if (acqStation.equals("Flow cytometry")) {
            metadataEditor = new FlowMetadataManager(
                    dataViewer, openBISViewer, openBISProcessor, globalSettingsManager);
        } else if (acqStation.equals("Microscopy")) {
            metadataEditor = new MicroscopyMetadataManager(
                    dataViewer, openBISViewer, openBISProcessor, globalSettingsManager);
        } else {
            System.err.println("Unknown acquisition station type! Aborting.");
            System.exit(1);
        }

        // Return the created editor
        return metadataEditor;
    }

}
