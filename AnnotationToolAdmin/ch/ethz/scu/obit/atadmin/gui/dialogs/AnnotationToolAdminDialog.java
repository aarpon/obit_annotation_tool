package ch.ethz.scu.obit.atadmin.gui.dialogs;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ch.ethz.scu.obit.common.settings.AppSettings;
import ch.ethz.scu.obit.common.settings.AppSettingsManager;
import ch.ethz.scu.obit.common.utils.QueryOS;
import ch.ethz.scu.obit.common.version.VersionInfo;

/**
 * AnnotationTool administrator Dialog
 *
 * @author Aaron Ponti
 */
public class AnnotationToolAdminDialog extends JDialog {

	/* Private instance variables */
	private static final long serialVersionUID = 1L;

	protected String arrow; // String decorator

	protected JLabel urlLabel;
	protected JLabel acqStationDescription;
	protected JButton editOpenBISURLButton;
	protected JButton addConfigurationButton;
	protected JButton remConfigurationButton;
	protected JButton lowerConfigurationButton;
	protected JButton higherConfigurationButton;
	protected JButton dirButton;
	protected JButton userdirButton;
	protected JButton saveButton;
	protected JButton closeButton;
	protected JTextArea machineNameText;
	protected JTextArea openBISURLText;
	protected JComboBox<String> acqStationsList;
	protected JComboBox<String> configurationNameList;
	protected JComboBox<String> acceptSelfSignedCertsList;
	protected JComboBox<String> createMarkerFileList;

	AppSettingsManager manager;

	/**
	 * Constructor
	 */
	public AnnotationToolAdminDialog() {

		// Define a text decorator
		arrow = Character.toString('\u25CF') + " ";

		// Use the system default look-and-feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't set look and feel.");
		}

		// Icon
		ImageIcon appIcon = new ImageIcon(this.getClass().getResource("icons/icon_admin.png"));

		// Set it to the window
		setIconImage(appIcon.getImage());

		// Set the dialog title
		setTitle("openBIS Importer Toolset (oBIT) :: Annotation Tool Admin v" + VersionInfo.version + " "
				+ VersionInfo.status);

		// Read the properties
		manager = new AppSettingsManager();

		// Make the dialog modal and not resizable
		setModal(true);
		setResizable(false);

		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);

		// Create constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.BOTH;

		// Row index
		int rowIndex = 0;

		// Add a label for the copyright info
		JLabel labelInfo = new JLabel(VersionInfo.copyright);
		Font f = labelInfo.getFont();
		labelInfo.setFont(new Font(f.getFontName(), f.getStyle(), 9));
		constraints.gridx = 0;
		constraints.gridy = rowIndex;
		constraints.insets = new Insets(3, 3, 5, 0);
		add(labelInfo, constraints);

		// Add a label for the selection of the openBIS URL
		urlLabel = new JLabel(arrow + "Set the openBIS URL");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(urlLabel, constraints);

		// Add a drop-down menu for the selection of the URL
		ArrayList<String> allConfigurationNames = manager.getAllConfigurationNames();
		String configurationName = manager.getActiveConfigurationName();
		int index = -1;
		for (int i = 0; i < allConfigurationNames.size(); i++) {
			if (allConfigurationNames.get(i).equals(configurationName)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			String msg = "Unknown configuration name! Defaulting to "
					+ manager.defaultValueForSetting("ConfigurationName") + ".";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			index = 0;
		}
		configurationNameList = new JComboBox<String>();
		for (String currConfigurationName : allConfigurationNames) {
			configurationNameList.addItem(currConfigurationName);
		}
		configurationNameList.setSelectedIndex(index);
		configurationNameList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("comboBoxChanged")) {
					int index = configurationNameList.getSelectedIndex();
					changeCurrentSettingIndex(index);
				}
			}
		});
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 15;
		constraints.gridheight = 1;
		constraints.weightx = 0.9;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 0);
		add(configurationNameList, constraints);

		// Create the "Edit configuration" button ("...")
		editOpenBISURLButton = new JButton("...");
		editOpenBISURLButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Ask the user to specify a new openBIS URL
				String newConfName = JOptionPane.showInputDialog("Configuration name:",
						manager.getActiveConfigurationName());
				if (newConfName == null || newConfName.equals("")) {
					return;
				}

				// Update the configuration name
				AppSettings conf = manager.getActiveConfiguration();
				conf.setConfigurationName(newConfName);

				// Update active configuration
				manager.setActiveConfiguration(newConfName);

				// Update UI
				refillConfigurationNameList();
				updateUI();

			}
		});
		constraints.gridx = 15;
		constraints.gridy = rowIndex;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 0.05;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 0);
		add(editOpenBISURLButton, constraints);

		// Create the "Add configuration" button ("+")
		addConfigurationButton = new JButton(Character.toString('\u002B'));
		addConfigurationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Ask the user to specify a new configuration name
				String confName = JOptionPane.showInputDialog("Please enter new configuration name:");
				if (confName == null || confName.equals("")) {
					return;
				}

				// Check that a configuration with this name is not already present
				if (manager.doesSettingExist("ConfigurationName", confName)) {
					JOptionPane.showMessageDialog(null, "Sorry, a configuration with this name already exists!",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Add a new server to the list of settings
				manager.add(confName);

				// Update UI
				refillConfigurationNameList();
				updateUI();

			}
		});
		constraints.gridx = 16;
		constraints.gridy = rowIndex;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 0.05;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 0);
		add(addConfigurationButton, constraints);

		// Add the "Remove configuration" button ("-")
		remConfigurationButton = new JButton(Character.toString('\u2212'));
		remConfigurationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// Ask the user for confirmation
				if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this configuration?",
						"Question", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}

				// Remove
				int index = configurationNameList.getSelectedIndex();
				try {
					manager.remove(index);
				} catch (ArrayIndexOutOfBoundsException a) {
					return;
				}

				// Update UI
				refillConfigurationNameList();
				updateUI();

			}
		});
		constraints.gridx = 17;
		constraints.gridy = rowIndex;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 0.05;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 0);
		add(remConfigurationButton, constraints);

		// Add the "^" button
		higherConfigurationButton = new JButton(Character.toString('\u25B2'));
		higherConfigurationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = configurationNameList.getSelectedIndex();
				try {
					manager.moveUp(index);
				} catch (ArrayIndexOutOfBoundsException a) {
					return;
				}

				// Update UI
				refillConfigurationNameList();
				updateUI();
			}
		});
		constraints.gridx = 18;
		constraints.gridy = rowIndex;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 0.05;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 0);
		add(higherConfigurationButton, constraints);

		// Add the "v" button
		lowerConfigurationButton = new JButton(Character.toString('\u25BC'));
		lowerConfigurationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = configurationNameList.getSelectedIndex();
				try {
					manager.moveDown(index);
				} catch (ArrayIndexOutOfBoundsException a) {
					return;
				}

				// Update UI
				refillConfigurationNameList();
				updateUI();
			}
		});
		constraints.gridx = 19;
		constraints.gridy = rowIndex;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 0.05;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(lowerConfigurationButton, constraints);

		// Add a label for the openBIS server URL
		JLabel confOpenBISURLLabel = new JLabel(arrow + "openBIS server URL");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(confOpenBISURLLabel, constraints);

		// Add a text field for optional configuration name
		openBISURLText = new JTextArea("");
		openBISURLText.setLineWrap(false);
		openBISURLText.getDocument().putProperty("filterNewlines", Boolean.TRUE);
		openBISURLText.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				manager.setSettingValue("OpenBISURL", openBISURLText.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				manager.setSettingValue("OpenBISURL", openBISURLText.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				manager.setSettingValue("OpenBISURL", openBISURLText.getText());
			}
		});

		openBISURLText.setText(manager.getSettingValue("OpenBISURL"));
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(openBISURLText, constraints);

		// Add a label for the options of accepting self-signed
		// certificates
		JLabel certLabel = new JLabel(arrow + "Accept self-signed SSL certificates when logging in to openBIS");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(certLabel, constraints);

		// Add a drop-down menu for the options of accepting self-signed
		// certificates
		String acceptSelfSignedCerts = manager.getSettingValue("AcceptSelfSignedCertificates");
		ArrayList<String> acceptSelfSignedCertsOptions = manager
				.possibleValuesForSetting("AcceptSelfSignedCertificates");
		index = -1;
		for (int i = 0; i < acceptSelfSignedCertsOptions.size(); i++) {
			if (acceptSelfSignedCertsOptions.get(i).equals(acceptSelfSignedCerts)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			String msg = "Unknown option for accepting self-signed certificates! Defaulting to \""
					+ manager.defaultValueForSetting("AcceptSelfSignedCertificates") + "\".";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			index = 0;
		}
		acceptSelfSignedCertsList = new JComboBox<String>();
		for (String currAcceptCertsOption : acceptSelfSignedCertsOptions) {
			acceptSelfSignedCertsList.addItem(currAcceptCertsOption);
		}
		acceptSelfSignedCertsList.setSelectedIndex(index);
		acceptSelfSignedCertsList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("comboBoxChanged")) {
					String value = (String) acceptSelfSignedCertsList.getSelectedItem();
					if (manager.getSettingValue("AcceptSelfSignedCertificates").equals(value)) {
						// No change, just return
						return;
					}
					if (manager.settingMustBeUnique("AcceptSelfSignedCertificates")) {
						if (manager.doesSettingExist("AcceptSelfSignedCertificates", value)) {
							JOptionPane.showMessageDialog(null, "You cannot duplicate this setting value!", "Error",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					// We can store it
					manager.setSettingValue("AcceptSelfSignedCertificates", value);
				}
			}
		});
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		add(acceptSelfSignedCertsList, constraints);

		// Add a label for the selection of the acquisition machine
		JLabel acqLabel = new JLabel(arrow + "Select the acquisition station or type");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(acqLabel, constraints);

		// Add a drop-down menu for the selection of the acquisition machine
		String acqStation = manager.getSettingValue("AcquisitionStation");
		ArrayList<String> acqStations = manager.possibleValuesForSetting("AcquisitionStation");
		index = -1;
		for (int i = 0; i < acqStations.size(); i++) {
			if (acqStations.get(i).equals(acqStation)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			String msg = "Unknown acquisition station! Defaulting to "
					+ manager.defaultValueForSetting("AcquisitionStation") + ".";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			System.err.println("Unknown acquisition station! Defaulting to "
					+ manager.defaultValueForSetting("AcquisitionStation") + ".");
			index = 0;
			// Restore default
			manager.setSettingValue("AcquisitionStation", manager.defaultValueForSetting("AcquisitionStation"));
		}
		acqStationsList = new JComboBox<String>();
		for (String currAcqStation : acqStations) {
			acqStationsList.addItem(currAcqStation);
		}
		acqStationsList.setSelectedIndex(index);
		acqStationsList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("comboBoxChanged")) {
					String value = (String) acqStationsList.getSelectedItem();
					if (manager.getSettingValue("AcquisitionStation").equals(value)) {
						// No change, just return
						return;
					}
					if (manager.settingMustBeUnique("AcquisitionStation")) {
						// This is here for completeness. Currently, the
						// acquisition station MUST not need to be univocal
						// among servers!
						if (manager.doesSettingExist("AcquisitionStation", value)) {
							JOptionPane.showMessageDialog(null,
									"This acquisition station has already been assigned to another server!", "Error",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					// We can store it
					manager.setSettingValue("AcquisitionStation", value);

					// Update the description as well
					acqStationDescription
							.setText(manager.getAcqStationDescription(manager.getSettingValue("AcquisitionStation")));
				}
			}
		});
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(acqStationsList, constraints);

		acqStationDescription = new JLabel(
				manager.getAcqStationDescription(manager.getSettingValue("AcquisitionStation")));
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(acqStationDescription, constraints);

		// Add a label for the human-friendly machine name
		JLabel machineNameLabel = new JLabel(arrow + "User-friendly machine name");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(machineNameLabel, constraints);

		// Add a text field for the human-friendly machine name
		machineNameText = new JTextArea("");
		machineNameText.setLineWrap(false);
		machineNameText.getDocument().putProperty("filterNewlines", Boolean.TRUE);
		machineNameText.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				manager.setSettingValue("HumanFriendlyHostName", machineNameText.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				manager.setSettingValue("HumanFriendlyHostName", machineNameText.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				manager.setSettingValue("HumanFriendlyHostName", machineNameText.getText());
			}
		});

		machineNameText.setText(manager.getSettingValue("HumanFriendlyHostName"));
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(machineNameText, constraints);

		// Add a label for the info text
		JLabel infoMachineNameLabel;
		String hostnameStr = "";
		try {
			hostnameStr = "For reference, the machine host name is " + QueryOS.getHostName() + ".";
		} catch (UnknownHostException e1) {
			hostnameStr = "";
		}
		infoMachineNameLabel = new JLabel(hostnameStr);
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(infoMachineNameLabel, constraints);

		// Add a label for the user directory
		JLabel userdirLabel = new JLabel(arrow + "Set user data directory");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
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
			@Override
			public void actionPerformed(ActionEvent e) {
				String dir = pickDir("Set the user data directory");
				String selUserDataDir = "";
				if (!dir.equals("")) {
					try {
						selUserDataDir = (new File(dir)).getCanonicalPath();
					} catch (IOException exc) {
						// This really should not happen
						selUserDataDir = "";
						JOptionPane.showMessageDialog(null, "Invalid directory chosen!", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					if (!(new File(selUserDataDir)).exists()) {
						JOptionPane.showMessageDialog(null, "The selected directory does not exist!", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (manager.getSettingValue("UserDataDir").equals(selUserDataDir)) {
						// No change, just return
						return;
					}
					if (manager.settingMustBeUnique("UserDataDir")) {
						if (manager.doesSettingExist("UserDataDir", selUserDataDir)) {
							JOptionPane.showMessageDialog(null,
									"This user directory has already been assigned to another server!", "Error",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					userdirButton.setText(selUserDataDir);
					pack();
					manager.setSettingValue("UserDataDir", selUserDataDir);
				}
			}
		});
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(userdirButton, constraints);

		// Add a label for the directory
		JLabel dirLabel = new JLabel(arrow + "Set Datamover incoming directory");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
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
			@Override
			public void actionPerformed(ActionEvent e) {
				String dir = pickDir("Set the Datamover incoming directory");
				String selIncomingDir = "";
				if (!dir.equals("")) {
					try {
						selIncomingDir = (new File(dir)).getCanonicalPath();
					} catch (IOException exc) {
						// This really should not happen
						selIncomingDir = "";
						JOptionPane.showMessageDialog(null, "Invalid directory chosen!", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					if (!(new File(selIncomingDir)).exists()) {
						JOptionPane.showMessageDialog(null, "The selected directory does not exist!", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (manager.getSettingValue("DatamoverIncomingDir").equals(selIncomingDir)) {
						// No change, just return
						return;
					}
					if (manager.settingMustBeUnique("DatamoverIncomingDir")) {
						if (manager.doesSettingExist("DatamoverIncomingDir", selIncomingDir)) {
							JOptionPane.showMessageDialog(null,
									"This Datamover incoming directory has "
											+ "already been assigned to another server!",
									"Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					dirButton.setText(selIncomingDir);
					pack();
					manager.setSettingValue("DatamoverIncomingDir", selIncomingDir);
				}
			}
		});
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(dirButton, constraints);

		// Add a label for the info text
		JLabel infoLabel = new JLabel(
				"<html>It is <b>highly recommended</b> to set both folders on the same file system.</html>");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(infoLabel, constraints);

		// Add a label for the options of creating the marker file to trigger
		// registration
		JLabel createMarkerFileLabel = new JLabel(
				arrow + "(Advanced) Create marker file to trigger registration from the incoming folder");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(createMarkerFileLabel, constraints);

		// Add a drop-down menu for the options of accepting self-signed
		// certificates
		String createMarkerFile = manager.getSettingValue("CreateMarkerFile");
		ArrayList<String> createMarkerFileOptions = manager.possibleValuesForSetting("CreateMarkerFile");
		index = -1;
		for (int i = 0; i < createMarkerFileOptions.size(); i++) {
			if (createMarkerFileOptions.get(i).equals(createMarkerFile)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			String msg = "Unknown option for creating marker file! Defaulting to \""
					+ manager.defaultValueForSetting("CreateMarkerFile") + "\".";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			index = 0;
		}
		createMarkerFileList = new JComboBox<String>();
		for (String currCreateMarkerFileOption : createMarkerFileOptions) {
			createMarkerFileList.addItem(currCreateMarkerFileOption);
		}
		createMarkerFileList.setSelectedIndex(index);
		createMarkerFileList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("comboBoxChanged")) {
					String value = (String) createMarkerFileList.getSelectedItem();
					if (manager.getSettingValue("CreateMarkerFile").equals(value)) {
						// No change, just return
						return;
					}
					if (manager.settingMustBeUnique("CreateMarkerFile")) {
						if (manager.doesSettingExist("CreateMarkerFile", value)) {
							JOptionPane.showMessageDialog(null, "You cannot duplicate this setting value!", "Error",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					// We can store it
					manager.setSettingValue("CreateMarkerFile", value);
				}
			}
		});
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		add(createMarkerFileList, constraints);

		// Add a label for the info text
		JLabel infoMarkerLabel = new JLabel(
				"This will work only if the Datamover incoming folder is also the dropbox folder.");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 20;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(infoMarkerLabel, constraints);

		// Some spacer
		JLabel spacerLabel = new JLabel("");
		constraints.gridx = 0;
		constraints.gridy = ++rowIndex;
		constraints.gridwidth = 4;
		constraints.gridheight = 1;
		constraints.weightx = 0.5;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(spacerLabel, constraints);

		// Create a Save button
		saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// Are all the Settings set?
				if (!manager.allSet()) {
					JOptionPane.showMessageDialog(null, "Incomplete configuration!", "Error",
							JOptionPane.ERROR_MESSAGE);

					return;
				}

				// Are all the Settings valid?
				if (!manager.validate()) {
					JOptionPane.showMessageDialog(null, manager.getLastErrorMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);

					return;
				}

				// Save
				if (!manager.save()) {
					JOptionPane.showMessageDialog(null, "Error saving settings: " + manager.getLastErrorMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);

					return;
				} else {

					saveButton.setText("<html><b>Saved!</b></html>");
					ActionListener ls = new ActionListener() {
						@Override
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
		constraints.gridy = rowIndex;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.weightx = 0.1;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(saveButton, constraints);

		// Create a close button
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
				System.exit(0);
			}
		});
		constraints.gridx = 18;
		constraints.gridy = rowIndex;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.weightx = 0.1;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(closeButton, constraints);

		// Make the login button react to the enter key
		getRootPane().setDefaultButton(saveButton);

		// Enable/disable buttons
		toggleDynamicWidgets();

		// Work around an issue with dark GTK themes in Linux
		if (QueryOS.isLinux() && UIManager.getSystemLookAndFeelClassName().contains("GTK")) {
			SwingUtilities.updateComponentTreeUI(this);
		}

		// Display the dialog
		setMinimumSize(new Dimension(700, 220));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	/**
	 * Asks the user to pick a directory
	 *
	 * @param dialogTitle Title for the directory chooser dialog.
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
	 * Changes the currently active AppSettings
	 *
	 * @param index of the AppSettings in the listAppSettings array.
	 */
	private void changeCurrentSettingIndex(int index) {

		if (index == -1) {
			// This happens when the server list is cleared
			return;
		}

		// Check that we really need to change
		if (index == manager.getCurrentIndex()) {
			// Nothing to do
			return;
		}

		// Try changing the index
		try {
			manager.setCurrentIndex(index);
		} catch (ArrayIndexOutOfBoundsException e) {
			JOptionPane.showMessageDialog(null,
					"There was a mistake setting the index of current "
							+ "server.\n\nThis is a bug, please inform your administrator!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Now update the UI with the new settings
		updateUI();

	}

	/**
	 * Refills the list of configurations when their number/content has changed
	 * after a user action.
	 */
	private void refillConfigurationNameList() {

		// Disable action listeners while we refill the list
		ActionListener[] actionListeners = configurationNameList.getActionListeners();
		for (ActionListener a : actionListeners) {
			configurationNameList.removeActionListener(a);
		}
		ArrayList<String> configurationNameOptions = manager.getAllConfigurationNames();
		configurationNameList.removeAllItems();
		for (String currConfName : configurationNameOptions) {
			configurationNameList.addItem(currConfName);
		}
		configurationNameList.setSelectedItem(manager.getActiveConfigurationName());
		// Add the action listeners back
		for (ActionListener a : actionListeners) {
			configurationNameList.addActionListener(a);
		}
	}

	/**
	 * Updates all fields with the values from current AppSettings
	 */
	private void updateUI() {
		configurationNameList.setSelectedItem(manager.getActiveConfigurationName());
		openBISURLText.setText(manager.getSettingValue("OpenBISURL"));
		acqStationsList.setSelectedItem(manager.getSettingValue("AcquisitionStation"));
		acqStationDescription.setText(manager.getAcqStationDescription(manager.getSettingValue("AcquisitionStation")));
		machineNameText.setText(manager.getSettingValue("HumanFriendlyHostName"));
		acceptSelfSignedCertsList.setSelectedItem(manager.getSettingValue("AcceptSelfSignedCertificates"));
		userdirButton.setText(manager.getSettingValue("UserDataDir"));
		dirButton.setText(manager.getSettingValue("DatamoverIncomingDir"));
		createMarkerFileList.setSelectedItem(manager.getSettingValue("CreateMarkerFile"));

		// Enable/disable buttons
		toggleDynamicWidgets();

	}

	/**
	 * Enable/disable buttons depending on current selections
	 */
	private void toggleDynamicWidgets() {
		boolean state = true;
		if (manager.getAllConfigurationNames().size() < 2) {
			state = false;
		}
		remConfigurationButton.setEnabled(state);
		lowerConfigurationButton.setEnabled(state);
		higherConfigurationButton.setEnabled(state);

		state = true;
		if (manager.getCurrentIndex() == 0) {
			state = false;
		}
		higherConfigurationButton.setEnabled(state);

		state = true;
		if (manager.getCurrentIndex() == (manager.getAllConfigurationNames().size() - 1)) {
			state = false;
		}
		lowerConfigurationButton.setEnabled(state);

		if (configurationNameList.getSelectedIndex() != 0) {
			urlLabel.setText(
					arrow + "Set the configuration name (current default: " + configurationNameList.getItemAt(0) + ")");
		} else {
			urlLabel.setText(arrow + "Set the configuration name (this is current default)");
		}
	}

}
