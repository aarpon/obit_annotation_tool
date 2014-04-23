package ch.eth.scu.importer.microscopy.gui.editors.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ch.eth.scu.importer.at.gui.editors.data.AbstractEditor;
import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.at.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.at.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.at.gui.viewers.openbis.OpenBISViewer;
import ch.eth.scu.importer.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.eth.scu.importer.microscopy.gui.editors.data.model.MicroscopyMetadata;
import ch.eth.scu.importer.microscopy.processors.data.MicroscopyProcessor.Experiment;
import ch.eth.scu.importer.microscopy.processors.data.MicroscopyProcessor.MicroscopyFile;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 *
 */
public final class MicroscopyEditor extends AbstractEditor {

	// List of experiments from the Data Model
	private List<ExperimentNode> experiments = 
			new ArrayList<ExperimentNode>();
	
	// List of metadata mappers
	private List<MicroscopyMetadata> metadataMappersList =
			new ArrayList<MicroscopyMetadata>();

	// Indicate which of the List<MicroscopyMetadata> is the active one
	private int currentExperimentIndex = -1;

	private JLabel labelExpName;
	private JComboBox<String> comboProjectList;
	private JTextArea expDescription;
	
	/**
	 * Constructor
	 */
	public MicroscopyEditor(AbstractViewer dataViewer, 
			OpenBISViewer openBISViewer) {

		// Store the reference to the data and openBIS viewers
		super(dataViewer, openBISViewer);

		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		panel.setLayout(gridBagLayout);

	}

	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	}

	/**
	 * Once the metadata has been completely filled, this method makes
	 * sure to update the data model (generated by the Processor) to be
	 * ready to be saved to the XML properties file. 
	 */
	@Override
	public boolean updateDataModel() {
		
		// Check whether we have some experiments to update
		if (metadataMappersList.size() == 0) {
			return false;
		}

		// Go over all experiments
		for (MicroscopyMetadata currentMetadata : metadataMappersList) {
			
			// Get the experiment node
			ExperimentNode expNode = currentMetadata.experimentNode;
			assert(expNode.getType().equals("Experiment"));
			
			// We first start by updating the Experiment descriptor itself
			Experiment expDescr = (Experiment) expNode.getUserObject();
	
			// Set the openBIS experiment identifier
			Map<String, String> expOpenBISAttributes = 
					new Hashtable<String, String>();
			expOpenBISAttributes.put("openBISIdentifier", 
					currentMetadata.getOpenBISExerimentIdentifier());
			expOpenBISAttributes.put("openBISSpaceIdentifier", 
					currentMetadata.getOpenBISSpaceIdentifier());
			expDescr.addOpenBISAttributes(expOpenBISAttributes);

			// Set the description
			Map<String, String> expUserAttributes = 
					new Hashtable<String, String>();
			expUserAttributes.put("description", 
					currentMetadata.getExperiment().description); 
			expDescr.addUserAttributes(expUserAttributes);

			// Now get the MicroscopyFile children of the Experiment
			for (int i = 0; i < expNode.getChildCount(); i++) {
	
				// Get the i-th child node
				AbstractNode microscopyFileNode =
						(AbstractNode) expNode.getChildAt(i);
	
				// Get the MicroscopyFile Descriptor
				MicroscopyFile microscopyFileDescriptor =
						(MicroscopyFile) microscopyFileNode.getUserObject();
	
				// Make sure we have a Tray or a Specimen
				assert(microscopyFileNode.getType().equals("MicroscopyFile"));

				// Set the openBIS space and experiment identifiers
				Map<String, String> microscopyFileOpenBISAttributes = 
						new Hashtable<String, String>();
				microscopyFileOpenBISAttributes.put(
						"openBISSpaceIdentifier", 
						currentMetadata.getOpenBISSpaceIdentifier());
				microscopyFileOpenBISAttributes.put(
						"openBISExperimentIdentifier", 
						currentMetadata.getOpenBISExerimentIdentifier());
				microscopyFileDescriptor.addOpenBISAttributes(
						microscopyFileOpenBISAttributes);

			}
		}

		return true;
	}	

	/**
	 * Map the data and openBIS models
	 */
	@Override
	protected boolean initMetadata() {
		
		// Make sure both viewers have completed their models
		if (!openBISViewer.isReady() || !dataViewer.isReady()) {
			return false;
		}
		
		// Store the and openBIS nodes
		storeOpenBISProjects();
		storeExperiments();
		
		// Check that there is at least one entry in each of the 
		// arrays
		if (experiments.size() == 0 || openBISProjects.size() == 0) {
			return false;
		}
		
		// Create all MicroscopyMetadata objects and initially assign all
		// experiments to the first project
		for (ExperimentNode node : experiments) {
			metadataMappersList.add(
					new MicroscopyMetadata(
							node, openBISProjects.get(0)));
		}
		
		// Set the index of the experiment (if needed)
		if (currentExperimentIndex < 0
				|| currentExperimentIndex > (experiments.size() - 1)) {
			currentExperimentIndex = 0;
		}
		
		// Return success
		return true;
	}
	
	/**
	 * Renders all widgets on the panel
	 * @throws Exception if some openBIS identifiers cannot be computed
	 */
	@Override
	protected void createUIElements(ObserverActionParameters params) throws Exception {

		// Get selected metadata element
		MicroscopyMetadata metadata = metadataMappersList.get(
				currentExperimentIndex);

		// If we are repainting this panel, let's make sure we delete the
		// child components already displayed.
		clearUIElements();

		// Make sure both viewers have completed their models
		if (metadataMappersList.size() == 0) {
			return;
		}

		/*
		 *  Folder name
		 */
		
		// Constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		/*
		 *  Experiment name
		 */
		
		String expName = metadata.experimentNode.toString();
		
		// Create a label for the experiment
		constraints.insets = new Insets(0, 10, 0, 10);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		labelExpName = new JLabel(expName);
		labelExpName.setIcon(new ImageIcon(
				this.getClass().getResource("icons/experiment.png")));		
		panel.add(labelExpName, constraints);

		/*
		 * Description label
		 */
		
		// Create a label for the experiment description
		constraints.insets = new Insets(0, 10, 0, 10);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		JLabel labelExpDescription = new JLabel("Description");
		labelExpDescription.setHorizontalAlignment(JLabel.CENTER);
		panel.add(labelExpDescription, constraints);

		/*
		 * Description text area
		 */
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 2;
		expDescription = new JTextArea(
				metadata.getExperiment().description);
		expDescription.setLineWrap(true);
		expDescription.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateExpDescription();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateExpDescription();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateExpDescription();
			}
        });
		JScrollPane areaScrollPane = new JScrollPane(expDescription);
		areaScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setMinimumSize(new Dimension(400, 50));
		areaScrollPane.setPreferredSize(new Dimension(400, 50));
		panel.add(areaScrollPane, constraints);

		/*
		 *  Label openBIS projects
		 */

		// Create a label for the openBIS projects
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 3;
		JLabel labelProjects = new JLabel("Target openBIS project");
		labelProjects.setHorizontalAlignment(JLabel.CENTER);
		panel.add(labelProjects, constraints);
		
		/*
		 *  Tray openBIS projects
		 */
		
		// Add the project identifier
		comboProjectList = new JComboBox<String>();
		
		for (OpenBISProjectNode s : openBISProjects) {

			// Add the openBIS Project node
			comboProjectList.addItem(s.getIdentifier());

		}
		
		// Select the correct one
		for (int i = 0; i < openBISProjects.size(); i++) {
			if (openBISProjects.get(i).getIdentifier().equals(
					metadata.openBISProjectNode.getIdentifier())) {
				comboProjectList.setSelectedIndex(i);
				break;
			}
		}

		// When a project is selected, update the corresponding 
		// experiment in the data model 
		comboProjectList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("comboBoxChanged")) {

					// Get selected project identifier
					@SuppressWarnings("unchecked")
					String projectID;
                    projectID = (String)
                    ((JComboBox<String>)
                            e.getSource()).getSelectedItem();

					// Get the selected project node
					OpenBISProjectNode selProjNode = null;
					for (OpenBISProjectNode projNode : openBISProjects) {
						if (projNode.getIdentifier().equals(projectID)) {
							selProjNode = projNode;
							break;
						}
					}

					// Ask the user if he wants to set this project only 
					// to this experiment or to all
					Object[] options = {"To this only", "To all"};
					int n = JOptionPane.showOptionDialog(null,
					    "Set this project to this experiment only or to all?",
					    "Question",
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[0]);
					
					// Apply user's choice
					if (n == 1) {
						
						// Apply to all
						for (int i = 0; i < metadataMappersList.size(); i++) {
							metadataMappersList.get(i).openBISProjectNode = selProjNode;
						}
						
					} else {
						
						// Apply to current experiment only
						metadataMappersList.get(
								currentExperimentIndex).openBISProjectNode =
								selProjNode;
					}
				}
			}
		});

		// Add the project combo box
		constraints.insets = new Insets(0, 10, 0, 10);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 4;
		panel.add(comboProjectList, constraints);

		/*
		 *  Spacer
		 */
		
		// Add a spacer
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		panel.add(new JLabel(""), constraints);
		
		// Now redraw
		panel.validate();
		panel.repaint();

	}

	/**
	 * Update all widgets on the panel
	 */
	@Override
	protected void updateUIElements(ObserverActionParameters params) {
		
		// Update the currentExperimentIndex property
		currentExperimentIndex = experiments.indexOf(params.node);

		// Get the active metadata object
		MicroscopyMetadata metadata = metadataMappersList.get(
				currentExperimentIndex);

		// Update the experiment name
		labelExpName.setText(metadata.getExperimentName());

		// Update the description
		expDescription.setText(
				metadata.getExperiment().description);

		// Select the correct one
		for (int i = 0; i < openBISProjects.size(); i++) {
			if (openBISProjects.get(i).getIdentifier().equals(
					metadata.openBISProjectNode.getIdentifier())) {
				comboProjectList.setSelectedIndex(i);
				break;
			}
		}

	}

	/**
	 * Collects and stores data folders for mapping
	 */
	private void storeExperiments() {

		// Reset the Experiment list
		experiments = new ArrayList<ExperimentNode>();

		// Store the data model
		dataModel = dataViewer.getDataModel();
		
		// We extract all experiments from the data model
		RootNode dataRoot = (RootNode) dataModel.getRoot();
		
		// First level are the folder nodes 
		int dataNChildren = dataRoot.getChildCount();

		for (int i = 0; i < dataNChildren; i++) {

			// Get the FolderNode
			ExperimentNode experimentNode = 
					(ExperimentNode) dataRoot.getChildAt(i);

			// Store the reference to the ExperimentNode
			experiments.add(experimentNode);
		}        
	}

	/**
	 * We update the experiment description on the fly while the user 
	 * is typing in the Text Area.
	 */
	protected void updateExpDescription() {

		// Get the active metadata object
		MicroscopyMetadata metadata = metadataMappersList.get(
				currentExperimentIndex);
		
		// Store the experiment description
		metadata.getExperiment().description = expDescription.getText();
    }

	/**
	 * Discard metadata information since it went out of sync with the data
	 * and openBIS models.
	 */    
	@Override
	protected void resetMetadata(ObserverActionParameters p) {
		// Make sure it is the correct action
		if (p.action != ObserverActionParameters.Action.ABOUT_TO_RESCAN) {
			return;
		}
		metadataMappersList = new ArrayList<MicroscopyMetadata>();
	}

}
