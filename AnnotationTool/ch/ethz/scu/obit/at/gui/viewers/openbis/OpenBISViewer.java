package ch.ethz.scu.obit.at.gui.viewers.openbis;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ch.ethz.scu.obit.at.gui.pane.OutputPane;
import ch.ethz.scu.obit.at.gui.viewers.ObserverActionParameters;
import ch.ethz.scu.obit.at.gui.viewers.openbis.dialogs.OpenBISCreateTagDialog;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.AbstractOpenBISNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISExperimentNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISSampleNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISSpaceNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISUserNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.view.OpenBISViewerTree;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;
import ch.ethz.scu.obit.common.utils.QueryOS;
import ch.ethz.scu.obit.processors.data.model.Tag;
import ch.ethz.scu.obit.processors.data.model.TagListExportTransferHandler;
import ch.ethz.scu.obit.processors.openbis.OpenBISProcessor;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;

/**
 * Graphical user interface to log in to openBIS and choose where to store
 * acquired datasets.
 *
 * @author Aaron Ponti
 */
public class OpenBISViewer extends Observable implements ActionListener,
TreeSelectionListener, TreeWillExpandListener {

    protected JPanel panel;
    protected JButton scanButton;
    protected JLabel userTags;
    protected JList<Tag> userTagList;

    private GlobalSettingsManager globalSettingsManager;
    private OpenBISProcessor openBISProcessor;
    private OpenBISCreateTagDialog createTagDialog;

    private OpenBISUserNode userNode;
    private OpenBISViewerTree tree;
    private String defaultRootNodeString = "/";

    // Keep track of the last visited TreePath to prevent multiple firing
    // of treeWillExpand() and last selected node to prevent processing
    // a valueChanged event twice.
    private TreePath lastVisitedPath = null;
    private AbstractOpenBISNode lastSelectedNode = null;

    private boolean isReady = false;

    protected OutputPane outputPane;

    String loginErrorMessage = "";
    String loginErrorTitle = "";
    boolean loginErrorRecoverable = true;

    /**
     * Constructor
     *
     * @param openBISProcessor      openBIS Processor
     * @param outputPane            output pane
     * @param globalSettingsManager global settings manager
     */
    public OpenBISViewer(OpenBISProcessor openBISProcessor,
            OutputPane outputPane,
            GlobalSettingsManager globalSettingsManager) {

        // Store the global settings manager
        this.globalSettingsManager = globalSettingsManager;

        // Store the OpenBISProcessor reference
        this.openBISProcessor = openBISProcessor;

        // Store the OutputPane reference
        this.outputPane = outputPane;

        // Create a panel
        panel = new JPanel();

        // Set a grid bag layout
        panel.setLayout(new GridBagLayout());

        // Common constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;

        // Add a title JLabel
        JLabel title = new JLabel("<html><b>openBIS</b></html>");

        // Add the tree viewer to the layout
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(0, 5, 5, 0);
        panel.add(title, constraints);

        // Create a splitpane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                openBISViewerPanel(), tagsPanel());
        splitPane.setResizeWeight(0.75);
        splitPane.setBorder(null);

        // Add the split panel to the layout
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(0, 0, 0, 0);
        panel.add(splitPane, constraints);

        // Set sizes
        panel.setMinimumSize(new Dimension(400, 500));
        panel.setPreferredSize(new Dimension(400, 700));
    }

    /**
     * Return the OpenBISProcessor.
     *
     * @return the OpenBISProcessor.
     */
    public OpenBISProcessor getOpenBISProcessor() {
        return openBISProcessor;
    }

    /**
     * Return the default target openBIS project as set in the User settings or
     * the first returned project from openBIS if none is set.
     *
     * @return openBISProject node or null if there are no projects in openBIS.
     */
    public OpenBISProjectNode getDefaultProjectOrFirst() {

        // This method must be called after the openBIS server nodes
        // have been retrieved.
        List<OpenBISProjectNode> openBISProjects = getOpenBISProjectNodes();

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

    /**
     * Return a list of OpenBISProjectNodes from the data model.
     *
     * @return List of OpenBISProjectNode objects.
     */
    public List<OpenBISProjectNode> getOpenBISProjectNodes() {

        // Store the openBIS model
        TreeModel openBISModel = getDataModel();

        // We extract all projects from the openBIS model and create a list
        // with which we will then create JComboBox associated to each project
        // from the data model
        List<OpenBISProjectNode> openBISProjects = new ArrayList<OpenBISProjectNode>();

        AbstractOpenBISNode openBISRoot = (AbstractOpenBISNode) openBISModel
                .getRoot();

        // Iterate over the space nodes (there should be at least one)
        for (int i = 0; i < openBISRoot.getChildCount(); i++) {

            // Get the Space
            AbstractOpenBISNode openBISSpaceNode = (AbstractOpenBISNode) openBISRoot
                    .getChildAt(i);

            // Go over the child Projects
            int n = openBISSpaceNode.getChildCount();

            for (int j = 0; j < n; j++) {

                // Get the OpenBISProjectNode
                OpenBISProjectNode openBISProjectNode = (OpenBISProjectNode) openBISSpaceNode
                        .getChildAt(j);

                // Add it to the list.
                openBISProjects.add(openBISProjectNode);

            }
        }

        return openBISProjects;
    }

    /**
     * Returns the user name if successfully logged in, empty string otherwise
     *
     * @return user name or empty String if log on was not successful
     */
    public String getUserName() {
        if (!openBISProcessor.isLoggedIn()) {
            return "";
        }
        return openBISProcessor.getUserName();
    }

    /**
     * Called when selection in the Tree View is changed.
     *
     * @param e A TreeSelectionEvent
     */
    @Override
    public void valueChanged(TreeSelectionEvent e) {

        // Get the selected tree node
        AbstractOpenBISNode node = (AbstractOpenBISNode) tree
                .getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        // The valuedChanged() method is called twice when the a node is
        // chosen in the tree. Workaround: do not process the same node
        // twice in a row
        if (node == lastSelectedNode) {
            return;
        }
        lastSelectedNode = node;

        // If the Node is a space, refresh the tags
        // Get the node object
        Object nodeInfo = node.getUserObject();

        // Print the attributes
        String className = nodeInfo.getClass().getSimpleName();
        if (className.equals("Space") || className.equals("Project")) {

            Space space = null;
            if (className.equals("Space")) {
                // Get the space
                space = (Space) nodeInfo;

            } else {

                // Get the Space from the parent node
                OpenBISSpaceNode spaceNode = (OpenBISSpaceNode) node
                        .getParent();
                space = (Space) spaceNode.getUserObject();

            }

            // Retrieve tags for current space
            List<Sample> tags = openBISProcessor.getTagsForSpace(space);

            // Clear the tag list
            clearTagList();

            // Add the new tags to the list
            setTagList(tags, space);
        }

    }

    /**
     * Return the Tree's data model.
     *
     * @return data model tree
     */
    public TreeModel getDataModel() {
        return tree.getModel();
    }

    /**
     * Clear the tree view
     */
    private void clearTree() {

        // Is there already something in the tree?
        if (tree == null) {
            return;
        }

        // Clear the tree model
        TreeModel model = tree.getModel();
        if (model != null) {
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model
                    .getRoot();
            if (rootNode != null) {
                rootNode.removeAllChildren();
                ((DefaultTreeModel) model).reload();
                rootNode = null;
            }
        }

        // Create the root node
        userNode = new OpenBISUserNode(defaultRootNodeString);
        tree.setModel(new DefaultTreeModel(userNode));

        // Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        // Listen for when a node is about to be opened (for lazy loading)
        tree.addTreeWillExpandListener(this);
    }

    /**
     * Fill the tree view with the data obtained from openBIS
     */
    public void scan() {

        OpenBISSpaceNode space;
        OpenBISProjectNode project;

        // Inform
        outputPane.log("Retrieving openBIS structure...");

        // Clear the tree
        clearTree();

        // Notify observers that the scanning is about to start
        synchronized (this) {
            setChanged();
            notifyObservers(new ObserverActionParameters(
                    ObserverActionParameters.Action.ABOUT_TO_RESCAN, null));
        }

        // Do we have a connection with openBIS?
        // We just need an active facade for scanning; the queryFacade
        // should actually be on as well, but we do not need it here.
        if (!openBISProcessor.isLoggedIn()) {
            return;
        }

        // Check that the session is still open (we just check the
        // facade, the queryFacade is not necessary
        if (!openBISProcessor.isLoggedIn()) {
            JOptionPane.showMessageDialog(this.panel,
                    "The openBIS session is no longer valid!\n"
                            + "Please try logging in again.",
                            "Session error", JOptionPane.ERROR_MESSAGE);
            clearTree();
            return;
        }

        // Disable the "scan" button
        scanButton.setEnabled(false);

        // Set the root of the tree
        userNode = new OpenBISUserNode(openBISProcessor.getUserName());

        // Get spaces
        SearchResult<Space> spaces = openBISProcessor.retrieveAndCacheSpaces();
        if (spaces.getTotalCount() == 0) {
            JOptionPane.showMessageDialog(this.panel,
                    "Sorry, there are no (accessible) spaces.\n\n"
                            + "Please ask your administrator to create a "
                            + "space for you or to grant you access to an "
                            + "existing one.\nNo data registration will be "
                            + "possible until this issue is fixed.",
                            "Warning", JOptionPane.WARNING_MESSAGE);
            // We do not need to return, this case is treated below
        }

        // Keep a count of the usable projects
        int nProjects = 0;

        for (Space s : spaces.getObjects()) {

            // Add the space
            space = new OpenBISSpaceNode(s);
            userNode.add(space);

            // Get the projects for current space
            List<Project> projects = openBISProcessor.getProjectsForSpace(s);

            // We add the projects -- experiments and samples will be
            // lazily loaded on node expansion
            for (Project p : projects) {

                // We keep but do not display projects with code
                // "COMMON_ORGANIZATION_UNITS", since those store the
                // shared tags for the space.
                if (p.getCode().equals("COMMON_ORGANIZATION_UNITS")) {
                    continue;
                }
                // Add the project
                project = new OpenBISProjectNode(p);
                space.add(project);
                nProjects++;
            }

        }

        // Update the view
        tree.setModel(new DefaultTreeModel(userNode));

        // Listen for when the selection changes
        tree.addTreeSelectionListener(this);

        // Listen for when a node is about to be opened (for lazy loading)
        tree.addTreeWillExpandListener(this);

        if (nProjects > 0) {

            // Set isReady to true
            isReady = true;

            // Notify observers that the scanning is done
            synchronized (this) {
                setChanged();
                notifyObservers(new ObserverActionParameters(
                        ObserverActionParameters.Action.SCAN_COMPLETE, null));
            }
        } else {
            JOptionPane.showMessageDialog(this.panel,
                    "Sorry, there are no (accessible) projects.\n\n"
                            + "You will need to create one yourself or "
                            + "ask your space administrator to do it "
                            + "for you.\nNo data registration will be "
                            + "possible until this issue is fixed.",
                            "Warning", JOptionPane.WARNING_MESSAGE);
            // We do not need to return, this case is treated below

        }

        // Clear the tag list
        clearTagList();

        // Set the tags for the default space/project
        setTagListFromOpenBISProjectNode(getDefaultProjectOrFirst());

        // Re-enable the "scan" button
        scanButton.setEnabled(true);

        // Inform
        outputPane.log("Retrieving openBIS structure completed.");
    }

    /**
     * Returns true if the viewer has completed creation of the data model
     *
     * @return true if the data model is complete, false otherwise
     */
    public boolean isReady() {
        return isReady;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Currently we do not do anything.
    }

    /**
     * Return the reference to the JPanel to be added to a container component
     *
     * @return JPanel reference
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Called when a node in the Tree is about to expand.
     *
     * @param event A TreeExpansionEvent. Required by TreeWillExpandListener
     *              interface.
     */
    @Override
    public void treeWillExpand(TreeExpansionEvent event)
            throws ExpandVetoException {
        TreePath path = event.getPath();
        if (path == lastVisitedPath) {
            return;
        } else {
            lastVisitedPath = path;
        }
        loadLazyChildren((AbstractOpenBISNode) path.getLastPathComponent());
    }

    /**
     * Called when a node in the Tree is about to collapse.
     *
     * @param event A TreeExpansionEvent. Required by TreeWillExpandListener
     *              interface.
     */
    @Override
    public void treeWillCollapse(TreeExpansionEvent event)
            throws ExpandVetoException {
        // We do nothing
    }

    /**
     * Load the childen of the specified node if needed.
     *
     * @param node Node to query for children.
     */
    private synchronized void loadLazyChildren(AbstractOpenBISNode node) {

        // If the node children were loaded already, we just return
        if (node.isLoaded()) {
            return;
        }

        // Get the user object stored in the node
        Object obj = node.getUserObject();

        // Which openBIS object did we get?
        String className = obj.getClass().getSimpleName();

        // Proceed with the loading
        if (className.equals("Project")) {

            // If we have a Project, we load the contained Experiments
            Project p = (Project) obj;
            List<String> projId = new ArrayList<String>();
            projId.add(p.getIdentifier().toString());

            // Then define and start the worker
            class Worker extends SwingWorker<List<Experiment>, Void> {

                final private OpenBISProcessor o;
                final private List<String> pId;
                final private AbstractOpenBISNode n;
                final private Project p;

                /**
                 * Constructor.
                 *
                 * @param o   OpenBISProcessor reference.
                 * @param eId List of Experiment identifiers.
                 * @param n   Node to which to add the Experiment nodes.
                 * @param p   Project reference.
                 */
                public Worker(OpenBISProcessor o, List<String> pId,
                        AbstractOpenBISNode n, Project p) {
                    this.o = o;
                    this.pId = pId;
                    this.n = n;
                    this.p = p;
                }

                @Override
                public List<Experiment> doInBackground() {

                    return (o.getExperimentsForProjects(p));

                }

                @Override
                public void done() {

                    // Initialize Sample list
                    List<Experiment> experiments = new ArrayList<Experiment>();

                    // Retrieve the result
                    try {
                        experiments = get();
                    } catch (InterruptedException | ExecutionException e) {
                        experiments = new ArrayList<Experiment>();
                    }

                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                    for (Experiment e : experiments) {

                        // We keep the experiment with code
                        // "ORGANIZATION_UNITS_COLLECTION"
                        // but we do not add it to the tree since it contains
                        // the user tags.
                        if (e.getCode()
                                .equals("ORGANIZATION_UNITS_COLLECTION")) {
                            continue;
                        }

                        // Add the experiments
                        OpenBISExperimentNode experiment = new OpenBISExperimentNode(
                                e);
                        model.insertNodeInto(experiment, n, n.getChildCount());
                    }

                    // Inform
                    outputPane.log("Retrieved list of experiments for project "
                            + p.getIdentifier() + ".");

                    // Mark the node as loaded
                    n.setLoaded();

                }

            }
            ;

            // Run the worker!
            (new Worker(openBISProcessor, projId, node, p)).execute();

        } else if (className.equals("Experiment")) {

            // If we have an Experiment, we load the contained Samples
            Experiment e = (Experiment) obj;
            List<String> experimentId = new ArrayList<String>();
            experimentId.add(e.getIdentifier().toString());

            // To be restored -- and extended -- in the future.

            // EnumSet<SampleFetchOption> opts =
            // EnumSet.of(SampleFetchOption.PROPERTIES);
            // List<Sample> samples =
            // facade.listSamplesForExperiments(experimentID, opts);
            // for (Sample sm : samples) {
            // // Add the samples
            // OpenBISSampleNode sample = new OpenBISSampleNode(sm);
            // node.add(sample);
            // }

            // Temporarily, we just display the number of contained
            // samples in the Experiment. Later we could re-enable this
            // if we decided to provided specialized versions of the
            // openBIS Viewer/Processor.

            // Then define and start the worker
            class Worker extends SwingWorker<List<Sample>, Void> {

                final private OpenBISProcessor o;
                final private List<String> eId;
                final private AbstractOpenBISNode n;
                final private Experiment e;

                /**
                 * Constructor.
                 *
                 * @param o   OpenBISProcessor reference.
                 * @param eId List of Experiment identifiers.
                 * @param n   Node to which to add the Sample nodes.
                 * @param e   Experiment reference.
                 */
                public Worker(OpenBISProcessor o, List<String> eId,
                        AbstractOpenBISNode n, Experiment e) {
                    this.o = o;
                    this.eId = eId;
                    this.n = n;
                    this.e = e;
                }

                @Override
                public List<Sample> doInBackground() {

                    return (o.getSamplesForExperiments(this.e));

                }

                @Override
                public void done() {

                    // Initialize Sample list
                    List<Sample> samples = new ArrayList<Sample>();

                    // Retrieve the result
                    try {
                        samples = get();
                    } catch (InterruptedException | ExecutionException e) {
                        samples = new ArrayList<Sample>();
                    }

                    if (samples.size() > 0) {

                        // Get the model
                        DefaultTreeModel model = (DefaultTreeModel) tree
                                .getModel();

                        // Append the experiment samples
                        for (Sample sample : samples) {

                            // Is it an EXPERIMENT sample?
                            if (sample.getType().toString()
                                    .endsWith("_EXPERIMENT")) {

                                String nodeName = sample.getCode();
                                if (sample.getProperties()
                                        .containsKey("$NAME")) {
                                    nodeName = sample.getProperties()
                                            .get("$NAME");
                                }

                                // Add to the tree
                                model.insertNodeInto(
                                        new OpenBISSampleNode(sample), n,
                                        n.getChildCount());

                            }

                        }

                    }

                    // Inform
                    outputPane.log("Retrieved number of samples for experiment "
                            + e.getIdentifier() + ".");

                    // Mark the node as loaded
                    n.setLoaded();
                }

            }
            ;

            // Run the worker!
            (new Worker(openBISProcessor, experimentId, node, e)).execute();

        } else {

            // Mark the node as loaded (in any case)
            node.setLoaded();
        }

    }

    /**
     * Sets a mouse event listener on the JTree
     *
     * @param e Mouse event
     */
    private void setListenerOnJTree(MouseEvent e) {

        if (e.isPopupTrigger()
                && e.getComponent() instanceof OpenBISViewerTree) {

            // Position of mouse click
            int x = e.getPoint().x;
            int y = e.getPoint().y;

            // Get selected node
            TreePath p = tree.getPathForLocation(x, y);
            if (p == null) {
                // There is nothing usable at that location
                return;
            }
            AbstractOpenBISNode node = (AbstractOpenBISNode) p
                    .getLastPathComponent();

            // Type of node
            String nodeType = node.getClass().getSimpleName();

            // Add relevant context menu
            if (nodeType.equals("OpenBISSpaceNode")) {
                JPopupMenu popup = createSpacePopup((OpenBISSpaceNode) node);
                popup.show(e.getComponent(), x, y);
            } else if (nodeType.equals("OpenBISProjectNode")) {
                JPopupMenu popup = createProjectPopup(
                        (OpenBISProjectNode) node);
                popup.show(e.getComponent(), x, y);
            } else {
                // Nothing to do.
            }
        }
    }

    /**
     * Create a popup menu with actions for a space node
     *
     * @param node OpenBIS Space node to which the popup menu is associated.
     * @return a JPopupMenu for the passed item
     */
    private JPopupMenu createSpacePopup(final OpenBISSpaceNode node) {

        // Create the popup menu.
        JPopupMenu popup = new JPopupMenu();

        // Create new project
        JMenuItem createProjectMenuItem = new JMenuItem("Create new project");
        createProjectMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (createNewProject(node)) {
                    // Rescan
                    scan();
                }
            }
        });
        popup.add(createProjectMenuItem);

        // Create new tag
        JMenuItem createTagMenuItem = new JMenuItem("Create new tag");
        createTagMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                createNewTag(node);
            }
        });
        popup.add(createTagMenuItem);

        return popup;
    }

    /**
     * Create a popup menu with actions for a project node
     *
     * @param node OpenBIS Project node to which the popup menu is associated.
     * @return a JPopupMenu for the passed item
     */
    private JPopupMenu createProjectPopup(final OpenBISProjectNode node) {

        // Create the popup menu.
        JPopupMenu popup = new JPopupMenu();

        // Create new project
        String menuEntry = "Set as default target project";
        JMenuItem menuItem = new JMenuItem(menuEntry);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Set the project with given identified as default target
                if (setAsDefaultProject(node.getIdentifier())) {
                    outputPane.log("Project " + node.getIdentifier() + " successfully set as default.");
                } else {
                    outputPane.err("Project could not be set as default.");
                }
            }
        });
        popup.add(menuItem);

        return popup;
    }

    /**
     * Asks the user to give a project name and will then try to create it as a
     * child of the passed OpenBISSpaceNode
     *
     * @param node An OpenBISSpaceNode
     * @return true if creation was successfull, false otherwise.
     */
    private boolean createNewProject(final OpenBISSpaceNode node) {

        // Retrieve and store the createProject service
        if (!openBISProcessor.isLoggedIn()) {

            // TODO Throw an exception to distinguish the case where
            // the project could not be created.
            return false;

        }

        // Get the space object from the openBIS node
        Space space = (Space) node.getUserObject();

        // Ask the user to specify a project name
        String projectCode = JOptionPane
                .showInputDialog(null,
                        "Space: " + space.getCode() +
                        "\n\nNew project name:",
                        "Create new project...",
                        JOptionPane.INFORMATION_MESSAGE);
        if (projectCode == null || projectCode.equals("")) {
            outputPane.warn("Creation of new project aborted by user.");
            return false;
        }

        // Create the project
        List<ProjectPermId> createdProjects;
        try {
            createdProjects = openBISProcessor.createProject(space.getCode(),
                    projectCode, false);
        } catch (Exception e) {
            outputPane.err("Could not create project /" + space.getCode() + "/"
                    + projectCode + "! " + e.getMessage());
            return false;
        }

        // One more check
        if (createdProjects.size() == 0) {
            outputPane.err("Could not create project /" + space.getCode() + "/"
                    + projectCode + "! Please contact your "
                    + "openBIS administrator!");
            return false;
        }

        // Inform
        outputPane.log("Project /" + space.getCode() + "/" + projectCode
                + " created successfully.");

        // Return success
        return true;
    }

    /**
     * Asks the user to give a project name and will then try to create it as a
     * child of the passed OpenBISSpaceNode
     *
     * @return true if creation was successful, false otherwise.
     */
    private boolean createNewTag(AbstractOpenBISNode node) {

        // Retrieve and store the createProject service
        if (!openBISProcessor.isLoggedIn()) {

            // TODO Throw an exception to distinguish the case where
            // the project could not be created.
            outputPane.err("No valid session! Please log in first!");
            return false;

        }

        // Get the list of spaces
        SearchResult<Space> spaceResult = this.openBISProcessor
                .retrieveAndCacheSpaces();
        if (spaceResult.getTotalCount() == 0) {
            return false;
        }

        // Get the list of Spaces
        List<Space> spaces = spaceResult.getObjects();

        // Initialize the Space object
        Space space = null;

        // If we haven't Is there something already selected in the tree?
        if (node == null) {
            node = (AbstractOpenBISNode) tree.getLastSelectedPathComponent();
        }

        // If there is nothing selected in the tree, do we have a default project
        // (from which to get the space?)
        if (node == null) {
            node = getDefaultProjectOrFirst();
        }

        // Still check if node is != null in case nothing usable was
        // selected in the tree.
        if (node != null) {
            String className = node.getClass().getSimpleName();

            if (className.equals("OpenBISUserNode")) {

                node = getDefaultProjectOrFirst();
                OpenBISSpaceNode spaceNode = (OpenBISSpaceNode) node
                        .getParent();
                space = (Space) spaceNode.getUserObject();

            } else if (className.equals("OpenBISSpaceNode")) {

                space = (Space) node.getUserObject();

            } else if (className.equals("OpenBISProjectNode")) {

                OpenBISSpaceNode spaceNode = (OpenBISSpaceNode) node
                        .getParent();
                space = (Space) spaceNode.getUserObject();

            } else if (className.equals("OpenBISExperimentNode")) {

                OpenBISProjectNode projectNode = (OpenBISProjectNode) node
                        .getParent();
                OpenBISSpaceNode spaceNode = (OpenBISSpaceNode) projectNode
                        .getParent();
                space = (Space) spaceNode.getUserObject();

            } else if (className.equals("OpenBISSampleNode")) {

                OpenBISExperimentNode experimentNode = (OpenBISExperimentNode) node
                        .getParent();
                OpenBISProjectNode projectNode = (OpenBISProjectNode) experimentNode
                        .getParent();
                OpenBISSpaceNode spaceNode = (OpenBISSpaceNode) projectNode
                        .getParent();
                space = (Space) spaceNode.getUserObject();

            } else if (className.equals("OpenBISUserNode")) {

                // User node: fall back to picking the first node in the tree
                space = null;

            } else {

                // Unknown node... Fall back to picking the first node in the
                // tree.
                space = null;
            }
        }

        // Create the dialog
        if (createTagDialog == null) {
            createTagDialog = new OpenBISCreateTagDialog(spaces, space);
        } else {
            createTagDialog.setSelectedSpace(space);
            createTagDialog.resetTagFields();
            createTagDialog.setVisible(true);
        }

        // Did the user cancel?
        if (createTagDialog.wasCancelled()) {
            outputPane.log("Tag creation cancelled.");
            return false;
        }

        // Retrieve the entered information.
        space = createTagDialog.getSelectedSpace();
        String tagName = createTagDialog.getTagName();
        String tagDescr = createTagDialog.getTagDescription();

        if (tagName == null || tagName.equals("")) {
            outputPane.warn("Creation of new tag aborted by user.");
            return false;
        }
        if (tagName.length() > 60) {
            outputPane.err("The name of the tag cannot be more"
                    + " than 60 characters long.");
            return false;
        }

        // Try creating the tag
        boolean success = openBISProcessor.createTag(space, tagName, tagDescr);

        if (success) {

            // Make sure the selected space is shown in the tree
            TreeModel model = tree.getModel();
            OpenBISUserNode rootNode = (OpenBISUserNode) model.getRoot();
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                OpenBISSpaceNode spaceNode = (OpenBISSpaceNode) rootNode
                        .getChildAt(i);
                Space currentSpace = (Space) spaceNode.getUserObject();
                if (currentSpace.getPermId().toString()
                        .equals(space.getPermId().toString())) {

                    tree.setSelectionPath(new TreePath(spaceNode.getPath()));
                    break;
                }
            }

            // Update the tag list
            outputPane.log("Successfully created tag " + tagName + ".");
            clearTagList();
            setTagList(openBISProcessor.getTagsForSpace(space), space);
            return true;
        } else {
            outputPane.err("Could not create tag " + tagName + "! " + openBISProcessor.getLastErrorMessage());
            return false;
        }
    }

    /**
     * Set the project with given identifier as the default target.
     *
     * @param projectId The openBIS project identifier.
     * @return bool True if the default project could be set successfully, false
     *         otherwise.
     */
    private boolean setAsDefaultProject(final String projectId) {
        return globalSettingsManager.setDefaultProject(projectId);
    }

    /**
     * Clear the list of tags in the UI.
     */
    private void clearTagList() {
        userTagList.setModel(new DefaultListModel<Tag>());
    }

    /**
     * Extract and set the list of tags for the given openBIS project node.
     *
     * @param tags An OpenBISProjectNode.
     */
    private void setTagListFromOpenBISProjectNode(
            OpenBISProjectNode projectNode) {

        // Clear the list of tags
        clearTagList();

        if (projectNode == null) {
            return;
        }

        // Retrieve the parent Space
        OpenBISSpaceNode spaceNode = (OpenBISSpaceNode) projectNode.getParent();
        Space space = (Space) spaceNode.getUserObject();

        // Retrieve the tag list
        List<Sample> tags = openBISProcessor.getTagsForSpace(space);

        // Fill the tag list
        setTagList(tags, space);
    }

    /**
     * Set the list of tags in the UI.
     *
     * @param tags List of tags retrieved from openBIS.
     */
    private void setTagList(List<Sample> tags, Space space) {
        clearTagList();
        DefaultListModel<Tag> listModel = (DefaultListModel<Tag>) userTagList
                .getModel();
        for (Sample tag : tags) {
            listModel.addElement(new Tag(tag));
        }
        userTags.setText(
                "<html><b>Tags</b> from <b>" + space.getCode() + "</b></html>");
    }

    /**
     * Create the panel that contains the openBIS tree viewer
     *
     * @return a JPanel.
     */
    private JPanel openBISViewerPanel() {

        // Create a panel
        JPanel openBISViewerPanel = new JPanel();

        // Set a grid bag layout
        openBISViewerPanel.setLayout(new GridBagLayout());

        // Common constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;

        // Create the root node for the tree
        userNode = new OpenBISUserNode(defaultRootNodeString);

        // Create a tree that allows one selection at a time.
        tree = new OpenBISViewerTree(userNode);

        // Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        // Listen for when a node is about to be opened (for lazy loading)
        tree.addTreeWillExpandListener(this);

        // Add a context menu
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (QueryOS.isWindows()) {
                    return;
                }
                setListenerOnJTree(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (QueryOS.isMac()) {
                    return;
                }
                setListenerOnJTree(e);
            }
        });

        // Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);

        // Add the tree viewer to the layout
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = 3;
        constraints.insets = new Insets(5, 5, 5, 0);
        openBISViewerPanel.add(treeView, constraints);

        // Add a rescan button
        scanButton = new JButton("Scan");
        scanButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Invalidate the cache
                openBISProcessor.resetCachedData();

                // Rescan
                scan();
            }
        });

        // Add to the layout
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        // constraints.gridwidth = 1;
        // constraints.gridheight = 1;
        constraints.gridwidth = 3;
        constraints.insets = new Insets(0, 5, 5, 0);
        openBISViewerPanel.add(scanButton, constraints);

        return openBISViewerPanel;
    }

    private JPanel tagsPanel() {

        // Create a panel
        JPanel tagsPanel = new JPanel();

        // Set a grid bag layout
        tagsPanel.setLayout(new GridBagLayout());

        // Common constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;

        // Add a simple label
        userTags = new JLabel("<html><b>Tags</b></html>");
        // userTags.setVerticalAlignment(SwingConstants.TOP);

        // Add to the layout
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(5, 0, 0, 5);
        tagsPanel.add(userTags, constraints);

        // Add a spacer
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(5, 0, 0, 0);
        tagsPanel.add(new JLabel(""), constraints);

        // Add a push button
        JButton addTagButton = new JButton("Create new tag...");
        addTagButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                createNewTag(null);
            }
        });
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(5, 0, 0, 0);
        tagsPanel.add(addTagButton, constraints);

        // Add the list of tags
        userTagList = new JList<Tag>(new DefaultListModel<Tag>());
        userTagList.setVisibleRowCount(5);
        userTagList.getSelectionModel().setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        userTagList.setDragEnabled(true);
        userTagList.setTransferHandler(
                new TagListExportTransferHandler(userTagList));
        JScrollPane tagScrollPane = new JScrollPane(userTagList);

        // Add to the layout
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = 3;
        // constraints.gridheight = 1;
        constraints.insets = new Insets(5, 5, 5, 0);
        tagsPanel.add(tagScrollPane, constraints);

        return tagsPanel;
    }

}
