package ch.eth.scu.importer.atadmin.gui.dialogs;

import javax.swing.*;

import ch.eth.scu.importer.common.settings.AppSettingsManager;
import ch.eth.scu.importer.common.version.VersionInfo;

import java.io.File;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * AnnotationTool administrator Dialog
 * @author Aaron Ponti
 */
public class AnnotationToolAdminDialog extends JDialog {
	
	/* Private instance variables */
	private static final long serialVersionUID = 1L;

    protected JButton addOpenBISURLButton;
    protected JButton remOpenBISURLButton;
    protected JButton lowerOpenBISURLButton;
    protected JButton higherOpenBISURLButton;   	
	protected JTextField openBISURLInput;
	protected JButton dirButton;
	protected JButton userdirButton;
	protected JButton saveButton;
	protected JButton closeButton;
	protected JComboBox<String> acqStationsList;
    protected JComboBox<String> openBISURLList;	
	protected JComboBox<String> acceptSelfSignedCertsList;

	AppSettingsManager manager;

	/**
	 * Constructor
	 */
	public AnnotationToolAdminDialog() {

		// Set the dialog title
		setTitle("openBIS Importer Toolset :: Annotation Tool Admin v" +
		VersionInfo.version + " " + VersionInfo.status);

		// Read the properties
		manager = new AppSettingsManager();
		
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
		JLabel urlLabel = new JLabel("Set the openBIS URL " +
		"(the first will be default for the users)");
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(urlLabel, constraints);

        // Add a drop-down menu for the selection of the URL
        String openBISURL = manager.getServer();
        ArrayList<String> openBISURLOptions = 
                manager.possibleValuesForSetting("OpenBISURL");
        int index = -1;
        for (int i = 0; i < openBISURLOptions.size(); i++) {
            if (openBISURLOptions.get(i).equals(openBISURL)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            String msg = "Unknown openBIS URL! Defaulting to " +
                    manager.defaultValueForSetting("OpenBISURL") +
                    ".";
            JOptionPane.showMessageDialog(null, msg, "Error",
                    JOptionPane.ERROR_MESSAGE);
            index = 0;
        }
        openBISURLList = new JComboBox<String>();
        for (String currOpenBISURL : openBISURLOptions) {
            openBISURLList.addItem(currOpenBISURL);
        }
        openBISURLList.setSelectedIndex(index);
        openBISURLList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxChanged")) {
                	String selURL = (String) openBISURLList.getSelectedItem();
                	manager.setServer(selURL);
                }
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 16;
        constraints.gridheight = 1;     
        constraints.weightx = 0.9;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 0);
        add(openBISURLList, constraints);

        // Create the "Add openBIS URL" button ("+")
        addOpenBISURLButton = new JButton(Character.toString('\u002B'));
        addOpenBISURLButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Ask the user to specify a new openBIS URL
                String url = JOptionPane.showInputDialog(
                        "Please enter full openBIS URL:");
                if (url == null || url.equals("")) {
                    return;
                }
                // TODO Validate URL
                
                // Add and select it
                openBISURLList.addItem(url);
                openBISURLList.setSelectedIndex(openBISURLList.getItemCount() - 1);
                
                // Validate URL list
                validateOpenBISURLList();
            }
        });
        constraints.gridx = 16;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;     
        constraints.weightx = 0.05;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 0);
        add(addOpenBISURLButton, constraints);

        // Add the "Remove openBIS URL" button ("-")
        remOpenBISURLButton = new JButton(Character.toString('\u2212'));
        remOpenBISURLButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (openBISURLList.getItemCount() == 0) {
                    return;
                }
                int indx = openBISURLList.getSelectedIndex();
                if (indx != -1) {
                    openBISURLList.removeItemAt(indx);
                }
                
                // Make sure to clean up
                validateOpenBISURLList();
            }
        });
        constraints.gridx = 17;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;     
        constraints.weightx = 0.05;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 0);
        add(remOpenBISURLButton, constraints);

        // Add the "^" button
        higherOpenBISURLButton = new JButton(Character.toString('\u25B2'));
        higherOpenBISURLButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Implement
            }
        });
        constraints.gridx = 18;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;     
        constraints.weightx = 0.05;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 0);
        add(higherOpenBISURLButton, constraints);

        // Add the "v" button
        lowerOpenBISURLButton = new JButton(Character.toString('\u25BC'));
        lowerOpenBISURLButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Implement
            }
        });
        constraints.gridx = 19;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;     
        constraints.weightx = 0.05;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(lowerOpenBISURLButton, constraints);

	
		// Add a label for the options of accepting self-signed
		// certificates
		JLabel certLabel = new JLabel("Accept self-signed SSL certificates "
				+ "when logging in to openBIS");
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(certLabel, constraints);
		
		// Add a drop-down menu for the options of accepting self-signed
		// certificates
		String acceptSelfSignedCerts = manager.getSettingValue(
				"AcceptSelfSignedCertificates");
		ArrayList<String> acceptSelfSignedCertsOptions = 
				manager.possibleValuesForSetting(
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
					manager.defaultValueForSetting(
							"AcceptSelfSignedCertificates") +
					"\".";
			JOptionPane.showMessageDialog(null, msg, "Error",
				    JOptionPane.ERROR_MESSAGE);
			index = 0;
		}
		acceptSelfSignedCertsList = new JComboBox<String>();
        for (String currAcceptCertsOption : acceptSelfSignedCertsOptions) {
        	acceptSelfSignedCertsList.addItem(currAcceptCertsOption);
        }
		acceptSelfSignedCertsList.setSelectedIndex(index);
		acceptSelfSignedCertsList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		if (e.getActionCommand().equals("comboBoxChanged")) {
        			manager.setSettingValue("AcceptSelfSignedCertificates",
        					(String) acceptSelfSignedCertsList.getSelectedItem());
        		}
            }
        });
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		add(acceptSelfSignedCertsList, constraints);

		// Add a label for the selection of the acquisition machine
		JLabel acqLabel = new JLabel("Select the acquisition station or type");
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(acqLabel, constraints);

		// Add a drop-down menu for the selection of the acquisition machine
		String acqStation = manager.getSettingValue("AcquisitionStation");
		ArrayList<String> acqStations = 
				manager.possibleValuesForSetting("AcquisitionStation");
		index = -1;
		for (int i = 0; i < acqStations.size(); i++) {
			if (acqStations.get(i).equals(acqStation)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			String msg = "Unknown acquisition station! Defaulting to " +
					manager.defaultValueForSetting("AcquisitionStation") +
					".";
			JOptionPane.showMessageDialog(null, msg, "Error",
				    JOptionPane.ERROR_MESSAGE);			
			System.err.println("Unknown acquisition station! Defaulting to " +
					manager.defaultValueForSetting("AcquisitionStation") +
					".");
			index = 0;
		}
		acqStationsList = new JComboBox<String>();
        for (String currAcqStation : acqStations) {
        	acqStationsList.addItem(currAcqStation);
        }
		acqStationsList.setSelectedIndex(index);
		acqStationsList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		if (e.getActionCommand().equals("comboBoxChanged")) {
        			manager.setSettingValue("AcquisitionStation", 
        					(String) acqStationsList.getSelectedItem());
        		}
            }
        });
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(acqStationsList, constraints);

		// Add a label for the user directory
		JLabel userdirLabel = new JLabel("Set user data directory");
		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(userdirLabel, constraints);
		
		// Add a pushButton to choose the user directory
		// Create a text field for the user name
		userdirButton = new JButton(manager.getSettingValue("UserDataDir"));
		userdirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String dir = pickDir("Set the user data directory");
            	String selUserDataDir = "";
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
            	manager.setSettingValue("UserDataDir", selUserDataDir);
            }
        });		
		constraints.gridx = 0;
		constraints.gridy = 7;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(userdirButton, constraints);

		// Add a label for the directory
		JLabel dirLabel = new JLabel("Set Datamover incoming directory");
		constraints.gridx = 0;
		constraints.gridy = 8;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(dirLabel, constraints);
		
		// Add a pushButton to choose the directory
		// Create a text field for the user name
		dirButton = new JButton(manager.getSettingValue("DatamoverIncomingDir"));
		dirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String dir = pickDir("Set the datamover incoming directory");
            	String selIncomingDir = "";
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
            	manager.setSettingValue("DatamoverIncomingDir", selIncomingDir);
            }
        });		
		constraints.gridx = 0;
		constraints.gridy = 9;
		constraints.gridwidth = 20;
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

            	// Are all the Settings set?
            	if (!manager.allSet()) {
            		JOptionPane.showMessageDialog(null,
            				"Incomplete configuration!",
            				"Error", JOptionPane.ERROR_MESSAGE);

            		return;            		
            	}

            	// Save
            	if (!manager.save()) {
            		JOptionPane.showMessageDialog(null,
            				"Could not save settings: " +
            						manager.getLastErrorMessage(),
            				"Error", JOptionPane.ERROR_MESSAGE);

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
		constraints.gridx = 16;
		constraints.gridy = 11;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;		
		constraints.weightx = 0.1;
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
		constraints.gridx = 18;
		constraints.gridy = 11;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;			
		constraints.weightx = 0.1;
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
     * Validate the list of openBISURLs. In particular, removes empty strings.
     */
    private void validateOpenBISURLList() {
        
        // Store the selected item
        String current = (String)openBISURLList.getSelectedItem();
        for (int i = openBISURLList.getItemCount() - 1; i >= 0; i --) {
            String sel = openBISURLList.getItemAt(i);
            if (sel.equals("")) {
                // Remove
                openBISURLList.removeItemAt(i);
            }
        }
        
        // Are there still URLs?
        if (openBISURLList.getItemCount() == 0) {
            openBISURLList.addItem("");
            openBISURLList.setSelectedIndex(0);
            return;
        }
        
        // Reselect the object if valid, otherwise point to first entry
        if (! current.equals("")) {
            openBISURLList.setSelectedItem(current);
        } else {
            openBISURLList.setSelectedIndex(0);
        }
    }

    /**
     * Changes the currently active AppSettings
     * @param index of the AppSettings in the listAppSettings array.
     */
    private void changeCurrentSettingIndex(int index) {
    	
    	// Try changing the index 
    	try {
    		manager.setCurrent(index);
    	} catch (ArrayIndexOutOfBoundsException e) {
    		JOptionPane.showMessageDialog(null,
    				"There was a mistake setting the index of current " +
    		"server.\n\nThis is a bug, please inform your administrator!",
    				"Error", JOptionPane.ERROR_MESSAGE);
    		return;        
    	}

		// Now update the UI with the new settings
    	updateUI();

    }
    
    /**
     * Changes the currently active AppSettings
     * @param index of the AppSettings in the listAppSettings array.
     */
    private void addNewSetting() {
    	
    	manager.add();
    	
    	// Now update the UI with the new settings
    	updateUI();
    	
    }

    /**
     * Changes the currently active AppSettings
     * @param index of the AppSettings in the listAppSettings array.
     */
    private void removeSetting(int index) {
    	
    	try {
    		manager.remove(index);
    	} catch (ArrayIndexOutOfBoundsException e) {
    		JOptionPane.showMessageDialog(null,
    				"There was a mistake removing current server.\n\n" + 
    		"This is a bug, please inform your administrator!",
    				"Error", JOptionPane.ERROR_MESSAGE);
    		return;   
    	}
    	
    	// Now update the UI with the new settings
    	updateUI();
    	
    }
    
    /**
     * Updates all fields with the values from current AppSettings
     */
    private void updateUI() {
    	openBISURLList.setSelectedItem(manager.getServer());
    	acqStationsList.setSelectedItem(manager.getSettingValue("AcquisitionStation"));
    	acceptSelfSignedCertsList.setSelectedItem(manager.getSettingValue("AcceptSelfSignedCertificates"));
    	userdirButton.setText(manager.getSettingValue("UserDataDir"));
    	dirButton.setText(manager.getSettingValue("DatamoverIncomingDir"));
    }
}
