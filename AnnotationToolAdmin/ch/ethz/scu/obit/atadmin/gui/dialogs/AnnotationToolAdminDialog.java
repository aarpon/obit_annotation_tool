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
import java.net.MalformedURLException;
import java.net.URL;
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
    protected JButton addOpenBISURLButton;
    protected JButton remOpenBISURLButton;
    protected JButton lowerOpenBISURLButton;
    protected JButton higherOpenBISURLButton;
    protected JButton dirButton;
    protected JButton userdirButton;
    protected JButton saveButton;
    protected JButton closeButton;
    protected JTextArea machineNameText;
    protected JTextArea configurationNameText;
    protected JComboBox<String> acqStationsList;
    protected JComboBox<String> openBISURLList;
    protected JComboBox<String> acceptSelfSignedCertsList;

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
        ArrayList<String> openBISURLOptions = manager.getAllServers();
        String openBISURL = manager.getActiveServer();
        int index = -1;
        for (int i = 0; i < openBISURLOptions.size(); i++) {
            if (openBISURLOptions.get(i).equals(openBISURL)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            String msg = "Unknown openBIS URL! Defaulting to " + manager.defaultValueForSetting("OpenBISURL") + ".";
            JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
            index = 0;
        }
        openBISURLList = new JComboBox<String>();
        for (String currOpenBISURL : openBISURLOptions) {
            openBISURLList.addItem(currOpenBISURL);
        }
        openBISURLList.setSelectedIndex(index);
        openBISURLList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxChanged")) {
                    int index = openBISURLList.getSelectedIndex();
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
        add(openBISURLList, constraints);

        // Create the "Edit openBIS URL" button ("...")
        editOpenBISURLButton = new JButton("...");
        editOpenBISURLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ask the user to specify a new openBIS URL
                String url = JOptionPane.showInputDialog("Edit openBIS URL:", manager.getActiveServer());
                if (url == null || url.equals("") || url.equalsIgnoreCase(manager.getActiveServer())) {
                    return;
                }
                // TODO Validate URL better
                try {
                    new URL(url);
                } catch (MalformedURLException u) {
                    JOptionPane.showMessageDialog(null, "Malformed URL! Please try again.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check that the URL is not already present
                if (manager.doesSettingExist("OpenBISURL", url)) {
                    JOptionPane.showMessageDialog(null, "Sorry, this server already exists!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Update URL
                manager.setCurrentServerURL(url);

                // Update UI
                refillServerList();
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

        // Create the "Add openBIS URL" button ("+")
        addOpenBISURLButton = new JButton(Character.toString('\u002B'));
        addOpenBISURLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ask the user to specify a new openBIS URL
                String url = JOptionPane.showInputDialog("Please enter full openBIS URL:");
                if (url == null || url.equals("")) {
                    return;
                }
                // TODO Validate URL better
                try {
                    new URL(url);
                } catch (MalformedURLException u) {
                    JOptionPane.showMessageDialog(null, "Malformed URL! Please try again.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check that the URL is not already present
                if (manager.doesSettingExist("OpenBISURL", url)) {
                    JOptionPane.showMessageDialog(null, "Sorry, this server already exists!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Add a new server to the list of settings
                manager.add(url);

                // Update UI
                refillServerList();
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
        add(addOpenBISURLButton, constraints);

        // Add the "Remove openBIS URL" button ("-")
        remOpenBISURLButton = new JButton(Character.toString('\u2212'));
        remOpenBISURLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Ask the user for confirmation
                if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this server?", "Question",
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }

                // Remove
                int index = openBISURLList.getSelectedIndex();
                try {
                    manager.remove(index);
                } catch (ArrayIndexOutOfBoundsException a) {
                    return;
                }

                // Update UI
                refillServerList();
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
        add(remOpenBISURLButton, constraints);

        // Add the "^" button
        higherOpenBISURLButton = new JButton(Character.toString('\u25B2'));
        higherOpenBISURLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = openBISURLList.getSelectedIndex();
                try {
                    manager.moveUp(index);
                } catch (ArrayIndexOutOfBoundsException a) {
                    return;
                }

                // Update UI
                refillServerList();
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
        add(higherOpenBISURLButton, constraints);

        // Add the "v" button
        lowerOpenBISURLButton = new JButton(Character.toString('\u25BC'));
        lowerOpenBISURLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = openBISURLList.getSelectedIndex();
                try {
                    manager.moveDown(index);
                } catch (ArrayIndexOutOfBoundsException a) {
                    return;
                }

                // Update UI
                refillServerList();
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
        add(lowerOpenBISURLButton, constraints);

        // Add a label for the optional configuration name
        JLabel confNameLabel = new JLabel(arrow + "User-friendly configuration name (optional)");
        constraints.gridx = 0;
        constraints.gridy = ++rowIndex;
        constraints.gridwidth = 20;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(confNameLabel, constraints);

        // Add a text field for optional configuration name
        configurationNameText = new JTextArea("");
        configurationNameText.setLineWrap(false);
        configurationNameText.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        configurationNameText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                manager.setSettingValue("ConfigurationName", configurationNameText.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                manager.setSettingValue("ConfigurationName", configurationNameText.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                manager.setSettingValue("ConfigurationName", configurationNameText.getText());
            }
        });

        configurationNameText.setText(manager.getSettingValue("ConfigurationName"));
        constraints.gridx = 0;
        constraints.gridy = ++rowIndex;
        constraints.gridwidth = 20;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(configurationNameText, constraints);

        // Add a label for the options of accepting self-signed
        // certificates
        JLabel certLabel = new JLabel(arrow + "Accept self-signed SSL " + "certificates when logging in to openBIS");
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
            String msg = "Unknown option for accepting self-signed " + "certificates! Defaulting to \""
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
                                    "This acquisition station has already " + "been assigned to another server!",
                                    "Error", JOptionPane.ERROR_MESSAGE);
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
                                    "This user directory has already been " + "assigned to another server!", "Error",
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
     * Refills the list of opeenBIS servers when their number has changed after a
     * user action.
     */
    private void refillServerList() {

        // Disable action listeners while we refill the list
        ActionListener[] actionListeners = openBISURLList.getActionListeners();
        for (ActionListener a : actionListeners) {
            openBISURLList.removeActionListener(a);
        }
        ArrayList<String> openBISURLOptions = manager.getAllServers();
        openBISURLList.removeAllItems();
        for (String currOpenBISURL : openBISURLOptions) {
            openBISURLList.addItem(currOpenBISURL);
        }
        openBISURLList.setSelectedItem(manager.getActiveServer());
        // Add the action listeners back
        for (ActionListener a : actionListeners) {
            openBISURLList.addActionListener(a);
        }
    }

    /**
     * Updates all fields with the values from current AppSettings
     */
    private void updateUI() {
        openBISURLList.setSelectedItem(manager.getActiveServer());
        acqStationsList.setSelectedItem(manager.getSettingValue("AcquisitionStation"));
        acqStationDescription.setText(manager.getAcqStationDescription(manager.getSettingValue("AcquisitionStation")));
        machineNameText.setText(manager.getSettingValue("HumanFriendlyHostName"));
        acceptSelfSignedCertsList.setSelectedItem(manager.getSettingValue("AcceptSelfSignedCertificates"));
        userdirButton.setText(manager.getSettingValue("UserDataDir"));
        dirButton.setText(manager.getSettingValue("DatamoverIncomingDir"));

        // Enable/disable buttons
        toggleDynamicWidgets();

    }

    /**
     * Enable/disable buttons depending on current selections
     */
    private void toggleDynamicWidgets() {
        boolean state = true;
        if (manager.getAllServers().size() < 2) {
            state = false;
        }
        remOpenBISURLButton.setEnabled(state);
        lowerOpenBISURLButton.setEnabled(state);
        higherOpenBISURLButton.setEnabled(state);

        state = true;
        if (manager.getCurrentIndex() == 0) {
            state = false;
        }
        higherOpenBISURLButton.setEnabled(state);

        state = true;
        if (manager.getCurrentIndex() == (manager.getAllServers().size() - 1)) {
            state = false;
        }
        lowerOpenBISURLButton.setEnabled(state);

        if (openBISURLList.getSelectedIndex() != 0) {
            urlLabel.setText(arrow + "Set the openBIS URL (current default: " + openBISURLList.getItemAt(0) + ")");
        } else {
            urlLabel.setText(arrow + "Set the openBIS URL (this is current default)");
        }
    }

}
