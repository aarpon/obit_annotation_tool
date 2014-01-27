package ch.eth.scu.importer.at.gui.editors.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.eth.scu.importer.at.datamover.ATDataMover;
import ch.eth.scu.importer.at.gui.pane.OutputPane;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.openbis.OpenBISViewer;
import ch.eth.scu.importer.common.settings.AppSettingsManager;

public class EditorContainer extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private AbstractEditor metadataEditor;
	private JButton registerButton;

	protected AbstractViewer dataViewer;
	protected OpenBISViewer openBISViewer;
	protected OutputPane outputPane;
	
	/**
	 * Constructor
	 */
	public EditorContainer(AbstractViewer dataViewer, 
			OpenBISViewer openBISViewer, OutputPane outputPane) {
		
		// Store the references
		this.dataViewer = dataViewer;
		this.openBISViewer = openBISViewer;
		this.outputPane = outputPane;
		
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
		constraints.insets = new Insets(5, 5, 5, 5);
		add(title, constraints);

		// Add the editor
		metadataEditor = EditorFactory.createEditor(dataViewer, openBISViewer);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		add(metadataEditor.getPanel(), constraints);
		
		// Add an upload button
		registerButton = new JButton("Send to openBIS");
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		registerButton.addActionListener(this);
		add(registerButton, constraints);
		
		// Set the preferred and minimum size
		this.setMinimumSize(new Dimension(400, 700));
		this.setPreferredSize(new Dimension(400, 700));
		

	}

	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == registerButton) {
			
			// Make sure all models are ready
			if (!dataViewer.isReady() || !openBISViewer.isReady()) {
				outputPane.warn("Data is not ready to be sent to openBIS!");
				return;
			}

			// Update the data model with the openBIS and user attributes
			if (!metadataEditor.updateDataModel()) {
				outputPane.warn("No new datasets ready for upload!");
				return;
			}

			// Get the application properties
			AppSettingsManager manager = new AppSettingsManager();
			String outputDirectory = 
					manager.getSettingValue("UserDataDir");

			// Save to XML (*_properties.oix)
			if (dataViewer.saveToXML(outputDirectory)) {
				outputPane.log("Annotations successfully written.");
			} else {
				outputPane.err("Could not write annotations! " +
					"Sending data to openBIS failed!");
				return;
			}

			// Save the data structure file (data_structure.ois)
			if (dataViewer.saveDataStructureMap(
					outputDirectory + File.separator +
					openBISViewer.getUserName())) {
				outputPane.log("Data structure map successfully written.");
			} else {
				outputPane.err("Could not data structure map! " +
					"Sending data to openBIS failed!");
				return;
			}

			// Now move the user folder to the datamover incoming folder
			new ATDataMover(openBISViewer.getUserName()).move();
			outputPane.log("Data transferred.");
			
			// Re-scan
			dataViewer.scan();
		}
    }
	
	/**
	 * Return a reference to the contained editor
	 * @return a reference to the editor
	 */
	public AbstractEditor getEditor() {
		return metadataEditor;
	}
	
}
