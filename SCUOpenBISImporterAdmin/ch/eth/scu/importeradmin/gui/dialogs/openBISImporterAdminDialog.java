package ch.eth.scu.importeradmin.gui.dialogs;

import javax.swing.*;

import ch.eth.scu.importer.properties.AppProperties;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * SCUOpenBISImporter administrator Dialog
 * @author Aaron Ponti
 *
 */
public class openBISImporterAdminDialog extends JDialog 
	implements ActionListener {
	
	/* Private instance variables */
	private static final long serialVersionUID = 1L;

	protected String selAcqStation = "";
	protected String selIncomingDir = "";
	
	protected JButton dirButton;
	protected JButton saveButton;
	protected JButton cancelButton;
	protected JComboBox acqStationsList;
	
	/**
	 * Constructor
	 */
	public openBISImporterAdminDialog() {
		
		// Read the properties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		
		// Make the dialog modal and not resizable
		setModal(true);
		setResizable(false);
		
		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		// Constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
				
		// Add a label for the selection of the acquisition machine
		JLabel acqLabel = new JLabel("Select the acquisition station");
		add(acqLabel, constraints);

		// Add a drop-down menu for the selection of the acquisition machine
		String acqStation = appProperties.getProperty("AcquisitionStation");
		String[] acqStations = { "LSRFortessa", "LeicaSP5" };
		int index = -1;
		for (int i = 0; i < acqStations.length; i++) {
			if (acqStations[i].equals(acqStation)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			System.err.println("Unknown acquisition station! Defaulting to " +
					acqStations[0] + ".");
			index = 0;
		}
		acqStationsList = new JComboBox(acqStations);
		acqStationsList.setSelectedIndex(index);
		selAcqStation = acqStations[index];
		acqStationsList.addActionListener(this);
		constraints.gridy = 1;
		add(acqStationsList, constraints);
		
		// Add a label for the directory
		JLabel dirLabel = new JLabel("Data mover incoming directory");
		constraints.gridy = 2;
		add(dirLabel, constraints);
		
		// Add a pushButton to choose the directory
		// Create a text field for the user name
		selIncomingDir = appProperties.getProperty("DatamoverIncomingDir");
		dirButton = new JButton(selIncomingDir);
		constraints.gridy = 3;
		dirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String dir = pickDir();
            	if (!dir.equals("")) {
            		selIncomingDir = dir;
            		dirButton.setText(selIncomingDir);
            	}
            }
        });		
		add(dirButton, constraints);

		// Create a Save button
		saveButton = new JButton("Save");
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            	// The acquisition station is always set; we make sure
            	// that the user also picked an incoming directory
            	if (selIncomingDir.equals("")) {
            		JOptionPane.showMessageDialog(null, 
            				"Please set the incoming directory!", "Error", 
            				JOptionPane.ERROR_MESSAGE);

            		return;
            	}

            	// Save the selection to the properties file
            	if (saveProperties() == false) {
            		JOptionPane.showMessageDialog(null, 
            				new String( "Could not save settings! " +
            		"Make sure you have administrator rights!"), "Error", 
            				JOptionPane.ERROR_MESSAGE);
            	} else {
            	
            		setVisible(false);
            		dispose();
            	}
            }
        });
		add(saveButton, constraints);

		// Create a cancel button
		cancelButton = new JButton("Cancel");
		constraints.gridx = 1;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	// If we cancel the admin mode, we quit the application
            	// TODO Ask the user for confirmation
            	setVisible(false);
            	dispose();
            	System.exit(0);
            }
        });
		add(cancelButton, constraints);
		
		// Make the login button react to the enter key
		getRootPane().setDefaultButton(saveButton);

		// Display the dialog
		setMinimumSize(new Dimension(600, 50));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setVisible(true);
		
	}

	/**
	 * Implement the actionPerformed method
	 * @param ActionEvent 
	 */	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			selAcqStation = (String) acqStationsList.getSelectedItem();
		}
	}
	
	/**
	 * Asks the user to pick a directory
	 * @return the absolute path of the selected directory
	 */
	private String pickDir() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Set the datamover incoming directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return (chooser.getSelectedFile().getAbsolutePath());
		} else {
			return "";
		}
	}

	/**
	 * Saves current properties to file
	 * @return true if saving was successful, false otherwise
	 */
	private boolean saveProperties() {
		
		// Check that everything is set
		if (selAcqStation.equals("") || selIncomingDir.equals("")) {
			return false;
		}
		
		// Save the properties to file
		return AppProperties.savePropertiesToFile(selAcqStation, selIncomingDir);
	}
	
}
