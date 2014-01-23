package ch.eth.scu.importer.atadmin.gui.dialogs;

import javax.swing.*;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.common.properties.DefaultProperties;
import ch.eth.scu.importer.common.version.VersionInfo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

/**
 * AnnotationTool administrator Dialog
 * @author Aaron Ponti
 */
public class AnnotationToolAdminDialog extends JDialog {
	
	/* Private instance variables */
	private static final long serialVersionUID = 1L;

	protected String selAcqStation;
	protected String selAcceptSelfSignedCerts;
	protected String selIncomingDir;
	protected String selUserDataDir;
	protected String selOpenBISURL;

	protected JTextField openBISURLInput;
	protected JButton dirButton;
	protected JButton userdirButton;
	protected JButton saveButton;
	protected JButton closeButton;
	protected JComboBox<Object> acqStationsList;
	protected JComboBox<Object> acceptSelfSignedCertsList;
	
	/**
	 * Constructor
	 */
	public AnnotationToolAdminDialog() {

		// Set the dialog title
		setTitle("openBIS Importer Toolset :: Annotation Tool Admin v" +
		VersionInfo.version + " " + VersionInfo.status);

		// Read the properties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		boolean isUpToDate = 
				AppProperties.isPropertiesFileVersionCurrent(appProperties);
		if (appProperties == null || !isUpToDate) {
			appProperties = AppProperties.initializePropertiesFile();
			if (appProperties == null) {
				JOptionPane.showMessageDialog(null,
						"Could not save application settings to disk!",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}

		// Make the dialog modal and not resizable
		setModal(true);
		setResizable(false);

		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);

		// Common constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.BOTH;
		
		// Add a label for the selection of the openBIS URL
		JLabel urlLabel = new JLabel("Set the openBIS URL");
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 8;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(urlLabel, constraints);

		// Add a formatted text info that validates URLs
		String openBISURL = appProperties.getProperty("OpenBISURL");
	    openBISURLInput = new JTextField(openBISURL);
	    openBISURLInput.setHorizontalAlignment(JTextField.CENTER);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 8;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(openBISURLInput, constraints);
	
		// Add a label for the options of accepting self-signed
		// certificates
		JLabel certLabel = new JLabel("Accept self-signed SSL certificates "
				+ "when logging in to openBIS");
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 8;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(certLabel, constraints);
		
		// Add a drop-down menu for the options of accepting self-signed
		// certificates
		String acceptSelfSignedCerts =
				appProperties.getProperty("AcceptSelfSignedCertificates");
		ArrayList<String> acceptSelfSignedCertsOptions = 
				DefaultProperties.possibleValuesForProperty(
						"AcceptSelfSignedCertificates");
		int index = -1;
		for (int i = 0; i < acceptSelfSignedCertsOptions.size(); i++) {
			if (acceptSelfSignedCertsOptions.get(i).equals(
					acceptSelfSignedCerts)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			String msg = "Unknown option for accepting self-signed " + 
					"certificates! Defaulting to \"" +
					DefaultProperties.defaultValueForProperty(
							"AcceptSelfSignedCertificates") +
					"\".";
			JOptionPane.showMessageDialog(null, msg, "Error",
				    JOptionPane.ERROR_MESSAGE);
			index = 0;
		}
		acceptSelfSignedCertsList = new JComboBox<Object>(acceptSelfSignedCertsOptions.toArray());
		acceptSelfSignedCertsList.setSelectedIndex(index);
		selAcceptSelfSignedCerts = acceptSelfSignedCertsOptions.get(index);
		acceptSelfSignedCertsList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		if (e.getActionCommand().equals("comboBoxChanged")) {
        			selAcceptSelfSignedCerts = (String) acceptSelfSignedCertsList.getSelectedItem();
        		}
            }
        });
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 8;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		add(acceptSelfSignedCertsList, constraints);

		// Add a label for the selection of the acquisition machine
		JLabel acqLabel = new JLabel("Select the acquisition station or type");
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 8;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
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
			String msg = "Unknown acquisition station! Defaulting to " +
					DefaultProperties.defaultValueForProperty("AcquisitionStation") +
					".";
			JOptionPane.showMessageDialog(null, msg, "Error",
				    JOptionPane.ERROR_MESSAGE);			
			System.err.println("Unknown acquisition station! Defaulting to " +
					DefaultProperties.defaultValueForProperty("AcquisitionStation") +
					".");
			index = 0;
		}
		acqStationsList = new JComboBox<Object>(acqStations.toArray());
		acqStationsList.setSelectedIndex(index);
		selAcqStation = acqStations.get(index);
		acqStationsList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		if (e.getActionCommand().equals("comboBoxChanged")) {
        			selAcqStation = (String) acqStationsList.getSelectedItem();
        		}
            }
        });
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.gridwidth = 8;
		constraints.gridheight = 1;		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(acqStationsList, constraints);

		// Add a label for the user directory
		JLabel userdirLabel = new JLabel("Set user data directory");
		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.gridwidth = 8;
		constraints.gridheight = 1;		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(userdirLabel, constraints);
		
		// Add a pushButton to choose the user directory
		// Create a text field for the user name
		selUserDataDir = appProperties.getProperty("UserDataDir");
		userdirButton = new JButton(selUserDataDir);
		userdirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String dir = pickDir("Set the user data directory");
            	if (!dir.equals("")) {
            		try {
						selUserDataDir = (new File(dir)).getCanonicalPath();
					} catch (IOException exc) {
						// This really should not happen
						selUserDataDir = "";
						JOptionPane.showMessageDialog(null,
							    "Invalid directory chosen!",
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
					}
            		userdirButton.setText(selUserDataDir);
            		pack();
            	}
            }
        });		
		constraints.gridx = 0;
		constraints.gridy = 7;
		constraints.gridwidth = 8;
		constraints.gridheight = 1;		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(userdirButton, constraints);

		// Add a label for the directory
		JLabel dirLabel = new JLabel("Set Datamover incoming directory");
		constraints.gridx = 0;
		constraints.gridy = 8;
		constraints.gridwidth = 8;
		constraints.gridheight = 1;		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(dirLabel, constraints);
		
		// Add a pushButton to choose the directory
		// Create a text field for the user name
		selIncomingDir = appProperties.getProperty("DatamoverIncomingDir");
		dirButton = new JButton(selIncomingDir);
		dirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String dir = pickDir("Set the datamover incoming directory");
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
            		pack();
            	}
            }
        });		
		constraints.gridx = 0;
		constraints.gridy = 9;
		constraints.gridwidth = 8;
		constraints.gridheight = 1;		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(dirButton, constraints);
		
		// Add a label for the info text
		JLabel infoLabel = new JLabel(
				"<html>It is <b>highly recommended</b> to set " +
		"both folders on the same file system.</html>");
		constraints.gridx = 0;
		constraints.gridy = 10;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(infoLabel, constraints);
		
		// Some spacer
		JLabel spacerLabel = new JLabel("");
		constraints.gridx = 0;
		constraints.gridy = 11;
		constraints.gridwidth = 4;
		constraints.gridheight = 1;		
		constraints.weightx = 0.5;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(spacerLabel, constraints);

		// Create a Save button
		saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            	// The acquisition station, the openBIS URL and the
            	// option for accepting self-signed certificates are always set;
            	// we make sure that the user also picked an user data directory
            	if (selUserDataDir.equals("")) {
            		JOptionPane.showMessageDialog(null,
            				"Please set the user data directory!", "Error",
            				JOptionPane.ERROR_MESSAGE);

            		return;
            	}

            	// ... and also the Datamover incoming directory
            	if (selIncomingDir.equals("")) {
            		JOptionPane.showMessageDialog(null,
            				"Please set the incoming directory!", "Error",
            				JOptionPane.ERROR_MESSAGE);

            		return;
            	}

            	// Save the selection to the properties file. We leave eventual error messages 
            	// to be displayed by savePorperties() or the functions called by it.
            	if (!saveProperties()) {
            		return;
            	} else {

            		saveButton.setText("<html><b>Saved!</b></html>");
            		ActionListener ls = new ActionListener() {
          		      public void actionPerformed(ActionEvent evt) {
                     		saveButton.setText("Save");
             		      }
              		  };
            		Timer timer = new Timer(2000, ls);
            		timer.setRepeats(false);
            		timer.start();

            }
          }
        });
		constraints.gridx = 4;
		constraints.gridy = 11;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;		
		constraints.weightx = 0.25;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(saveButton, constraints);
		
		// Create a close button
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	setVisible(false);
            	dispose();
            	System.exit(0);
            }
        });
		constraints.gridx = 6;
		constraints.gridy = 11;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;			
		constraints.weightx = 0.25;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(closeButton, constraints);
		
		// Make the login button react to the enter key
		getRootPane().setDefaultButton(saveButton);

		// Display the dialog
		setMinimumSize(new Dimension(600, 220));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);

	}

	/**
	 * Asks the user to pick a directory
	 * @return the absolute path of the selected directory
	 */
	private String pickDir(String dialogTitle) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle(dialogTitle);
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
		
		// Get the openBIS URL
		selOpenBISURL = openBISURLInput.getText();
		
		// Is the URL a valid URL?
		try {
			new URL(selOpenBISURL);
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(null,
					"Malformed openBIS server URL", "Error",
    				JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// Check that everything is set
		if (selOpenBISURL.equals("") || selAcqStation.equals("") ||
				selAcceptSelfSignedCerts.equals("") ||
				selUserDataDir.equals("") || 
				selIncomingDir.equals("")) {
			JOptionPane.showMessageDialog(null,
    				"Please set all fields!", "Error",
    				JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		// Check that the two paths do not point to the same folder
		if (selUserDataDir.equalsIgnoreCase(selIncomingDir)) {
    		JOptionPane.showMessageDialog(null,
    				"User and working directory must point to different locations!",
    				"Error", JOptionPane.ERROR_MESSAGE);
    		return false;
		}
		
		// Save the properties to file
		boolean success = AppProperties.writePropertiesToFile(
				selOpenBISURL, selAcceptSelfSignedCerts, selAcqStation, 
				selUserDataDir, selIncomingDir);
		if (! success) {
    		JOptionPane.showMessageDialog(null,
    				AppProperties.getLastErrorMessage(),
    				"Error", JOptionPane.ERROR_MESSAGE);
		}
		
		return success;
	}

}
