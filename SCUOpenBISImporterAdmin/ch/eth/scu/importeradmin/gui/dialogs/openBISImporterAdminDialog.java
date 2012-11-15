package ch.eth.scu.importeradmin.gui.dialogs;

import javax.swing.*;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.common.properties.DefaultProperties;

import java.io.File;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

/**
 * SCUOpenBISImporter administrator Dialog
 * @author Aaron Ponti
 */
public class openBISImporterAdminDialog extends JDialog {
	
	/* Private instance variables */
	private static final long serialVersionUID = 1L;

	protected String selAcqStation = "";
	protected String selIncomingDir = "";
	protected String selOpenBISURL = "";
	
	protected JButton dirButton;
	protected JButton saveButton;
	protected JButton cancelButton;
	protected JComboBox acqStationsList;
	protected JComboBox openBISURLList;
	
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

		// Add a label for the selection of the openBIS URL
		JLabel urlLabel = new JLabel("Set the openBIS URL");
		add(urlLabel, constraints);

		// Add a drop-down menu for the selection of the URL
		String openBISURL = appProperties.getProperty("OpenBISURL");
		ArrayList<String> openBISURLOptions = 
				DefaultProperties.possibleValuesForProperty("OpenBISURL");
		int index = -1;
		for (int i = 0; i < openBISURLOptions.size(); i++) {
			if (openBISURLOptions.get(i).equals(openBISURL)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			System.err.println("Unknown openBIS URL! Defaulting to " +
					DefaultProperties.defaultValueForProperty("OpenBISURL") +
					".");
			index = 0;
		}
		openBISURLList = new JComboBox(openBISURLOptions.toArray());
		openBISURLList.setSelectedIndex(index);
		selOpenBISURL = openBISURLOptions.get(index);
		openBISURLList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		if (e.getActionCommand().equals("comboBoxChanged")) {
        			selOpenBISURL = (String) openBISURLList.getSelectedItem();
        		}
            }
        });
		constraints.gridy = 1;
		add(openBISURLList, constraints);		
		
		// Add a label for the selection of the acquisition machine
		JLabel acqLabel = new JLabel("Select the acquisition station");
		constraints.gridy = 2;
		add(acqLabel, constraints);
		
		// Add a drop-down menu for the selection of the acquisition machine
		String acqStation = appProperties.getProperty("AcquisitionStation");
		ArrayList<String> acqStations = 
				DefaultProperties.possibleValuesForProperty("AcquisitionStation");
		index = -1;
		for (int i = 0; i < acqStations.size(); i++) {
			if (acqStations.get(i).equals(acqStation)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			System.err.println("Unknown acquisition station! Defaulting to " +
					DefaultProperties.defaultValueForProperty("AcquisitionStation") +
					".");
			index = 0;
		}
		acqStationsList = new JComboBox(acqStations.toArray());
		acqStationsList.setSelectedIndex(index);
		selAcqStation = acqStations.get(index);
		acqStationsList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		if (e.getActionCommand().equals("comboBoxChanged")) {
        			selAcqStation = (String) acqStationsList.getSelectedItem();
        		}
            }
        });
		constraints.gridy = 3;
		add(acqStationsList, constraints);
		
		// Add a label for the directory
		JLabel dirLabel = new JLabel("Data mover incoming directory");
		constraints.gridy = 4;
		add(dirLabel, constraints);
		
		// Add a pushButton to choose the directory
		// Create a text field for the user name
		selIncomingDir = appProperties.getProperty("DatamoverIncomingDir");
		dirButton = new JButton(selIncomingDir);
		constraints.gridy = 5;
		dirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String dir = pickDir();
            	if (!dir.equals("")) {
            		try {
						selIncomingDir = (new File(dir)).getCanonicalPath();
					} catch (IOException exc) {
						// This really should not happen
						selIncomingDir = "";
						JOptionPane.showMessageDialog(null,
							    "Invalid directory chosen!",
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
					}
            		dirButton.setText(selIncomingDir);
            	}
            }
        });		
		add(dirButton, constraints);

		// Create a Save button
		saveButton = new JButton("Save");
		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.gridwidth = 1;
		saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            	// The acquisition station and the openBIS URL are always set;
            	// we make sure that the user also picked an incoming directory
            	if (selIncomingDir.equals("")) {
            		JOptionPane.showMessageDialog(null, 
            				"Please set the incoming directory!", "Error", 
            				JOptionPane.ERROR_MESSAGE);

            		return;
            	}

            	// Save the selection to the properties file
            	if (!saveProperties()) {
            		JOptionPane.showMessageDialog(null,
                            "Could not save settings! " +
                                    "Make sure you have administrator rights!",
                            "Error",
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
		constraints.gridy = 6;
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
		if (selOpenBISURL.equals("") || selAcqStation.equals("") || 
				selIncomingDir.equals("")) {
			return false;
		}
		
		// Save the properties to file
		return AppProperties.writePropertiesToFile(selOpenBISURL, 
				selAcqStation, selIncomingDir);
	}
	
}
