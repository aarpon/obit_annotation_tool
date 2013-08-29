package ch.eth.scu.importer.at.gui.editors.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.eth.scu.importer.at.gui.pane.OutputPane;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.openbis.OpenBISViewer;
import ch.eth.scu.importer.common.properties.AppProperties;

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
				outputPane.warn("Data models not ready!");
				return;
			}

			// Update the data model with the openBIS and user attributes
			if (!metadataEditor.updateDataModel()) {
				outputPane.warn("No new datasets ready for upload!");
				return;
			}

			// Get the application properties
			Properties appProperties = AppProperties.readPropertiesFromFile();
			String outputDirectory = 
					appProperties.getProperty("UserDataDir");

			// Save to XML
			dataViewer.saveToXML(outputDirectory);
			outputPane.log("Metadata information stored. " +
			"Data is now ready for transfer.");
			
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
