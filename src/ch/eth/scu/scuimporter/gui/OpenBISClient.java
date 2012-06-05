package ch.eth.scu.scuimporter.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import java.awt.GridLayout;

/**
 * Graphical user interface to log in to openBIS and choose where to store
 * acquired datasets.
 * @author Aaron Ponti
 *
 */
public class OpenBISClient extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private String userName = "";
	private String userPassword = "";
	private int timeout = 60000;
	IOpenbisServiceFacade facade = null;
	
	private JLabel labelSpaces, labelProjects, labelExperiments;
	private JComboBox comboSpaces, comboProjects, comboExperiments;

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

		setLayout(new GridLayout(6, 1, 0, 0));
		
		labelSpaces = new JLabel("Spaces");
		add(labelSpaces);
		
		comboSpaces = new JComboBox();
		comboSpaces.setEnabled(false);
		add(comboSpaces);
		
		labelProjects = new JLabel("Projects");
		add(labelProjects);
		
		comboProjects = new JComboBox();
		comboProjects.setEnabled(false);
		add(comboProjects);
		
		labelExperiments = new JLabel("Experiments");
		add(labelExperiments);
		
		comboExperiments = new JComboBox();
		comboExperiments.setEnabled(false);
		add(comboExperiments);

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

		// Set up the frame
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
			facade = OpenbisServiceFacadeFactory.tryCreate(userName, userPassword, openBISURL, timeout);
		} catch (UserFailureException e) {
			JOptionPane.showMessageDialog(this, "Login failed. Please try again.");
			userName = "";
			userPassword = "";
			return false;
		}

		// Fill in the user interface with the obtained data
		fillUIWithOpenBISData();
		
		return true;
	}

	/**
	 * Return a list of spaces with projects
	 * @return list of spaces with projects and role assignments
	 */
	public List<SpaceWithProjectsAndRoleAssignments> getSpaces() {
		if (facade == null) {
			// Use this syntax to prevent error from Javadoc
			List<SpaceWithProjectsAndRoleAssignments> l = Collections.emptyList();
			return l;

			// Note: Javadoc gives an error on this (valid) syntax:
			// return (Collections.<SpaceWithProjectsAndRoleAssignments>emptyList());
		}
		return (facade.getSpacesWithProjects());
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
	 * Fill the UI elements with the data obtained from openBIS 
	 */
	private void fillUIWithOpenBISData() {
		
		// Fill in the spaces combo box
		List<SpaceWithProjectsAndRoleAssignments> spaces = getSpaces(); 
		DefaultComboBoxModel dcm = new DefaultComboBoxModel();
		for(SpaceWithProjectsAndRoleAssignments s : spaces) {
		    dcm.addElement(s.getCode());
		}
		comboSpaces.setModel(dcm);
		comboSpaces.setEnabled(true);
		
		// Fill in the projects combo box
		if (spaces.size() == 0) {
			return;
		}
		List<Project> projects = spaces.get(0).getProjects();
		dcm = new DefaultComboBoxModel();
		for(Project p : projects) {
		    dcm.addElement(p.getCode());
		}
		comboProjects.setModel(dcm);
		comboProjects.setEnabled(true);
		
		// Now fill in the experiments combo box
		List<String> expId = new ArrayList<String>();
		System.out.println("Getting experiments from " + spaces.get(0).getCode() + "/" + projects.get(0).getCode());
		expId.add(spaces.get(0).getCode() + "/" + projects.get(0).getCode());
		try {
			List<Experiment> exp = facade.getExperiments(expId);
			for (Experiment e : exp) {
				dcm.addElement(e.getCode());
			}
			comboExperiments.setModel(dcm);
			comboExperiments.setEnabled(true);		
		} catch ( UserFailureException u ) {
			System.out.println("No experiments found with ID " + expId);
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
