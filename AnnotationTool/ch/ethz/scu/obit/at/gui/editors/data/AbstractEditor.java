package ch.ethz.scu.obit.at.gui.editors.data;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.tree.TreeModel;

import ch.ethz.scu.obit.at.gui.editors.data.model.AbstractMetadataMapper;
import ch.ethz.scu.obit.at.gui.viewers.ObserverActionParameters;
import ch.ethz.scu.obit.at.gui.viewers.data.AbstractViewer;
import ch.ethz.scu.obit.at.gui.viewers.openbis.OpenBISViewer;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.AbstractOpenBISNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;

/**
 * Abstract editor for processors
 * @author Aaron Ponti
 */
abstract public class AbstractEditor extends Observable
implements ActionListener, Observer {

    protected JPanel panel;

    // Reference to the global settings manager
    protected GlobalSettingsManager globalSettingsManager;

    /**
     * References to the data viewer and model
     */
    protected AbstractViewer dataViewer;
    protected TreeModel dataModel;

    /**
     * References to the openBIS viewer and model
     */
    protected OpenBISViewer openBISViewer;
    protected TreeModel openBISModel;

    // List of openBISProjects
    protected List<OpenBISProjectNode> openBISProjects =
            new ArrayList<OpenBISProjectNode>();

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
     * Collects and stores openBIS projects for mapping
     */
    protected void storeOpenBISProjects() {

        // Store the openBIS model
        openBISModel = openBISViewer.getDataModel();

        // We extract all projects from the openBIS model and create a list
        // with which we will then create JComboBox associated to each project
        // from the data model
        openBISProjects = new ArrayList<OpenBISProjectNode>();

        AbstractOpenBISNode openBISRoot =
                (AbstractOpenBISNode) openBISModel.getRoot();

        // Iterate over the space nodes (there should be at least one)
        for (int i = 0; i < openBISRoot.getChildCount(); i++) {

            // Get the Space
            AbstractOpenBISNode openBISSpaceNode =
                    (AbstractOpenBISNode) openBISRoot.getChildAt(i);

            // Go over the child Projects
            int n = openBISSpaceNode.getChildCount();

            for (int j = 0; j < n; j++) {

                // Get the OpenBISProjectNode
                OpenBISProjectNode openBISProjectNode =
                        (OpenBISProjectNode) openBISSpaceNode.getChildAt(j);

                // Add it to the list: we wrap it into a wrapper
                // class to override the toString() method; we do
                // this because in constrast to what happens in the
                // openBIS viewer, here we need the (openBIS) identifier
                //  instead of the code.
                openBISProjects.add(openBISProjectNode);

            }
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
     * Clean a comma-separated list of tags and remove duplicates.
     * @param tagList List of tags to be processed.
     * @return comma-separated list of tags.
     */
    protected String cleanTagList(String tagList) {

        // Split the string into items
        List<String> items = Arrays.asList(tagList.split("\\s*,\\s*"));

        // Use a set to test for duplicates
        Set<String> s = new LinkedHashSet<String>();

        // Loop over list of items
        for (String item : items) {
            if (!s.contains(item)) {
                s.add(item);
            }
        }

        // Build the string with a buffer
        StringBuffer buff = new StringBuffer();

        for (Iterator<String> it = s.iterator(); it.hasNext();) {
            buff.append(it.next());
            buff.append(" ");
        }

        // Return the string
        return buff.toString().trim().replaceAll(" ", ", ");
    }

    /**
     * Return the default target openBIS project as set in the User settings
     * or the first returned project from openBIS if none is set.
     * @return openBISProject node.
     * @throws Exception if the openBIS server nodes have not been retrieved yet.
     */
    public OpenBISProjectNode getDefaultProjectOrFirst() throws Exception {

        // This method must be called after the openBIS server nodes
        // have been retrieved.
        if (openBISProjects.size() == 0) {
            // No projects retrieved/found!
            return null;
        }

        // Retrieve the default target project from the User settings or
        // revert to the first project in the list if none is set.
        OpenBISProjectNode defaultProjectNode = null;
        String defaultProject = globalSettingsManager.getDefaultProject();
        if (defaultProject.equals("")) {
            defaultProjectNode = openBISProjects.get(0);
        } else {
            for (OpenBISProjectNode current : openBISProjects) {
                if (current.getIdentifier().equals(defaultProject)) {
                    defaultProjectNode = current;
                    break;
                }
            }
            if (defaultProjectNode == null) {
                // The stored default project does not exist!
                // Fallback to the first one in the list.
                defaultProjectNode = openBISProjects.get(0);
            }
        }

        return defaultProjectNode;
    }
}
