package ch.eth.scu.scuimporter.gui;

import java.util.Collections;
import java.util.List;

import javax.swing.*;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

/**
 * Graphical user interface to log in to openBIS and choose where to store
 * acquired datasets.
 * @author Aaron Ponti
 *
 */
public class OpenBISClient extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private String userName = "";
	private String userPassword = "";
	private int timeout = 60000;
	IOpenbisServiceFacade facade = null;
	
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

		// Set up the frame
		setSize(600, 600);
		setVisible(true);
		
		// Log in
		if (login() == true ) {
			System.out.println("Login successful.");
			
			// Display spaces
			for ( SpaceWithProjectsAndRoleAssignments s : getSpaces()) {
				System.out.println("Space: " + s.getCode());
				for (Project p : s.getProjects()) {
					System.out.println("|_ Project: " + p.getIdentifier());
				}
				
			}
			
		} else {
			System.out.println("Login failed.");
		}
		
		// Log out
		if (logout() == true) {
			System.out.println("Log out successful.");
		} else {
			System.out.println("Log out failed.");
		}

		// Set exit on close
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
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
			return false;
		}

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
		int result = JOptionPane.showOptionDialog(null,
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
		if (facade != null ) {
			facade.logout();
			return true;
		}
		return false;
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
