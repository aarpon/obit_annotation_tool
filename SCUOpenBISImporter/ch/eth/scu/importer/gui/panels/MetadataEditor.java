package ch.eth.scu.importer.gui.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 *
 */
public class MetadataEditor extends JPanel {

	private static final long serialVersionUID = 1L;

	private JButton uploadButton;
	
	/**
	 * Constructor
	 */
	public MetadataEditor() {

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
		JLabel spacer = new JLabel("");
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridheight = GridBagConstraints.REMAINDER;
		add(spacer, constraints);
		
		// Add an upload button
		uploadButton = new JButton("Upload to openBIS");
		constraints.anchor = GridBagConstraints.SOUTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		add(uploadButton, constraints);

		// Set sizes
		setMinimumSize(new Dimension(400, 800));
		setPreferredSize(new Dimension(400, 800));
	}
	
}
