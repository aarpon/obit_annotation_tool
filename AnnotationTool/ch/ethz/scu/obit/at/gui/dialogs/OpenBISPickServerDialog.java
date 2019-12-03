package ch.ethz.scu.obit.at.gui.dialogs;

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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import ch.ethz.scu.obit.common.settings.AppSettings;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;

/**
 * openBIS pick server dialog
 *
 * @author Aaron Ponti
 */
public class OpenBISPickServerDialog extends JDialog {

	/* Private instance variables */
	private static final long serialVersionUID = 1L;

	protected JComboBox<String> configurationsList;
	protected JCheckBox configurationSetDefault;
	private AppSettings currentConfiguration;
	private AppSettings selectedConfiguration;

	/**
	 * Constructor
	 *
	 * @param configurations       List of configurations.
	 * @param current              Name of the currently selected configuration.
	 * @param globalSettigsManager The global settings manager.
	 */
	public OpenBISPickServerDialog(ArrayList<AppSettings> configurations, AppSettings current,
			final GlobalSettingsManager globalSettigsManager) {

		// Store the currently selected configuration and keep track of
		// what the original selection was in case the user cancels
		// the selection.
		currentConfiguration = current;
		selectedConfiguration = current;

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
		JLabel label = new JLabel("Please choose the openBIS server you want to use:");
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(label, constraints);

		// Add a drop-down menu for the selection of the configuration
		configurationsList = new JComboBox<String>();
		for (AppSettings configuration : configurations) {
			configurationsList.addItem(configuration.getConfigurationName());
		}
		configurationsList.setSelectedItem(current);
		configurationsList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("comboBoxChanged")) {

					// Selected configuration name
					String selectedConfigurationName = (String) configurationsList.getSelectedItem();

					for (AppSettings settings : configurations) {
						if (settings.getConfigurationName().equals(selectedConfigurationName)) {
							selectedConfiguration = settings;
							return;
						}
					}

					// If not found (this should not happen!), fall back to the first
					selectedConfiguration = configurations.get(0);
				}
			}
		});
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(configurationsList, constraints);

		// Add a checkbox
		configurationSetDefault = new JCheckBox("Set selected as default server");
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(configurationSetDefault, constraints);

		// Create a "Select" button
		JButton selectButton = new JButton("Select");
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 0.50;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Store the default server if so requested by the user
				if (configurationSetDefault.isSelected()) {
					globalSettigsManager.setFavoriteConfiguration(selectedConfiguration.getConfigurationName());
				}
				setVisible(false);
				dispose();
			}
		});
		add(selectButton, constraints);

		// Create a cancel button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Restore the original server
				selectedConfiguration = currentConfiguration;
				setVisible(false);
				dispose();
			}
		});
		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 0.50;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 0, 5, 5);
		add(cancelButton, constraints);

		// Make the ok button react to the enter key
		getRootPane().setDefaultButton(selectButton);

		// If the user just closes the login dialog without trying to log in
		// we ask him if he wants to quit the application, otherwise we insist
		// that he logs in since we need valid login credentials for the proper
		// functioning of the application.
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Restore the original server
				selectedConfiguration = currentConfiguration;
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
	 * Get the selected configuration.
	 *
	 * @return name of the selected configuration.
	 */
	public AppSettings getSelectedConfiguration() {
		return selectedConfiguration;
	}

}
