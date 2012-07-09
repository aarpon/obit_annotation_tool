package ch.eth.scu.importer.gui.panels;

import java.util.Observable;
import java.util.Observer;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.eth.scu.importer.common.properties.DropboxProperties;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 */
public class MetadataEditor extends JPanel implements Observer, ActionListener {

	private static final long serialVersionUID = 1L;

	private JButton uploadButton;
	private JLabel labelDataValue;
	private JLabel labelExpValue;
	
	private DropboxProperties dropboxProperties;
	
	/**
	 * Constructor
	 */
	public MetadataEditor(DropboxProperties dropboxProperties) {

		// Store the DropboxProperties object
		this.dropboxProperties = dropboxProperties;
		
		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);

		// Common constraints
		GridBagConstraints constraints = new GridBagConstraints();
		
		// Add a title JLabel
		JLabel title = new JLabel("Metadata editor");

		// Add the tree viewer to the layout
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		add(title, constraints);

		// Add a spacer
		JLabel firstSpacer = new JLabel("");
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		add(firstSpacer, constraints);
		
		// Add a JLabel for the experiment selection title
		JLabel labelExpTitle = new JLabel("Data item");
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		add(labelExpTitle, constraints);

		// Add a JLabel for the experiment value
		labelDataValue = new JLabel("undefined");
		labelDataValue.setAlignmentX(CENTER_ALIGNMENT);
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		add(labelDataValue, constraints);
		
		// Add a spacer
		JLabel openBISExpLabel = new JLabel("OpenBIS experiment");
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		add(openBISExpLabel, constraints);

		// Add a JLabel for the experiment value
		labelExpValue = new JLabel("undefined");
		labelExpValue.setAlignmentX(CENTER_ALIGNMENT);
		constraints.gridx = 0;
		constraints.gridy = 5;
		add(labelExpValue, constraints);
		
		// Add a spacer
		JLabel secondSpacer = new JLabel("");
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridheight = GridBagConstraints.REMAINDER;
		add(secondSpacer, constraints);
		
		// Add an upload button
		uploadButton = new JButton("Upload to openBIS");
		uploadButton.setEnabled(false);
		constraints.anchor = GridBagConstraints.SOUTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 7;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		uploadButton.addActionListener(this);
		add(uploadButton, constraints);

		// Set sizes
		setMinimumSize(new Dimension(400, 700));
		setPreferredSize(new Dimension(400, 700));
	}

	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == uploadButton) {
			String experiment = dropboxProperties.get("Experiment");
			if (experiment == null) {
				// Experiment not set yet
				return;
			}
			dropboxProperties.writePropertiesToFile();
			System.out.println("File written to disk (TODO: Use the output pane).");
		}
    }

	/**
	 * Update method from the Observer interface
	 * @param obj The observable object
	 * @param arg The argument passed by the observable object on change
	 */
	public void update(Observable obj, Object arg) {
		if (!(arg instanceof String)) {
			return;
		}
		
		// Get key-value pair
		String key = (String) arg;
		String value = dropboxProperties.get(key);
		if (value == null) {
			System.err.println("The selected key does not exist!");
			return;
		}
		
		// Update
		if (key.equals("Experiment")) {
			
			// Get the value
			// Display the value in the editor
			labelExpValue.setText(value);
			
			// Enable the upload button if experiment is set
			if (value.equals("")) {
				uploadButton.setEnabled(false);
			} else {
				uploadButton.setEnabled(true);
			}
		}
	}
}
