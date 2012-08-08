package ch.eth.scu.importer.gui.panels.editors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.gui.panels.openbis.OpenBISSpaceViewer;
import ch.eth.scu.importer.gui.panels.viewers.AbstractViewer;

public class EditorContainer extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private AbstractEditor metadataEditor;
	private JButton uploadButton;

	protected AbstractViewer dataViewer;
	protected OpenBISSpaceViewer openBISViewer;
	
	/**
	 * Constructor
	 */
	public EditorContainer(AbstractViewer dataViewer, 
			OpenBISSpaceViewer openBISViewer) {
		
		// Store the references
		this.dataViewer = dataViewer;
		this.openBISViewer = openBISViewer;
		
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

		// Get the application properties
		Properties appProperties = AppProperties.readPropertiesFromFile();

		// Add the actual editor
		String acqStation = appProperties.getProperty("AcquisitionStation");	
		if (acqStation.equals("LSRFortessa")) {
			metadataEditor = new BDLSRFortessaEditor(dataViewer, openBISViewer);
		} else if (acqStation.equals("LeicaSP5")) {
			metadataEditor = new LeicaSP5Editor(dataViewer, openBISViewer);
		} else {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		}
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

	}

	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == uploadButton) {
			// Get the application properties
			Properties appProperties = AppProperties.readPropertiesFromFile();
			// TODO - Correct XML file name!
			File xmlFile = new File(
					appProperties.getProperty("DatamoverIncomingDir") + 
					File.separator + "properties.xml");
			try {
				dataViewer.saveToXML(xmlFile.getCanonicalPath());
				System.out.println(
						"File written to disk (TODO: Use the output pane).");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.err.println(
						"Could not write xml file to disk (TODO: Use the output pane).");
			}
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
