package ch.ethz.scu.obit.updater.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ch.ethz.scu.obit.common.settings.AppUpdaterSettingsManager;
import ch.ethz.scu.obit.common.version.VersionInfo;


/**
 * AnnotationTool Updater Settings Dialog
 * @author Aaron Ponti
 */
public class AnnotationToolUpdaterSettingsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    boolean useProxy;
    String proxyServer;
    int proxyPort;

    protected AppUpdaterSettingsManager manager;
    
    JCheckBox useProxyCheckBox;
    JLabel proxyServerLabel; 
    JTextArea proxyServerText;
    JLabel proxyPortLabel; 
    JTextArea proxyPortText;

    JButton saveButton;
    JButton closeButton;

    /**
     * Constructor
     * @param manager Manager for the Updater settings.
     */
    public AnnotationToolUpdaterSettingsDialog(final AppUpdaterSettingsManager manager) {

        // This is used only locally 
        boolean useProxy = manager.getSettingValue("useProxyServer").equals("1");
       
        // Set the dialog title
        setTitle("Annotation Tool Updater v" + VersionInfo.version + " " +
        VersionInfo.status + ": Settings");

        // Make the dialog modal and not resizable
        setModal(true);
        setResizable(false);

        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        // Create constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;

        // Add checkbox for the proxy usage
        useProxyCheckBox = new JCheckBox("Use proxy to connect to remote server?");
        useProxyCheckBox.setSelected(useProxy);
        useProxyCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {

                JCheckBox o = (JCheckBox)e.getItemSelectable();
                if (o.isSelected()) {

                    // Enable proxy
                    manager.setSettingValue("useProxyServer", "1");

                    // Enable the other configuration fields
                    enableSettingsWidgets();

                } else {

                    // Disable proxy
                    manager.setSettingValue("useProxyServer", "0");

                    // Disable the other configuration fields
                    disableSettinsWidgets();
                }

            }
        });
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 20;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(useProxyCheckBox, constraints);

        // Add a label for the proxy server name
        proxyServerLabel = new JLabel("Proxy server name");
        proxyServerLabel.setEnabled(useProxy);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 20;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(proxyServerLabel, constraints);        

        // Add a text field for the human-friendly machine name
        proxyServerText = new JTextArea("");
        proxyServerText.setEnabled(useProxy);
        proxyServerText.setLineWrap(false);
        proxyServerText.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        proxyServerText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                manager.setSettingValue("proxyServerName",
                        proxyServerText.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                manager.setSettingValue("proxyServerName",
                        proxyServerText.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                manager.setSettingValue("proxyServerName",
                        proxyServerText.getText());
            }
        });
        proxyServerText.setText("");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 20;
        constraints.gridheight = 1;     
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(proxyServerText, constraints);

        // Add a label for the proxy server port
        proxyPortLabel = new JLabel("Proxy server port");
        proxyPortLabel.setEnabled(useProxy);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 20;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(proxyPortLabel, constraints);        

        // Add a text field for the human-friendly machine name
        proxyPortText = new JTextArea("");
        proxyPortText.setEnabled(useProxy);
        proxyPortText.setLineWrap(false);
        proxyPortText.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        proxyPortText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                manager.setSettingValue("proxyServerPort",
                        proxyPortText.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                manager.setSettingValue("proxyServerPort",
                        proxyPortText.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                manager.setSettingValue("proxyServerPort",
                        proxyPortText.getText());
            }
        });
        proxyPortText.setText("");
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 20;
        constraints.gridheight = 1;     
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(proxyPortText, constraints);

        // Some spacer
        JLabel spacerLabel = new JLabel("");
        constraints.gridx = 0;
        constraints.gridy = 5;
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
                    JOptionPane.showMessageDialog(null,
                            manager.getLastErrorMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);

                    return;
                }

                // Save
                if (!manager.save()) {
                    JOptionPane.showMessageDialog(null,
                            "Error saving settings: " +
                                    manager.getLastErrorMessage(),
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
        constraints.gridy = 6;
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
                
                // Are all the Settings set?
                if (!manager.allSet()) {
                    JOptionPane.showMessageDialog(null,
                            manager.getLastErrorMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);

                    return;
                }
                
                // Just hide
                setVisible(false);
            }
        });
        constraints.gridx = 18;
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(closeButton, constraints);

        // Make the login button react to the enter key
        getRootPane().setDefaultButton(saveButton);

        // Display the dialog
        setMinimumSize(new Dimension(400, 200));
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setVisible(true);

    }
    
    /**
     * Enable all setting widgets
     */
    private void enableSettingsWidgets() {
        proxyServerLabel.setEnabled(true);
        proxyServerText.setEnabled(true);
        proxyPortLabel.setEnabled(true);
        proxyPortText.setEnabled(true);
    }

    /**
     * Disable all setting widgets
     */
    private void disableSettinsWidgets() {
        proxyServerLabel.setEnabled(false);
        proxyServerText.setEnabled(false);
        proxyPortLabel.setEnabled(false);
        proxyPortText.setEnabled(false);        
    }

}
