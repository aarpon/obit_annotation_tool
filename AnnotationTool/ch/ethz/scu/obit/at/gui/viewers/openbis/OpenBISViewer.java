package ch.ethz.scu.obit.at.gui.viewers.openbis;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ch.ethz.scu.obit.at.gui.pane.OutputPane;
import ch.ethz.scu.obit.at.gui.viewers.ObserverActionParameters;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.AbstractOpenBISNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISExperimentNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISSampleListNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISSpaceNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISUserNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.view.OpenBISViewerTree;
import ch.ethz.scu.obit.common.settings.UserSettingsManager;
import ch.ethz.scu.obit.common.utils.QueryOS;
import ch.ethz.scu.obit.processors.openbis.OpenBISProcessor;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * Graphical user interface to log in to openBIS and choose where to store
 * acquired datasets.
 * @author Aaron Ponti
 */
public class OpenBISViewer extends Observable
	implements ActionListener, TreeSelectionListener, TreeWillExpandListener {

	protected JPanel panel;
	protected JButton scanButton;
	
	private OpenBISProcessor openBISProcessor;

    private OpenBISUserNode userNode;
	private OpenBISViewerTree tree;
    private String defaultRootNodeString = "/";

	//private boolean isLoggedIn = false;

    private boolean isReady = false;
	
	protected OutputPane outputPane;

	String loginErrorMessage = "";
	String loginErrorTitle = "";
	boolean loginErrorRecoverable = true;


	/**
	 * Constructor
	 */
	public OpenBISViewer(OpenBISProcessor openBISProcessor, OutputPane outputPane) {

		// Store the OpenBISProcessor reference
		this.openBISProcessor = openBISProcessor;

		// Store the OutputPane reference
		this.outputPane = outputPane;

		// Create a panel
		panel = new JPanel();
		
		// Get the openBIS URL from the appProperties
		UserSettingsManager manager = new UserSettingsManager();
		if (! manager.load()) {
			JOptionPane.showMessageDialog(null,
					"Could not read application settings!\n" +
			"Please contact your administrator. The application\n" +
			"will now exit!",
			"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		// Set a grid bag layout
		panel.setLayout(new GridBagLayout());

		// Common constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.BOTH;
		
		// Add a title JLabel
        JLabel title = new JLabel("openBIS viewer");

		// Add the tree viewer to the layout
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.insets = new Insets(0, 5, 5, 0);
		panel.add(title, constraints);
		
		// Create the root node for the tree
		userNode = new OpenBISUserNode(defaultRootNodeString);
		
		// Create a tree that allows one selection at a time.
		tree = new OpenBISViewerTree(userNode);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

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
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 0);
		panel.add(treeView, constraints);

		// Add a rescan button
        scanButton = new JButton("Scan");
		scanButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                scan();
            }
        });  

		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(0, 5, 5, 0);
		panel.add(scanButton, constraints);
		
		// Set sizes
		panel.setMinimumSize(new Dimension(400, 700));
		panel.setPreferredSize(new Dimension(400, 700));
	}
	
	/**
	 * Return the OpenBISProcessor.
	 * @return the OpenBISProcessor.
	 */
	public OpenBISProcessor getOpenBISProcessor() {
		return openBISProcessor;
	}

	/**
	 * Returns the user name if successfully logged in, empty string otherwise 
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
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
		// Get the selected tree node
		AbstractOpenBISNode node = (AbstractOpenBISNode)
                tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}

		// TODO Implement!
	}

	/**
	 * Return the Tree's data model.
	 */
	public TreeModel getDataModel() {
		return tree.getModel();
	}
	
	/**
	 * Clear the tree view
	 */
	private void clearTree() {

		// Is there already something in the tree?
		if (tree != null) {
			TreeModel model = tree.getModel();
			if (model != null) {
				DefaultMutableTreeNode rootNode = 
						(DefaultMutableTreeNode) model.getRoot();
				if (rootNode != null) {
					rootNode.removeAllChildren();
					((DefaultTreeModel) model).reload();
					rootNode = null;
					System.gc();
				}
			}
		}
		
		// Create the root node
		userNode = new OpenBISUserNode(defaultRootNodeString);

		tree.setModel(new DefaultTreeModel(userNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

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
		if (! openBISProcessor.isLoggedIn()) {
			return;
		}
		
		// Check that the session is still open (we just check the
		// facade, the queryFacade is not necessary
		if (!openBISProcessor.checkSession()) {
			JOptionPane.showMessageDialog(this.panel,
					"The openBIS session is no longer valid!\n" + 
			"Please try logging in again.",	
					"Session error",
					JOptionPane.ERROR_MESSAGE);
			clearTree();
			return;
		}

		// Disable the "scan" button
		scanButton.setEnabled(false);
		
		// Set the root of the tree
		userNode = new OpenBISUserNode(openBISProcessor.getUserName());

		// Get spaces
		List<SpaceWithProjectsAndRoleAssignments> spaces = 
				openBISProcessor.getSpaces();
		if (spaces.isEmpty()) {
			JOptionPane.showMessageDialog(this.panel,
					"Sorry, there are no (accessible) spaces.\n\n" + 
						"Please ask your administrator to create a " +
						"space for you or to grant you access to an " +
						"existing one.\nNo data registration will be " +
						"possible until this issue is fixed.",
					"Warning",
					JOptionPane.WARNING_MESSAGE);
			// We do not need to return, this case is treated below
		}

		// Keep a count of the usable projects
		int nProjects = 0;
		
		for (SpaceWithProjectsAndRoleAssignments s : spaces) {

			// Add the space
			space = new OpenBISSpaceNode(s);
			userNode.add(space);
		    
			// Get the projects for current space
			List<Project> projects = s.getProjects();
			
			// We add the projects -- experiments and samples will be 
			// lazily loaded on node expansion
			for (Project p : projects) {
			    
				// Add the project
				project = new OpenBISProjectNode(p);
				space.add(project);
				nProjects++;
			}
			
		    
		}

		// Update the view
		tree.setModel(new DefaultTreeModel(userNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

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
					"Sorry, there are no (accessible) projects.\n\n" + 
						"You will need to create one yourself or " +
						"ask your space administrator to do it " +
						"for you.\nNo data registration will be " +
						"possible until this issue is fixed.",
					"Warning",
					JOptionPane.WARNING_MESSAGE);
			// We do not need to return, this case is treated below

		}
		
		// Re-enable the "scan" button
		scanButton.setEnabled(true);

		// Inform
		outputPane.log("Retrieving openBIS structure completed.");
	
	}

	/**
	 * Returns true if the viewer has completed creation of the data model
	 * @return true if the data model is complete, false otherwise
	 */
	public boolean isReady() { return isReady; }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Currently we do not do anything when the user interacts 
		// with the code 
	}
	
	/**
	 * Return the reference to the JPanel to be added to a container component
	 * @return JPanel reference
	 */
	public JPanel getPanel() {
		return panel;
	}


	/**
	 * Called when a node in the Tree is about to expand.
	 * @param event A TreeExpansionEvent.
	 * Required by TreeWillExpandListener interface.
	 */	
	@Override
	public void treeWillExpand(TreeExpansionEvent event)
			throws ExpandVetoException {
        TreePath path = event.getPath();
        loadLazyChildren(
        		(AbstractOpenBISNode) path.getLastPathComponent());
	}

	/**
	 * Called when a node in the Tree is about to collapse.
	 * @param event A TreeExpansionEvent.
	 * Required by TreeWillExpandListener interface.
	 */
	@Override
	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException {
		// We do nothing
	}
	
	/**
	 * Load the childen of the specified node if needed.
	 * @param node Node to query for children.
	 */
	private void loadLazyChildren(AbstractOpenBISNode node) {
		
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
			List<String> expId = new ArrayList<String>();
			expId.add(p.getIdentifier());
			List<Experiment> experiments = 
					openBISProcessor.getExperimentsForProjects(expId);
			for (Experiment e : experiments) {
				// Add the experiments
				OpenBISExperimentNode experiment = new OpenBISExperimentNode(e);
				node.add(experiment);
			}

			// Inform
			outputPane.log("Retrieved list of experiments for project " +
			p.getIdentifier() + ".");

		} else if (className.equals("Experiment")) {
		
			// If we have an Experiment, we load the contained Samples
			Experiment e = (Experiment) obj;
			List<String> experimentId = new ArrayList<String>();
			experimentId.add(e.getIdentifier());

			// To be restored -- and extended -- in the future.

			//EnumSet<SampleFetchOption> opts = EnumSet.of(SampleFetchOption.PROPERTIES);
			//List<Sample> samples = 
			//		facade.listSamplesForExperiments(experimentID, opts);
			//for (Sample sm : samples) {
			//	// Add the samples
			//	OpenBISSampleNode sample = new OpenBISSampleNode(sm);
			//	node.add(sample);
			//}

			// Temporarily, we just display the number of contained 
			// samples in the Experiment, until we will have uncoupled
			// the openBIS Viewer from the customizable openBIS 
			// Processor.
			List<Sample> samples = 
					openBISProcessor.getSamplesForExperiments(experimentId);
			int nSamples = samples.size();
			String title = ""; 
			if (nSamples == 0) {
				title = "No samples";
			} else if (nSamples == 1) {
				title = "One sample";
			} else {
				title = nSamples + " samples";
			}
			node.add(new OpenBISSampleListNode(title));
			
			// Inform
			outputPane.log("Retrieved number of samples for experiment " +
			e.getIdentifier() + ".");
			
		} else {
			
			// We do nothing for other openBIS object types
		}

		// Mark the node as loaded (in any case)
		node.setLoaded();
		
	}

	/**
	 * Sets a mouse event listener on the JTree
	 * @param e Mouse event
	 */
    private void setListenerOnJTree(MouseEvent e) {

        if (e.isPopupTrigger() &&
                e.getComponent() instanceof OpenBISViewerTree) {

            // Position of mouse click
            int x = e.getPoint().x;
            int y = e.getPoint().y;

            // Get selected node
            TreePath p = tree.getPathForLocation(x, y);
            if (p == null) {
                // There is nothing usable at that location
                return;
            }
            AbstractOpenBISNode node =
                    (AbstractOpenBISNode) p.getLastPathComponent();

            // Type of node
            String nodeType = node.getClass().getSimpleName();

            // Add relevant context menu
            if (nodeType.equals("OpenBISSpaceNode")) {

                JPopupMenu popup =
                        createSpacePopup((OpenBISSpaceNode) node);
                popup.show(e.getComponent(), x, y);
            }
        }
    }

	/**
	 * Create a popup menu with actions for a space node
	 * @return a JPopupMenu for the passed item
	 */
	private JPopupMenu createSpacePopup(final OpenBISSpaceNode node) {
		
		// Create the popup menu.
	    JPopupMenu popup = new JPopupMenu();

	    // Create new project
	    String menuEntry = "Create new project";
	    JMenuItem menuItem = new JMenuItem(menuEntry);
	    menuItem.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	if (createNewProject(node)) {
            		// Rescan
            		scan();
            	}

			}
        });
	    popup.add(menuItem);
	
	    return popup;
	}

	/**
	 * Asks the user to give a project name and will then try to create
	 * it as a child of the passed OpenBISSpaceNode
	 * @param node An OpenBISSpaceNode
	 * @return true if creation was successfull, false otherwise.
	 */
	private boolean createNewProject(final OpenBISSpaceNode node) {
		
		// Retrieve and store the createProject service
		if (!openBISProcessor.retrieveAndStoreServices()) {
				
			// TODO Throw an exception to distinguish the case where
			// the project could not be created.
			return false;

		}

		// Get the space object from the openBIS node
		SpaceWithProjectsAndRoleAssignments space =
				(SpaceWithProjectsAndRoleAssignments) 
				node.getUserObject();

		// Ask the user to specify a project name
		String projectCode = JOptionPane.showInputDialog(
				"Please enter new project name (code)");
		if (projectCode == null || projectCode.equals("")) {
			outputPane.warn("Creation of new project aborted by user.");
			return false;
		}

		// Call the ingestion server and collect the output
		QueryTableModel tableModel = openBISProcessor.createProject(
				space.getCode(), projectCode);

		// Display the output
		String success= "";
		String message = "";
		List<Serializable[]> rows = tableModel.getRows();
		for (Serializable[] row : rows) {
			success = (String)row[0];
			message = (String)row[1];
			if (success.equals("true")) {
				outputPane.log(message);
				return true;
			}
		}
		outputPane.err(message);
		return false;
	}
	
}
