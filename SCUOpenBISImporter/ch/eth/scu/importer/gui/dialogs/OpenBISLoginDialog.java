package ch.eth.scu.importer.gui.dialogs;

import javax.swing.*;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * openBIS login dialog
 * @author Aaron Ponti
 */
public class OpenBISLoginDialog extends JDialog {
	
	/* Private instance variables */
	private static final long serialVersionUID = 1L;

	private JTextField txtUsername;
	private JPasswordField pwdPassword;

	/**
	 * Constructor
	 */
	public OpenBISLoginDialog() {
		
		// Make the dialog modal and not resizable
		setModal(true);
		setResizable(false);
		
		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		// Add the SCU OpenBIS logo at the top
		JLabel labelLogo = new JLabel(new ImageIcon(
				this.getClass().getResource("logo.png")));
		GridBagConstraints logoConstraints = new GridBagConstraints();
		logoConstraints.anchor = GridBagConstraints.NORTHWEST;
		logoConstraints.gridx = 0;
		logoConstraints.gridy = 0;
		add(labelLogo, logoConstraints);
		
		// Add a label for the user name
		JLabel labelUsername = new JLabel("User name");
		GridBagConstraints usernameCnstr = new GridBagConstraints();
		usernameCnstr.gridx = 0;
		usernameCnstr.gridy = 1;
		usernameCnstr.insets = new Insets(10, 0, 0, 0);	
		add(labelUsername, usernameCnstr);

		// Create a text field for the user name
		txtUsername = new JTextField();
		txtUsername.setHorizontalAlignment(JTextField.CENTER);
		GridBagConstraints txtUsernameCnstr = new GridBagConstraints();
		txtUsernameCnstr.fill = GridBagConstraints.HORIZONTAL;
		txtUsernameCnstr.gridx = 0;
		txtUsernameCnstr.gridy = 2;
		txtUsernameCnstr.ipady = 10;
		txtUsernameCnstr.insets = new Insets(5, 30, 0, 30);	
		add(txtUsername, txtUsernameCnstr);
		
		// Add a label for the password
		JLabel labelPassword = new JLabel("Password");
		GridBagConstraints passwordCnstr = new GridBagConstraints();
		passwordCnstr.gridx = 0;
		passwordCnstr.gridy = 3;
		passwordCnstr.insets = new Insets(10, 0, 0, 0);	
		add(labelPassword, passwordCnstr);
		
		// Create a field for the user password
		pwdPassword = new JPasswordField();
		pwdPassword.setHorizontalAlignment(JPasswordField.CENTER);
		GridBagConstraints pwdPasswordCnstr = new GridBagConstraints();
		pwdPasswordCnstr.fill = GridBagConstraints.HORIZONTAL;
		pwdPasswordCnstr.gridx = 0;
		pwdPasswordCnstr.gridy = 4;
		pwdPasswordCnstr.ipady = 10;
		pwdPasswordCnstr.insets = new Insets(5, 30, 0, 30);	
		add(pwdPassword, pwdPasswordCnstr);

		// Create a login button
		JButton loginButton = new JButton("Login");
		GridBagConstraints loginButtonCnstr = new GridBagConstraints();
		loginButtonCnstr.fill = GridBagConstraints.HORIZONTAL;
		loginButtonCnstr.gridx = 0;
		loginButtonCnstr.gridy = 5;
		loginButtonCnstr.ipady = 15;
		loginButtonCnstr.insets = new Insets(10, 3, 3, 3);
		loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (!getUsername().equals("") &&
            			!getPassword().equals("")) {
            		setVisible(false);
            		dispose();
            	}
            }
        });
		add(loginButton, loginButtonCnstr);
		
		// Make the login button react to the enter key
		getRootPane().setDefaultButton(loginButton);

		// If the user just closes the login dialog without trying to log in
		// we ask him if he wants to quit the application, otherwise we insist
		// that he logs in since we need valid login credentials for the proper
		// functioning of the application.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				int answer = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to quit the application?",
						"Question", JOptionPane.YES_NO_OPTION);
				if (answer ==  JOptionPane.YES_OPTION) {
					System.exit(1);
				}
				setVisible(true);
			}
		});
		
		// Display the dialog
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setVisible(true);
		
	}

	/**
	 * Get the entered user name
	 * @return user name entered in the dialog 
	 */
	public String getUsername() {
		return txtUsername.getText();
	}
	
	/**
	 * Get the entered password
	 * @return password entered in the dialog 
	 */
	public String getPassword() {
		return (new String(pwdPassword.getPassword()));
	}
	
}
