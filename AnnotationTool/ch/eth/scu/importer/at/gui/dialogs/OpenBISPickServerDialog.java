package ch.eth.scu.importer.at.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * openBIS pick server dialog
 * @author Aaron Ponti
 */
public class OpenBISPickServerDialog extends JDialog {
	
	/* Private instance variables */
	private static final long serialVersionUID = 1L;

	protected JComboBox<String> openBISURLList;
	
	private String currentServer;
	private String selectedServer;
	
	/**
	 * Constructor
	 */
	public OpenBISPickServerDialog(ArrayList<String> servers, String current) {

		// Store the currently selected server and keep track of
		// what the original selection was in case the user cancels
		// the selection.
		currentServer = current;
		selectedServer = current;

		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);

		// Common constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		
		// Make the dialog modal and not resizable
		setModal(true);
		setResizable(false);
	
		// Add a label for the selection of the URL
		JLabel label = new JLabel("Please choose the server you want to use:");
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.weightx = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
        add(label, constraints);
		
        // Add a drop-down menu for the selection of the URL
        openBISURLList = new JComboBox<String>();
        for (String server : servers) {
            openBISURLList.addItem(server);
        }
        openBISURLList.setSelectedItem(current);
        openBISURLList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxChanged")) {
                	selectedServer = (String) openBISURLList.getSelectedItem();
                }
            }
        });
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 3;
		constraints.weightx = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
        add(openBISURLList, constraints);

        // Add a spacer
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;			
		constraints.weightx = 0.33;
		constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
		add(new JLabel(""), constraints);

		// Create an OK button
		JButton okButton = new JButton("OK");
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;			
		constraints.weightx = 0.33;
		constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
		okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	setVisible(false);
        		dispose();
            }
        });
		add(okButton, constraints);

		// Create a cancel button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	// Restore the original server
            	selectedServer = currentServer;
            	setVisible(false);
            	dispose();
            }
        });
		constraints.gridx = 2;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;			
		constraints.weightx = 0.33;
		constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
		add(cancelButton, constraints);

		// Make the ok button react to the enter key
		getRootPane().setDefaultButton(okButton);

		// If the user just closes the login dialog without trying to log in
		// we ask him if he wants to quit the application, otherwise we insist
		// that he logs in since we need valid login credentials for the proper
		// functioning of the application.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// Restore the original server
				selectedServer = currentServer;
				setVisible(false);
				dispose();
			}
		});
		
		// Display the dialog
		setMinimumSize(new Dimension(350, 120));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setVisible(true);
	}
	
	/**
	 * Get the entered password
	 * @return password entered in the dialog 
	 */
	public String getOpenBISServer() {
		return selectedServer;
	}

}
