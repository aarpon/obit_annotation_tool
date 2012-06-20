package ch.eth.scu.scuimporter.gui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import ch.eth.scu.scuimporter.gui.OpenBISLoginDialog;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * Graphical user interface to log in to openBIS and choose where to store
 * acquired datasets.
 * @author Aaron Ponti
 *
 */
public class OpenBISSpaceViewer extends JPanel 
	implements ActionListener, TreeSelectionListener {

	private static final long serialVersionUID = 1L;

	private String userName = "";
	private String userPassword = "";
	private int timeout = 60000;
	private IOpenbisServiceFacade facade;
	
	private DefaultMutableTreeNode rootNode;
	private JTree tree;
	private JScrollPane treeView;
	private String defaultRootNodeString = "Please login to openBIS...";
	
	private final static String openBISURL = "https://openbis-scu.ethz.ch/openbis/";

	private boolean isLoggedIn = false;
	
	private OpenBISLoginDialog loginDialog;
	
	/**
	 * Constructor
	 */
	public OpenBISSpaceViewer() {

		// Set a grid bag layout
		setLayout(new GridBagLayout());

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
		GridBagConstraints treeViewConstraints = new GridBagConstraints();
		treeViewConstraints.anchor = GridBagConstraints.NORTHWEST;
		treeViewConstraints.gridx = 0;
		treeViewConstraints.gridy = 0;
		treeViewConstraints.weightx = 1.0;
		treeViewConstraints.weighty = 1.0;
		treeViewConstraints.fill = GridBagConstraints.BOTH;
		add(treeView, treeViewConstraints);

		// Set sizes
		setMinimumSize(new Dimension(300, 400));
		setPreferredSize(new Dimension(300, 400));
	}
	
	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 * Asks the user to enter credentials.
	 * @return true if the credentials were entered correctly, false otherwise.
	 */
	private boolean askForCredentials() {

		// Modal dialog: stops here until the dialog is disposed
		// (when a username and password have been provided)
		loginDialog = new OpenBISLoginDialog();
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
		
		// Do we have a connection with openBIS?
		if (facade == null || isLoggedIn == false) {
			return;
		}
		
		// TODO Check that the session is still open

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
				
				List<String> expId = new ArrayList<String>();
				expId.add(p.getIdentifier());
				List<Experiment> experiments = 
						facade.listExperimentsForProjects(expId);
				
				for (Experiment e : experiments) {

					// Add the experiment
					ExperimentWrapper experimentWrapper = new ExperimentWrapper(e);
					experiment = new DefaultMutableTreeNode(experimentWrapper);
					project.add(experiment);
				
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
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}	

}
