package ch.eth.scu.scuimporter.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * Graphical user interface to log in to openBIS and choose where to store
 * acquired datasets.
 * @author Aaron Ponti
 *
 */
public class OpenBISClient extends JFrame 
	implements ActionListener, TreeSelectionListener {

	private static final long serialVersionUID = 1L;

	private String userName = "";
	private String userPassword = "";
	private int timeout = 60000;
	private IOpenbisServiceFacade facade;
	
	private DefaultMutableTreeNode rootNode;
	private JTree tree;
	private JScrollPane treeView;
	
	private final static String openBISURL = "https://openbis-scu.ethz.ch/openbis/";

	/**
	 * Constructor
	 */
	public OpenBISClient() {

		// Call the frame's constructor
		super("openBIS client");

		// Try to use the system look and feel
		try {
			if (System.getProperty("os.name").equals("Mac OS X")) {
				UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
			} else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		} catch (Exception e) {
			System.err.println("Couldn't set system look and feel.");
		}

		setLayout(new GridLayout(1, 1, 0, 0));
		
		// Create the root node
		rootNode = new DefaultMutableTreeNode("Please login...");
		
		// Create a tree that allows one selection at a time.
		tree = new JTree(rootNode);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		// Create the scroll pane and add the tree to it. 
		treeView = new JScrollPane(tree);
		add(treeView);
			
		// Add menu
		JMenu menu = new JMenu("File");
		menu.add(makeMenuItem("Log in"));
		menu.add(makeMenuItem("Log out"));
		menu.add(makeMenuItem("Quit"));
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);
		setJMenuBar(menuBar);
		
		// Set exit on close
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Make sure to logout when exiting 
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	logout();
	        	System.exit(0);
	        }
	    });

		// Set up the frame
		setMinimumSize(new Dimension(300, 600));
		pack();
		setVisible(true);
		
		// Ask the user to login
		login();
	}
	/**
	 * Create menu entries
	 * @param String to be displayed for the menu entry
	 * @return a JMenuItem to be added to the menubar
	 */
	private JMenuItem makeMenuItem(String name) {
		JMenuItem m = new JMenuItem(name);
		m.setActionCommand(name);
		m.addActionListener(this);
		return m;
	}


	/**
	 * Login to openBIS. Credentials provided by the user through a dialog.
	 * @return true if login was successful, false otherwise.
	 */
	public boolean login() {

		// Check that user name and password were set
		if (userName.equals("") || userPassword.equals("")) {
			if (askForCredentials() == false) {
				return false;
			}
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
			return false;
		}

		// Fill in the user interface with the obtained data
		fillUIWithOpenBISData();
		
		return true;
	}

	/**
	 * Asks the user to enter credentials.
	 * @return true if the credentials were entered correctly, false otherwise.
	 */
	private boolean askForCredentials() {

		// Create a login dialog
		JTextField userField = new JTextField();
		JPasswordField passField = new JPasswordField();
		String message = "Please enter your user name and password.";
		int result = JOptionPane.showOptionDialog(this,
				new Object[] { message, userField, passField },
				"Login", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null, null, null);
		if (result == JOptionPane.OK_OPTION) {
			userName = userField.getText();
			userPassword = new String(passField.getPassword());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Log out from openBIS
	 * @return true if logging out was successful, false otherwise.
	 */
	public boolean logout() {
		if (facade != null) {
			facade.logout();
			return true;
		}
		return false;
	}

	/**
	 *  User chose Open or Quit on the context menu
	 */
	public void actionPerformed(ActionEvent e) {

		// React to the context menu
		if (e.getActionCommand().equals("Log in")) {
			if (login() == true) {
				System.out.println("Login successful.");
			} else {
				System.out.println("Login failed.");
			}
		} else if (e.getActionCommand().equals("Log out")) {
			if (logout() == true) {
				System.out.println("Log out successful.");
			} else {
				System.out.println("Log out failed.");
			}
		} else if (e.getActionCommand().equals("Quit")) {
			logout();
			System.exit(0);
		} else {
			return;
		}
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
	 * Fill the UI elements with the data obtained from openBIS
	 * @param spaceIndex: index of the space in the JComboBox.
	 * @param projectIndex: index of the project in the JComboBox
	 */
	private void fillUIWithOpenBISData() {

		DefaultMutableTreeNode space = null;
		DefaultMutableTreeNode project = null;
		DefaultMutableTreeNode experiment = null;
		
		// Do we have a connection with openBIS?
		if (facade == null) {
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
				project = new DefaultMutableTreeNode(p.getCode());
				space.add(project);
				
				List<String> expId = new ArrayList<String>();
				expId.add(p.getIdentifier());
				List<Experiment> experiments = 
						facade.listExperimentsForProjects(expId);
				
				for (Experiment e : experiments) {

					// Add the experiment
					experiment = new DefaultMutableTreeNode(e.getCode());
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

	protected class SpaceWrapper {

		private SpaceWithProjectsAndRoleAssignments s;

		protected SpaceWrapper(SpaceWithProjectsAndRoleAssignments s) {
			this.s = s;
		}
		
		@Override
		public String toString() {
			Set<Role> roles = s.getRoles(userName);
			String rolesStr = ", roles: ";
			for (Role r : roles) {
				rolesStr += ( r + "; " ); 
			}
			return new String( s.getCode() + rolesStr );
		}
	}
	
	/**
	 * Program entry point
	 * @param args Ignored
	 */
	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new OpenBISClient();
			}
		});
	}
}
