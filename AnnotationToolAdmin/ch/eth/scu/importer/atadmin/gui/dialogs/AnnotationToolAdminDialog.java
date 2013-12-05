package ch.eth.scu.importer.atadmin.gui.dialogs;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.common.properties.DefaultProperties;

import java.io.File;
import java.io.IOException;
import java.awt.Dimension;
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

	protected JButton dirButton;
	protected JButton userdirButton;
	protected JButton saveButton;
	protected JButton closeButton;
	protected JComboBox<Object> acqStationsList;
	protected JComboBox<Object> openBISURLList;
	protected JComboBox<Object> acceptSelfSignedCertsList;
	
	private static final String version = "0.4.0";
	
	/**
	 * Constructor
	 */
	public AnnotationToolAdminDialog() {

		// Set the dialog title
		setTitle("openBIS Importer Toolset :: Annotation Tool Admin v" + version);

		// Read the properties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		boolean isUpToDate = 
				AppProperties.isPropertiesFileVersionCurrent(appProperties);
		if (appProperties == null || !isUpToDate) {
			appProperties = AppProperties.initializePropertiesFile();
			if (	appProperties == null) {
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
		setLayout(new MigLayout("insets 10"));

		// Add a label for the selection of the openBIS URL
		JLabel urlLabel = new JLabel("Set the openBIS URL");
		add(urlLabel, "wrap, width 100%");

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
			String msg = "Unknown openBIS URL! Defaulting to " +
					DefaultProperties.defaultValueForProperty("OpenBISURL") +
					".";
			JOptionPane.showMessageDialog(null, msg, "Error",
				    JOptionPane.ERROR_MESSAGE);
			index = 0;
		}
		openBISURLList = new JComboBox<Object>(openBISURLOptions.toArray());
		openBISURLList.setSelectedIndex(index);
		selOpenBISURL = openBISURLOptions.get(index);
		openBISURLList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		if (e.getActionCommand().equals("comboBoxChanged")) {
        			selOpenBISURL = (String) openBISURLList.getSelectedItem();
        		}
            }
        });
		add(openBISURLList, "wrap, width 100%");

		// Add a label for the options of accepting self-signed
		// certificates
		JLabel certLabel = new JLabel("Accept self-signed SSL certificates "
				+ "when logging in to openBIS");
		add(certLabel, "wrap, width 100%");
		
		// Add a drop-down menu for the options of accepting self-signed
		// certificates
		String acceptSelfSignedCerts =
				appProperties.getProperty("AcceptSelfSignedCertificates");
		ArrayList<String> acceptSelfSignedCertsOptions = 
				DefaultProperties.possibleValuesForProperty(
						"AcceptSelfSignedCertificates");
		index = -1;
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
		add(acceptSelfSignedCertsList, "wrap, width 100%");

		// Add a label for the selection of the acquisition machine
		JLabel acqLabel = new JLabel("Select the acquisition station");
		add(acqLabel, "wrap, width 100%");

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
		add(acqStationsList, "wrap, width 100%");

		// Add a label for the user directory
		JLabel userdirLabel = new JLabel("Set user data directory");
		add(userdirLabel, "wrap, width 100%");
		
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
            	}
            }
        });		
		add(userdirButton, "wrap, width 100%");

		// Add a label for the directory
		JLabel dirLabel = new JLabel("Set Datamover incoming directory");
		add(dirLabel, "wrap, width 100%");
		
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
            	}
            }
        });		
		add(dirButton, "wrap, width 100%");
		
		// Add a label for the info text
		JLabel infoLabel = new JLabel(
				"<html>It is <b>highly recommended</b> to set " +
		"both folders on the same file system.</html>");
		add(infoLabel, "wrap, width 100%");
		
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

            	// Save the selection to the properties file
            	if (!saveProperties()) {
            		JOptionPane.showMessageDialog(null,
                            "Could not save settings! " +
                                    "Make sure you have administrator rights!", "Error",
            				JOptionPane.ERROR_MESSAGE);
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
		add(saveButton, "tag ok, span, split 2");

		// Create a close button
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	setVisible(false);
            	dispose();
            	System.exit(0);
            }
        });
		add(closeButton, "tag cancel, span, split 2");
		
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
		
		// Check that everything is set
		if (selOpenBISURL.equals("") || selAcqStation.equals("") ||
				selAcceptSelfSignedCerts.equals("") ||
				selUserDataDir.equals("") || 
				selIncomingDir.equals("")) {
			return false;
		}
		
		// Save the properties to file
		return AppProperties.writePropertiesToFile(selOpenBISURL,
				selAcceptSelfSignedCerts, selAcqStation, 
				selUserDataDir, selIncomingDir);
	}
	
}
