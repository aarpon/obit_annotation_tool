package ch.ethz.scu.obit.at.gui.editors.data;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import ch.ethz.scu.obit.at.gui.editors.data.model.AbstractMetadataMapper;
import ch.ethz.scu.obit.at.gui.viewers.ObserverActionParameters;
import ch.ethz.scu.obit.at.gui.viewers.data.AbstractViewer;
import ch.ethz.scu.obit.at.gui.viewers.data.model.ExperimentNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.OpenBISViewer;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;
import ch.ethz.scu.obit.processors.data.model.Tag;

/**
 * Abstract editor for processors
 * @author Aaron Ponti
 */
abstract public class AbstractEditor extends Observable
implements ActionListener, Observer {

    protected JPanel panel;
    protected JTextArea expTags;

    // Reference to the global settings manager
    protected GlobalSettingsManager globalSettingsManager;

    /**
     * References to the data and openBIS viewers
     */
    protected AbstractViewer dataViewer;
    protected OpenBISViewer openBISViewer;

    // List of experiments from the Data Model
    protected List<ExperimentNode> experiments = null;

    // List of openBISProjects
    protected List<OpenBISProjectNode> openBISProjects = null;

    /**
     * Constructor
     *
     * The inheriting classes must call this constructor.
     *
     * @param dataViewer Reference to the viewer
     * @param openBISViewer Reference to the openBIS viewer
     * @param globalSettingsManager The global settings manager.
     */
    public AbstractEditor(AbstractViewer dataViewer,
            OpenBISViewer openBISViewer,
            GlobalSettingsManager globalSettingsManager) {

        panel = new JPanel();

        this.globalSettingsManager = globalSettingsManager;
        this.dataViewer = dataViewer;
        this.openBISViewer = openBISViewer;

        // Initialize node lists
        experiments = new ArrayList<ExperimentNode>();

        // List of openBISProjects
        openBISProjects = new ArrayList<OpenBISProjectNode>();

        // Set the preferred and minimum size
        panel.setMinimumSize(new Dimension(400, 500));
        panel.setPreferredSize(new Dimension(400, 700));

    }

    /**
     * Clear elements from the editor.
     */
    protected void clearUIElements() {
        // Remove elements and force a redraw of the panel
        if (panel.getComponentCount() > 0) {
            panel.removeAll();
            panel.validate();
            panel.repaint();
        }
    }

    /**
     * Resets the metadata mapping.
     * @param p Action parameters for the Observer.
     */
    abstract protected void resetMetadata(ObserverActionParameters p);

    /**
     * Update the Experiment tags.
     */
    abstract protected void updateExpTags(List<Tag> tagList);

    /**
     * Return the identifier of the project currently selected in the editor.
     * @return
     */
    public abstract String getCurrentProjectIdentifier();

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
                updateUIElements(observerActionParams);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        case FILE_CHANGED:
            try {
                updateUIElements(observerActionParams);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        case READY_TO_SEND:
            // Nothing to do
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

        // Clear the editor
        clearUIElements();

        // Init the metadata
        if (initMetadata()) {

            // Create the widgets
            createUIElements(params);

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
     * Renders all widgets on the panel
     * @param params Action parameters for the Observer.
     * @throws Exception if some openBIS identifiers cannot be computed
     */
    abstract protected void createUIElements(ObserverActionParameters params) throws Exception;

    /**
     * Updates all widgets on the panel
     * @param params Action parameters for the Observer.
     */
    abstract protected void updateUIElements(ObserverActionParameters params);

    /**
     * Once the metadata has been completely filled, this method makes
     * sure to update the data model (generated by the Processor) to be
     * ready to be saved to the XML properties file.
     * @return true if the data model could be updated successfully, false otherwise.
     */
    abstract public boolean updateDataModel();

    /**
     * Return the reference to the JPanel to be added to a container component
     * @return JPanel reference
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Updates the tags in the UI and updates the Experiments.
     * @param tagList List of tags to be processed.
     * @return comma-separated list of tags.
     */
    public void appendTags(List<Tag> tagList)
    {
        // Build the string with a buffer
        StringBuffer buff = new StringBuffer();

        for (Tag tag : tagList) {
            buff.append(tag.toString());
            buff.append("; ");
        }

        // Build the string
        String strTags = buff.toString().trim();
        strTags = strTags.substring(0, strTags.length() - 1);

        // Set current text
        expTags.setText(strTags);

        // Update the Experiment
        updateExpTags(tagList);
    }
}
