package ch.ethz.scu.obit.at.gui.dialogs;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ch.ethz.scu.obit.common.settings.AppSettingsManager;

/**
 * openBIS login dialog
 * @author Aaron Ponti
 */
public class OpenBISLoginDialog extends JDialog {
	
	/* Private instance variables */
	private static final long serialVersionUID = 1L;

	private JTextField txtUsername;
	private JPasswordField pwdPassword;
	
	private String openBISServerURL;
	private ArrayList<String> servers;

	/**
	 * Constructor
	 */
	public OpenBISLoginDialog() {
		
		// Get the list of configured servers and set the default one
		AppSettingsManager manager = new AppSettingsManager();
		servers = manager.getAllServers();
		openBISServerURL = servers.get(0);
		
		// Use a variable to keep track of where we are in the layout
		int gridY = 0;
		
		// Make the dialog modal and not resizable
		setModal(true);
		setResizable(false);
		
		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		if (servers.size() > 1) {
			
			// Add a menu
			JLabel serverMenu = new JLabel("...");
			serverMenu.setOpaque(true);
			serverMenu.setBackground(Color.white);
			serverMenu.setHorizontalAlignment(SwingConstants.LEFT);
			serverMenu.setBorder(new EmptyBorder(3, 3, 0, 0));
			serverMenu.addMouseListener((new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent arg0) {
					// Modal dialog
					OpenBISPickServerDialog dialog =
							new OpenBISPickServerDialog(servers,
									openBISServerURL);
					openBISServerURL = dialog.getOpenBISServer();
				}

				@Override
				public void mouseEntered(MouseEvent arg0) {
				}

				@Override
				public void mouseExited(MouseEvent arg0) {
				}

				@Override
				public void mousePressed(MouseEvent arg0) {
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
				}
			}));
			GridBagConstraints serverMenuCnstr = new GridBagConstraints();
			serverMenuCnstr.gridx = 0;
			serverMenuCnstr.gridy = gridY++;
			serverMenuCnstr.insets = new Insets(0, 0, 0, 0);
			serverMenuCnstr.fill = GridBagConstraints.HORIZONTAL;
			add(serverMenu, serverMenuCnstr);
		}
		
		// Add the SCU OpenBIS logo at the top
		JLabel labelLogo = new JLabel(new ImageIcon(
				this.getClass().getResource("logo.png")));
		GridBagConstraints logoConstraints = new GridBagConstraints();
		logoConstraints.anchor = GridBagConstraints.NORTHWEST;
		logoConstraints.gridx = 0;
		logoConstraints.gridy = gridY++;
		add(labelLogo, logoConstraints);
		
		// Add a label for the user name
		JLabel labelUsername = new JLabel("User name");
		GridBagConstraints usernameCnstr = new GridBagConstraints();
		usernameCnstr.gridx = 0;
		usernameCnstr.gridy = gridY++;
		usernameCnstr.insets = new Insets(10, 0, 0, 0);	
		add(labelUsername, usernameCnstr);

		// Create a text field for the user name
		txtUsername = new JTextField();
		txtUsername.setHorizontalAlignment(JTextField.CENTER);
		GridBagConstraints txtUsernameCnstr = new GridBagConstraints();
		txtUsernameCnstr.fill = GridBagConstraints.HORIZONTAL;
		txtUsernameCnstr.gridx = 0;
		txtUsernameCnstr.gridy = gridY++;
		txtUsernameCnstr.ipady = 10;
		txtUsernameCnstr.insets = new Insets(5, 30, 0, 30);	
		add(txtUsername, txtUsernameCnstr);
		
		// Add a label for the password
		JLabel labelPassword = new JLabel("Password");
		GridBagConstraints passwordCnstr = new GridBagConstraints();
		passwordCnstr.gridx = 0;
		passwordCnstr.gridy = gridY++;
		passwordCnstr.insets = new Insets(10, 0, 0, 0);	
		add(labelPassword, passwordCnstr);
		
		// Create a field for the user password
		pwdPassword = new JPasswordField();
		pwdPassword.setHorizontalAlignment(JPasswordField.CENTER);
		GridBagConstraints pwdPasswordCnstr = new GridBagConstraints();
		pwdPasswordCnstr.fill = GridBagConstraints.HORIZONTAL;
		pwdPasswordCnstr.gridx = 0;
		pwdPasswordCnstr.gridy = gridY++;
		pwdPasswordCnstr.ipady = 10;
		pwdPasswordCnstr.insets = new Insets(5, 30, 0, 30);	
		add(pwdPassword, pwdPasswordCnstr);

		// Create a login button
		JButton loginButton = new JButton("Login");
		GridBagConstraints loginButtonCnstr = new GridBagConstraints();
		loginButtonCnstr.fill = GridBagConstraints.HORIZONTAL;
		loginButtonCnstr.gridx = 0;
		loginButtonCnstr.gridy = gridY++;
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
	
	/**
	 * Get the entered password
	 * @return password entered in the dialog 
	 */
	public String getOpenBISServer() {
		return openBISServerURL;
	}

}
