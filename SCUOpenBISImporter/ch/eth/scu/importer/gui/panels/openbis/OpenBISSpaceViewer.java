package ch.eth.scu.importer.gui.panels.openbis;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.springframework.remoting.RemoteConnectFailureException;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.common.properties.DropboxProperties;
import ch.eth.scu.importer.gui.dialogs.OpenBISLoginDialog;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Graphical user interface to log in to openBIS and choose where to store
 * acquired datasets.
 * @author Aaron Ponti
 */
public class OpenBISSpaceViewer extends Observable
	implements ActionListener, TreeSelectionListener {

	protected JPanel panel;
	
	private String openBISURL = "";
	private String userName = "";
	private String userPassword = "";
	private int timeout = 60000;
	private IOpenbisServiceFacade facade;
	
	private JLabel title;
	private OpenBISRootNode rootNode;
	private JTree tree;
	private JScrollPane treeView;
	private String defaultRootNodeString = "Please login to openBIS...";

	private boolean isLoggedIn = false;
	
	private OpenBISLoginDialog loginDialog;
	
	private boolean isReady = false;
	
	/**
	 * Constructor
	 */
	public OpenBISSpaceViewer(DropboxProperties dropboxProperties) {

		// Create a panel
		panel = new JPanel();
		
		// Get the openBIS URL from the appProperties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		
		// Set the URL
		this.openBISURL = appProperties.getProperty("OpenBISURL");
		
		// Set a grid bag layout
		panel.setLayout(new GridBagLayout());

		// Common constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.BOTH;
		
		// Add a title JLabel
		title = new JLabel("openBIS viewer");

		// Add the tree viewer to the layout
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		panel.add(title, constraints);
		
		// Create the root node for the tree
		rootNode = new OpenBISRootNode(defaultRootNodeString);
		
		// Create a tree that allows one selection at a time.
		tree = new JTree(rootNode);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);
		
		// Create the scroll pane and add the tree to it. 
		treeView = new JScrollPane(tree);
		
		// Add the tree viewer to the layout
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		panel.add(treeView, constraints);

		// Set sizes
		panel.setMinimumSize(new Dimension(400, 700));
		panel.setPreferredSize(new Dimension(400, 700));
	}
	
	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
		// Get the selected tree node
		CustomOpenBISNode node = (CustomOpenBISNode)
                tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		
	}

	/**
	 * Asks the user to enter credentials.
	 * @return true if the credentials were entered correctly, false otherwise.
	 */
	private boolean askForCredentials() {

		// Modal dialog: stops here until the dialog is disposed
		// (when a username and password have been provided)
		loginDialog = new OpenBISLoginDialog();
		if (loginDialog.interrupted() == true) {
			return false;
		}
		userName = loginDialog.getUsername();
		userPassword = loginDialog.getPassword();
		return true;
	}
	
	/**
	 * Login to openBIS. Credentials provided by the user through a dialog.
	 * @return true if login was successful (or if already logged in), 
	 * false otherwise.
	 */
	public boolean login() {

		// Check that user name and password were set
		if (userName.equals("") || userPassword.equals("")) {
			if (askForCredentials() == false) {
				return false;
			}
		}

		// Are we already logged in?
		if (isLoggedIn == true) {
			return true;
		}
		
		// Try logging in with current credentials
		try {
			facade = OpenbisServiceFacadeFactory.tryCreate(
					userName, userPassword, openBISURL, timeout);
		} catch (UserFailureException e) {
			JOptionPane.showMessageDialog(this.panel,
					"Login failed. Please try again.");
			userName = "";
			userPassword = "";
			facade = null;
			return false;
		} catch (RemoteConnectFailureException e) {
			JOptionPane.showMessageDialog(this.panel,
					"Could not connect to openBIS.\n" + 
			"The server appears to be down.\n" +
							"Please try again later.",	
					"Connection error",
					JOptionPane.ERROR_MESSAGE);
			facade = null;
			return false;
		}

		// Set isLoggedIn to true
		isLoggedIn = true;
		
		// Fill in the tree view with the openBIS data
		fillTreeViewWithOpenBISData();
		
		return true;
	}
	
	/**
	 * Log out from openBIS
	 * @return true if logging out was successful, false otherwise.
	 */
	public boolean logout() {
		if (facade != null && isLoggedIn == true) {
			facade.logout();
			clearTreeView();
			isLoggedIn = false;
			return true;
		}
		return false;
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
	private void clearTreeView() {

		// Create the root node
		rootNode = new OpenBISRootNode(defaultRootNodeString);

		tree.setModel(new DefaultTreeModel(rootNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);		
	}
	
	/**
	 * Fill the tree view with the data obtained from openBIS
	 */
	private void fillTreeViewWithOpenBISData() {

		OpenBISSpaceNode space = null;
		OpenBISProjectNode project = null;
		OpenBISExperimentNode experiment = null;
		OpenBISSampleNode sample = null;
		
		// Do we have a connection with openBIS?
		if (facade == null || isLoggedIn == false) {
			return;
		}
		
		// TODO Check that the session is still open
		try {
			facade.checkSession();
		} catch ( InvalidSessionException e ) {
			JOptionPane.showMessageDialog(this.panel,
					"The openBIS session is no longer valid!\n" + 
			"Please try logging in again.",	
					"Session error",
					JOptionPane.ERROR_MESSAGE);
			facade = null;
			isLoggedIn = false;
			clearTreeView();
			return;
		}

		// Set the root of the tree
		rootNode = new OpenBISRootNode(userName);

		// Get spaces
		List<SpaceWithProjectsAndRoleAssignments> spaces =
				facade.getSpacesWithProjects();

		for (SpaceWithProjectsAndRoleAssignments s : spaces) {

			// Add the space
			space = new OpenBISSpaceNode(s);
			rootNode.add(space);
		    
			// Get the projects for current space
			List<Project> projects = s.getProjects();
			
			for (Project p : projects) {
			    
				// Add the project
				project = new OpenBISProjectNode(p);
				space.add(project);

				// Get the experiments
				List<String> expId = new ArrayList<String>();
				expId.add(p.getIdentifier());
				List<Experiment> experiments = 
						facade.listExperimentsForProjects(expId);
				
				for (Experiment e : experiments) {

					// Add the experiment
					experiment = new OpenBISExperimentNode(e);
					project.add(experiment);
				
					// Get the samples for the experiment
					List<String> sampleId = new ArrayList<String>();
					sampleId.add(e.getIdentifier());
					List<Sample> samples = 
							facade.listSamplesForExperiments(sampleId);
					
					for (Sample sm : samples) {
						
						// Add the sample
						sample = new OpenBISSampleNode(sm);
						experiment.add(sample);
					}
				}
				
			}
		    
		}
		
		// Update the view
		tree.setModel(new DefaultTreeModel(rootNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);
		
		// Set isReady to true
		isReady = true;
		
		// Notify observers that the scanning is done 
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns true if the viewer has completed creation of the data model
	 * @return true if the data model is complete, false otherwise
	 */
	public boolean isReady() { return isReady; }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Return the reference to the JPanel to be added to a container component
	 * @return JPanel reference
	 */
	public JPanel getPanel() {
		return panel;
	}

	/**
	 * A Custom TreeNode to store OpenBIS-related entities in the JTree
	 * @author Aaron Ponti
	 *
	 */
	abstract public class CustomOpenBISNode extends DefaultMutableTreeNode {
		
		private static final long serialVersionUID = 1L;

		protected String identifier;
		
		public CustomOpenBISNode(Object o) {
			super(o);
		}
		
		abstract public String getType();
		
		abstract public String toString();
		
		abstract public String getCode();
		
		abstract public String getIdentifier();
	}
	
	/**
	 * A Custom Root TreeNode
	 * @author Aaron Ponti
	 *
	 */
	public class OpenBISRootNode extends CustomOpenBISNode {
		
		private static final long serialVersionUID = 1L;
		
		protected String name;
		
		/**
		 * Constructor
		 * @param name User name
		 */
		public OpenBISRootNode(String name) {
			super(name);
			this.name = name;
		}
		
		/**
		 * Returns the type of the node
		 * @return Type of the node
		 */
		public String getType() { return "root"; }

		/**
		 * String representation of the node
		 * @return String representation of the node
		 */
		@Override
		public String toString() { return name; }

		/**
		 * Root code (custom)
		 * @return String openBIS code of the root object (set to "ROOT")
		 */
		@Override
		public String getCode() { return "ROOT"; }

		/**
		 * Root identifier (custom)
		 * @return String openBIS identifier of the root object (set to "/")
		 */
		@Override
		public String getIdentifier() { return "/"; }
		
	}
	
	/**
	 * A Custom Space TreeNode
	 * @author Aaron Ponti
	 *
	 */
	public class OpenBISSpaceNode extends CustomOpenBISNode {

		private static final long serialVersionUID = 1L;
		
		private SpaceWithProjectsAndRoleAssignments s;
		
		/**
		 * Constructor 
		 * @param s SpaceWithProjectsAndRoleAssignments object
		 */
		public OpenBISSpaceNode(SpaceWithProjectsAndRoleAssignments s) {
			super(s);
			this.s = s;
		}
		
		/**
		 * Returns the type of the node
		 * @return Type of the node
		 */		
		@Override
		public String getType() { return "Space"; }

		/**
		 * String representation of the node
		 * @return String representation of the node
		 */		
		@Override
		public String toString() { return ("[space] " + s.getCode()); }

		
		/**
		 * Space code
		 * @return Space code
		 */			
		@Override
		public String getCode() { return s.getCode(); }
		
		/**
		 * Space identifier
		 * @return Space identifier
		 */			
		@Override
		public String getIdentifier() { return s.getCode(); }
		
	}

	/**
	 * A Custom Project TreeNode
	 * @author Aaron Ponti
	 *
	 */	
	public class OpenBISProjectNode extends CustomOpenBISNode {
		
		private static final long serialVersionUID = 1L;
		
		private Project p;

		/**
		 * Constructor 
		 * @param p Project object
		 */		
		public OpenBISProjectNode(Project p) {
			super(p);
			this.p = p;
		}

		/**
		 * Returns the type of the node
		 * @return Type of the node
		 */			
		@Override
		public String getType() { return "Project"; }

		/**
		 * String representation of the node
		 * @return String representation of the node
		 */			
		@Override
		public String toString() { return ("[project] " + p.getCode()); }

		/**
		 * Project code
		 * @return Project code
		 */			
		@Override
		public String getCode() { return p.getCode(); }
		
		/**
		 * Project identifier
		 * @return Project identifier
		 */			
		@Override
		public String getIdentifier() { return p.getIdentifier(); }
		
	}

	/**
	 * A Custom Experiment TreeNode
	 * @author Aaron Ponti
	 *
	 */	
	public class OpenBISExperimentNode extends CustomOpenBISNode {
		
		private static final long serialVersionUID = 1L;

		private Experiment e;

		/**
		 * Constructor 
		 * @param e Experiment object
		 */		
		public OpenBISExperimentNode(Experiment e) {
			super(e);
			this.e = e;
		}

		/**
		 * Returns the type of the node
		 * @return Type of the node
		 */			
		@Override
		public String getType() { return "Experiment"; }

		/**
		 * String representation of the node
		 * @return String representation of the node
		 */			
		@Override
		public String toString() { return ( "[experiment] " + e.getCode()); }

		/**
		 * Experiment code
		 * @return Experiment code
		 */			
		@Override
		public String getCode() { return e.getCode(); }
		
		/**
		 * Experiment identifier
		 * @return Experiment identifier
		 */			
		@Override
		public String getIdentifier() { return e.getIdentifier(); }
		
	}
	
	/**
	 * A Custom Sample TreeNode
	 * @author Aaron Ponti
	 *
	 */	
	public class OpenBISSampleNode extends CustomOpenBISNode {
		
		private static final long serialVersionUID = 1L;

		private Sample s;

		/**
		 * Constructor 
		 * @param e Experiment object
		 */		
		public OpenBISSampleNode(Sample s) {
			super(s);
			this.s = s;
		}

		/**
		 * Returns the type of the node
		 * @return Type of the node
		 */			
		@Override
		public String getType() { return "Sample"; }

		/**
		 * String representation of the node
		 * @return String representation of the node
		 */			
		@Override
		public String toString() { return ( "[sample] " + s.getCode()); }

		/**
		 * Sample code
		 * @return Sample code
		 */			
		@Override
		public String getCode() { return s.getCode(); }
		
		/**
		 * Sample identifier
		 * @return Sample identifier
		 */			
		@Override
		public String getIdentifier() { return s.getIdentifier(); }
		
	}

}
