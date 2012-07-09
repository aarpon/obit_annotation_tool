package ch.eth.scu.importer.gui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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
public class OpenBISSpaceViewer extends JPanel 
	implements ActionListener, TreeSelectionListener {

	private static final long serialVersionUID = 1L;

	private String openBISURL = "";
	private String userName = "";
	private String userPassword = "";
	private int timeout = 60000;
	private IOpenbisServiceFacade facade;
	
	private JLabel title;
	private DefaultMutableTreeNode rootNode;
	private JTree tree;
	private JScrollPane treeView;
	private String defaultRootNodeString = "Please login to openBIS...";

	private boolean isLoggedIn = false;
	
	private OpenBISLoginDialog loginDialog;
	
	// Reference to the DropboxProperties object
	private DropboxProperties dropboxProperties;
	
	/**
	 * Constructor
	 */
	public OpenBISSpaceViewer(DropboxProperties dropboxProperties) {

		// Get the openBIS URL from the appProperties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		
		// Set the URL
		this.openBISURL = appProperties.getProperty("OpenBISURL");
		
		// Set the reference to the MetadataEditor
		this.dropboxProperties = dropboxProperties;
		
		// Set a grid bag layout
		setLayout(new GridBagLayout());

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
		add(title, constraints);
		
		// Create the root node for the tree
		rootNode = new DefaultMutableTreeNode(defaultRootNodeString);
		
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
		add(treeView, constraints);

		// Set sizes
		setMinimumSize(new Dimension(400, 700));
		setPreferredSize(new Dimension(400, 700));
	}
	
	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
		// Get the selected tree node
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		
		// Get the object stored at that nose
		Object nodeInfo = node.getUserObject();
		
		// Make sure it is an experiment
		if (nodeInfo instanceof ExperimentWrapper) {
			// Update the DropboxProperties
			dropboxProperties.add("Experiment", 
					((ExperimentWrapper) nodeInfo).getIdentifier());
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
			JOptionPane.showMessageDialog(this,
					"Login failed. Please try again.");
			userName = "";
			userPassword = "";
			facade = null;
			return false;
		} catch (RemoteConnectFailureException e) {
			JOptionPane.showMessageDialog(this,
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
	 * Clear the tree view
	 */
	private void clearTreeView() {

		// Create the root node
		rootNode = new DefaultMutableTreeNode(defaultRootNodeString);

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

		DefaultMutableTreeNode space = null;
		DefaultMutableTreeNode project = null;
		DefaultMutableTreeNode experiment = null;
		DefaultMutableTreeNode sample = null;
		
		// Do we have a connection with openBIS?
		if (facade == null || isLoggedIn == false) {
			return;
		}
		
		// TODO Check that the session is still open
		try {
			facade.checkSession();
		} catch ( InvalidSessionException e ) {
			JOptionPane.showMessageDialog(this,
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
		rootNode = new DefaultMutableTreeNode(userName);

		// Get spaces
		List<SpaceWithProjectsAndRoleAssignments> spaces =
				facade.getSpacesWithProjects();

		for (SpaceWithProjectsAndRoleAssignments s : spaces) {

			// Add the space
			SpaceWrapper spaceWrapper = new SpaceWrapper(s);
			space = new DefaultMutableTreeNode(spaceWrapper);
			rootNode.add(space);
		    
			// Get the projects for current space
			List<Project> projects = s.getProjects();
			
			for (Project p : projects) {
			    
				// Add the project
				ProjectWrapper projectWrapper = new ProjectWrapper(p);
				project = new DefaultMutableTreeNode(projectWrapper);
				space.add(project);

				// Get the experiments
				List<String> expId = new ArrayList<String>();
				expId.add(p.getIdentifier());
				List<Experiment> experiments = 
						facade.listExperimentsForProjects(expId);
				
				for (Experiment e : experiments) {

					// Add the experiment
					ExperimentWrapper experimentWrapper = new ExperimentWrapper(e);
					experiment = new DefaultMutableTreeNode(experimentWrapper);
					project.add(experiment);
				
					// Get the samples for the experiment
					List<String> sampleId = new ArrayList<String>();
					sampleId.add(e.getIdentifier());
					List<Sample> samples = 
							facade.listSamplesForExperiments(sampleId);
					
					for (Sample sm : samples) {
						
						// Add the sample
						SampleWrapper sampleWrapper = new SampleWrapper(sm);
						sample = new DefaultMutableTreeNode(sampleWrapper);
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
		
	}

	/**
	 * Wraps the SpaceWithProjectsAndRoleAssignments class to overload its
	 * toString() method
	 * @author Aaron Ponti
	 *
	 */	
	protected class SpaceWrapper {

		private SpaceWithProjectsAndRoleAssignments s;

		protected SpaceWrapper(SpaceWithProjectsAndRoleAssignments s) {
			this.s = s;
		}
		
		@Override
		public String toString() {
			return new String( "[space] " + s.getCode());
		}
		
		public String getIdentifier() {
			return s.getCode();
		}
		
	}

	/**
	 * Wraps the Project class to overload its toString() method
	 * @author Aaron Ponti
	 *
	 */
	protected class ProjectWrapper {

		private Project p;

		protected ProjectWrapper(Project p) {
			this.p = p;
		}
		
		@Override
		public String toString() {
			return new String( "[project] " + p.getCode());
		}
		
		public String getIdentifier() {
			return p.getIdentifier();
		}
		
	}

	/**
	 * Wraps the Experiment class to overload its toString() method
	 * @author Aaron Ponti
	 *
	 */
	protected class ExperimentWrapper {

		private Experiment e;

		protected ExperimentWrapper(Experiment e) {
			this.e = e;
		}
		
		@Override
		public String toString() {
			return new String( "[experiment] " + e.getCode());
		}

		public String getIdentifier() {
			return e.getIdentifier();
		}
	}

	/**
	 * Wraps the Sample class to overload its toString() method
	 * @author Aaron Ponti
	 *
	 */
	protected class SampleWrapper {

		private Sample s;

		protected SampleWrapper(Sample s) {
			this.s = s;
		}
		
		@Override
		public String toString() {
			return new String( "[sample] " + s.getCode());
		}

		public String getIdentifier() {
			return s.getIdentifier();
		}

	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}	

}
