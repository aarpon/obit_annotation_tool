package ch.ethz.scu.obit.at.gui.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import ch.ethz.scu.obit.at.gui.ObserverActionParameters;
import ch.ethz.scu.obit.at.gui.openbis.OpenBISViewer;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;
import ch.ethz.scu.obit.processors.openbis.OpenBISProcessor;

/**
 * Abstract editor for processors
 * @author Aaron Ponti
 */
abstract public class AbstractMetadataManager<T> extends Observable
implements ActionListener, Observer {

    /**
     * References to the data viewer and model
     */
    protected AbstractViewer dataViewer;

    /**
     * References to the openBIS viewer and model
     */
    protected OpenBISProcessor openBISProcessor;
    protected OpenBISViewer openBISViewer;

    /**
     * Set of MetadataMappers
     */
    protected Set<? extends AbstractMetadataMapper> metadataMappers = null;

    /**
     * Constructor
     *
     * The inheriting classes must call this constructor.
     *
     * @param dataViewer Reference to the viewer
     * @param openBISProcessor Reference to the openBIS viewer
     * @param globalSettingsManager The global settings manager.
     */
    public AbstractMetadataManager(AbstractViewer dataViewer,
            OpenBISViewer openBISViewer,
            OpenBISProcessor openBISProcessor,
            GlobalSettingsManager globalSettingsManager) {

        this.dataViewer = dataViewer;
        this.openBISViewer = openBISViewer;
        this.openBISProcessor = openBISProcessor;

    }

    /**
     * Resets the metadata mapping.
     * @param p Action parameters for the Observer.
     */
    abstract protected void resetMetadata(ObserverActionParameters p);

    /**
     * Observer update method
     * @param obs Observable object
     * @param arg Argument
     */
    @Override
    public void update(Observable obs, Object arg) {

        // Get the ObserverAction
        ObserverActionParameters observerActionParams =
                (ObserverActionParameters) arg;

        // Perform the correct action
        switch (observerActionParams.action) {
        case ABOUT_TO_RESCAN:
            try {
                //  sendToOpenBISButton.setEnabled(false);
                resetMetadata(observerActionParams);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        case SCAN_COMPLETE:
            try {
                init(observerActionParams);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        case EXPERIMENT_CHANGED:
            try {
                //                updateUIElements(observerActionParams);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        case FILE_CHANGED:
            try {
                //                updateUIElements(observerActionParams);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        case ABOUT_TO_SCAN_INCREMENTALLY:

            // Disable the send to openBIS button
            //sendToOpenBISButton.setEnabled(false);
            break;

        case INCREMENTAL_SCAN_COMPLETE:

            // Enable the send to openBIS button
            //sendToOpenBISButton.setEnabled(true);
            break;

        case READY_TO_SEND:
            // sendToOpenBISButton.setEnabled(true);
            break;

        default:
            break;
        }
    }

    /**
     * Function that maps the metadata information from the openBIS and
     * data viewers (when they notify being ready) and creates and
     * renders all required UI widgets for metadata editing.
     * @param params Observer action parameters.
     * @throws Exception if some of the openBIS identifiers cannot be computed
     */
    public void init(ObserverActionParameters params) throws Exception {

        // Make sure both viewers have completed their models
        if (!openBISViewer.isReady() || !dataViewer.isReady()) {
            return;
        }

        // Init the metadata
        if (initMetadata()) {

            // Notify observers that the data is ready to be transferred
            synchronized (this) {
                setChanged();
                notifyObservers(new ObserverActionParameters(
                        ObserverActionParameters.Action.READY_TO_SEND, null));
            }

        }

    }

    /**
     * Creates a list of MetadataMappers used to map data entities to
     * openBIS entities (with optionally additional metadata information
     * the user will provide through UI elements in the editor).
     * @throws Exception if initialization of metadata failed.
     * @return bool True if the initialization was successful, false otherwise.
     * @see AbstractMetadataMapper
     */
    abstract protected boolean initMetadata() throws Exception;

    /**
     * Once the metadata has been completely filled, this method makes
     * sure to update the data model (generated by the Processor) to be
     * ready to be saved to the XML properties file.
     * @return true if the data model could be updated successfully, false otherwise.
     */
    abstract public boolean updateDataModel();

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
