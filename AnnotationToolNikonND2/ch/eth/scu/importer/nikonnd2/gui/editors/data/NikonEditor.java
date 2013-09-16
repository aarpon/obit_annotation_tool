package ch.eth.scu.importer.nikonnd2.gui.editors.data;

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
import ch.eth.scu.importer.lsrfortessa.gui.editors.data.model.BDLSRFortessaFCSMetadata;
import ch.eth.scu.importer.nikonnd2.gui.editors.data.model.NikonMetadata;
import ch.eth.scu.importer.nikonnd2.processors.NikonProcessor.Experiment;
import ch.eth.scu.importer.nikonnd2.processors.NikonProcessor.ND2File;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 *
 */
public class NikonEditor extends AbstractEditor {

	// List of experiments from the Data Model
	protected List<ExperimentNode> experiments = 
			new ArrayList<ExperimentNode>();
	
	// List of metadata mappers
	protected List<NikonMetadata> metadataMappersList =
			new ArrayList<NikonMetadata>();

	// Indicate which of the List<NikonMetadata> is the active one
	protected int currentExperimentIndex = -1;

	protected JLabel labelExpName;
	protected JComboBox<String> comboGeometryList;
	protected JComboBox<String> comboProjectList;
	protected JTextArea expDescription;
	
	/**
	 * Constructor
	 */
	public NikonEditor(AbstractViewer dataViewer, 
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
		for (NikonMetadata currentMetadata : metadataMappersList) {
			
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

			// Now get the ND2File children of the Experiment
			for (int i = 0; i < expNode.getChildCount(); i++) {
	
				// Get the i-th child node
				AbstractNode nd2FileNode =
						(AbstractNode) expNode.getChildAt(i);
	
				// Get the ND2File Descriptor
				ND2File nd2FileDescriptor =
						(ND2File) nd2FileNode.getUserObject();
	
				// Make sure we have a Tray or a Specimen
				assert(nd2FileNode.getType().equals("ND2File"));

				// Set the openBIS space and experiment identifiers
				Map<String, String> ND2FileOpenBISAttributes = 
						new Hashtable<String, String>();
				ND2FileOpenBISAttributes.put(
						"openBISSpaceIdentifier", 
						currentMetadata.getOpenBISSpaceIdentifier());
				ND2FileOpenBISAttributes.put(
						"openBISExperimentIdentifier", 
						currentMetadata.getOpenBISExerimentIdentifier());
				nd2FileDescriptor.addOpenBISAttributes(
						ND2FileOpenBISAttributes);

			}
		}

		return true;
	}	

	/**
	 * Map the data and openBIS models
	 * @throws Exception 
	 */
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
			// TODO: Inform the user
			return false;
		}
		
		// Create all NikonMetadata objects and initially assign all
		// experiments to the first project
		for (ExperimentNode node : experiments) {
			metadataMappersList.add(
					new NikonMetadata(
							node, openBISProjects.get(0)));
		}
		
		// Set the index of the experiment (if needed)
		if (	currentExperimentIndex < 0 ||
				currentExperimentIndex > (experiments.size() - 1)) {
			currentExperimentIndex = 0;
		}
		
		// Return success
		return true;
	}
	
	/**
	 * Renders all widgets on the panel
	 * @throws Exception if some openBIS identifiers cannot be computed
	 */
	protected void createUIElements(ObserverActionParameters params) throws Exception {

		// Get selected metadata element
		NikonMetadata metadata = metadataMappersList.get(
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
		constraints.insets = new Insets(0, 20, 0, 20);
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
		constraints.insets = new Insets(0, 20, 0, 20);
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
		constraints.insets = new Insets(10, 10, 10, 10);
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

			// Add the BDLSRFortessaFCSMetadata object
			comboProjectList.addItem(
					((OpenBISProjectNode)s).getIdentifier());

		}
		
		// Select the correct one
		for (int i = 0; i < openBISProjects.size(); i++) {
			System.out.println(openBISProjects.get(i).getIdentifier());
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
					String projectID =
							(String)
							((JComboBox<String>)
									e.getSource()).getSelectedItem();

					// Get the ProjectNode that matches the identifier
					for (OpenBISProjectNode projNode : openBISProjects) {
						if (projNode.getIdentifier().equals(projectID)) {
							metadataMappersList.get(
									currentExperimentIndex).openBISProjectNode =
											projNode;
							break;
						}
					}

				}
			}
		});
	
		// Add the project combo box
		constraints.insets = new Insets(0, 20, 0, 20);
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
		
		// In case this was called when then window was already visible (i.e.
		// if the login failed the first time and this panel was drawn without
		// children)
		panel.revalidate();

	}

	/**
	 * Update all widgets on the panel
	 */
	protected void updateUIElements(ObserverActionParameters params) {
		
		// Update the currentExperimentIndex property
		currentExperimentIndex = experiments.indexOf(params.node);

		// Get the active metadata object
		NikonMetadata metadata = metadataMappersList.get(
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
	 * @return list of openBIS nodes
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
		NikonMetadata metadata = metadataMappersList.get(
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
		metadataMappersList = new ArrayList<NikonMetadata>();
	}

}
