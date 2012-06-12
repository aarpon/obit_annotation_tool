package ch.eth.scu.scuimporter.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class OpenBISLoginDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JLabel headerLabel;
	private CredentialsPanel credentialsPanel;
	private JButton loginButton;
	private boolean credentialsSet = false;

	/**
	 * Constructor
	 * @param owner parent window
	 */
	public OpenBISLoginDialog(Window owner) {

		// Make the dialog modal
		setModal(true);

		// Header
		headerLabel = new JLabel(
				new ImageIcon(this.getClass().getResource("login.png")));
		headerLabel.setSize(new Dimension(500, 200));

		// Add the widgets
		setLayout(new GridLayout(3, 1, 0, 0));
		add(headerLabel);
		credentialsPanel = new CredentialsPanel(); 
		add(credentialsPanel);
		loginButton = new JButton("Login");
		loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (getUsername().equals("") == false && 
            			getPassword().equals("") == false) {
            		credentialsSet = true;
            		setVisible(false);
            		dispose();
            	}
            }
        });
		add(loginButton);
		
		// Make the login button react to enter key
		getRootPane().setDefaultButton(loginButton);

		// Display the dialog
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setVisible(true);
	}

	/**
	 * Check if the credentials were entered
	 * @return true if the credentials were entered, false otherwise
	 */
	public boolean areCredentialsSet() {
		return credentialsSet;
	}
	
	/**
	 * Get the entered user name
	 * @return user name entered in the dialog 
	 */
	public String getUsername() {
		return credentialsPanel.userField.getText();
	}
	
	/**
	 * Get the entered password
	 * @return password entered in the dialog 
	 */
	public String getPassword() {
		return (new String(credentialsPanel.passField.getPassword()));
	}
	
	/**
	 * TODO Replace this with a better layout
	 */
	private class CredentialsPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private JTextField userField;
		private JPasswordField passField;
		
		GridBagConstraints constraints = new GridBagConstraints();

		private CredentialsPanel() {

			GridBagLayout gridbagLayout = new GridBagLayout(); 
			setLayout(gridbagLayout);

			Border padding = BorderFactory.createEmptyBorder(20,20,5,20);
			setBorder(padding);

			userField = new JTextField();
			passField = new JPasswordField();

			constraints.fill = GridBagConstraints.BOTH;
			constraints.weighty = 1.0;
			constraints.weightx = 1.0;

			constraints.weightx = 0.2;
			addGB(new JLabel("User name:"), 0, 0);
			addGB(new JLabel("Password:"), 0, 1);
			constraints.weightx = 0.8;
			addGB(userField, 1, 0);
			addGB(passField, 1, 1);
		}
		
		void addGB(Component component, int x, int y) {
			constraints.gridx = x;
			constraints.gridy = y;
			add(component, constraints);
		}
	}
}
