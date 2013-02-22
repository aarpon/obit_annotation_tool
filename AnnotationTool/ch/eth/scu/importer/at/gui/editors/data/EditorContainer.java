package ch.eth.scu.importer.at.gui.editors.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
	private JButton uploadButton;

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
		add(title, constraints);

		// Add the editor
		metadataEditor = EditorFactory.createEditor(dataViewer, openBISViewer);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		add(metadataEditor.getPanel(), constraints);
		
		// Add an upload button
		uploadButton = new JButton("Upload to openBIS");
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		uploadButton.addActionListener(this);
		add(uploadButton, constraints);
		
		// Set the preferred and minimum size
		this.setMinimumSize(new Dimension(450, 700));
		this.setPreferredSize(new Dimension(450, 700));
		

	}

	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == uploadButton) {
			
			// Make sure all models are ready
			if (!dataViewer.isReady() || !openBISViewer.isReady()) {
				outputPane.warn("Data models not ready!");
				return;
			}

			// Update the data model with the openBIS and user attributes
			if (!metadataEditor.updateDataModel()) {
				outputPane.warn("Metadata incomplete!");
				return;
			}
			
			// Get the application properties
			Properties appProperties = AppProperties.readPropertiesFromFile();
			String outputDirectory = 
					appProperties.getProperty("DatamoverIncomingDir");

			// Save to XML
			dataViewer.saveToXML(outputDirectory);
			outputPane.log("Metadata information stored. " +
			"Data is now ready for transfer.");
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
