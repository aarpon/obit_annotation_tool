package ch.eth.scu.importer.gui.editors.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.tree.TreeModel;

import ch.eth.scu.importer.gui.editors.data.model.BDLSRFortessaFCSMetadata;
import ch.eth.scu.importer.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.gui.viewers.data.model.FolderNode;
import ch.eth.scu.importer.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.gui.viewers.openbis.OpenBISViewer;
import ch.eth.scu.importer.gui.viewers.openbis.model.AbstractOpenBISNode;
import ch.eth.scu.importer.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.Experiment;
import ch.eth.scu.importer.processor.model.AbstractDescriptor;
import ch.eth.scu.importer.processor.model.DatasetDescriptor;
import ch.eth.scu.importer.processor.model.SampleDescriptor;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 */
public class BDLSRFortessaFCSEditor extends AbstractEditor {

	protected TreeModel dataModel;
	protected TreeModel openBISModel;
	
	protected List<OpenBISProjectNode> openBISProjects = 
			new ArrayList<OpenBISProjectNode>();

	// List of experiments from the Data Model
	protected List<FolderNode> dataFolders = 
			new ArrayList<FolderNode>();
	
	protected List<BDLSRFortessaFCSMetadata> experimentMetadata =
			new ArrayList<BDLSRFortessaFCSMetadata>();

	// Indicate which of the List<BDLSRFortessaFCSMetadata> is the 
	// active one
	protected int currentExperimentIndex = -1;

	protected JLabel labelFolderName;
	protected JLabel labelExpName;
	protected JComboBox<String> comboGeometryList;
	protected JComboBox<OpenBISProjectNode> comboProjectList;
	
	/**
	 * Constructor
	 */
	public BDLSRFortessaFCSEditor(AbstractViewer dataViewer, 
			OpenBISViewer openBISViewer) {

		// Store the reference to the data and openBIS viewers
		super(dataViewer, openBISViewer);

		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		panel.setLayout(gridBagLayout);

		// Set sizes
		panel.setMinimumSize(new Dimension(400, 700));
		panel.setPreferredSize(new Dimension(400, 700));
	}

	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	public void actionPerformed(ActionEvent e) {
	}

	/**
	 * Map the data and openBIS models
	 * @throws Exception 
	 */
	public void init(ObserverActionParameters params) throws Exception {
		
		// Make sure both viewers have completed their models
		if (!openBISViewer.isReady() || !dataViewer.isReady()) {
			return;
		}
		
		// Init the metadata
		if (initMetadata()) {
			
			// Create the widgets
			createUIElements(params);

		}
		
	}
	
	/**
	 * Map the data and openBIS models
	 * @throws Exception 
	 */
	private boolean initMetadata() {
		
		// Make sure both viewers have completed their models
		if (!openBISViewer.isReady() || !dataViewer.isReady()) {
			return false;
		}
		
		// Store the and openBIS nodes
		storeOpenBISProjects();
		storeDataFolders();
		
		// Check that there is at least one entry in each of the 
		// arrays
		if (dataFolders.size() == 0 || openBISProjects.size() == 0) {
			// TODO: Inform the user
			return false;
		}
		
		// Create all BDLSRFortessaFCSMetadata objects
		for (FolderNode node : dataFolders) {
			experimentMetadata.add(
					new BDLSRFortessaFCSMetadata(
							node, openBISProjects.get(0)));
		}
		
		// Initially we set the first openBIS project as a target
		// for all experiments
		currentExperimentIndex = 0;
		
		// Return success
		return true;
	}
	
	/**
	 * Renders all widgets on the panel
	 * @throws Exception if some openBIS identifiers cannot be computed
	 */
	private void createUIElements(ObserverActionParameters params) throws Exception {

		// Make sure both viewers have completed their models
		if (experimentMetadata.size() == 0) {
			return;
		}

		// If we are repainting this panel, let's make sure we delete the
		// child components already displayed.
		if (panel.getComponentCount() > 0) {
			panel.removeAll();
			panel.revalidate(); 
		}

		// Get selected metadata element
		BDLSRFortessaFCSMetadata metadata = experimentMetadata.get(
				currentExperimentIndex);
		
		/*
		 *  Folder name
		 */
		
		// Constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		// Create a label for the folder
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		labelFolderName = new JLabel(metadata.folderNode.toString());
		panel.add(labelFolderName, constraints);

		/*
		 *  Experiment name
		 */
		
		String expName = metadata.folderNode.getChildAt(0).toString();
		
		// Create a label for the experiment
		constraints.insets = new Insets(0, 20, 0, 20);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		labelExpName = new JLabel(expName); 
		panel.add(labelExpName, constraints);

		/*
		 *  Tray geometry label
		 */

		// Create a label for the geometry combo box
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 2;
		JLabel labelGeometry = new JLabel("Plate geometry");
		labelGeometry.setHorizontalAlignment(JLabel.CENTER);
		panel.add(labelGeometry, constraints);
		
		/*
		 *  Tray geometry combo
		 */
		
		// Display a combo box with the geometries
		ArrayList<String> supportedGeometries =
				metadata.supportedTrayGeometries;
		comboGeometryList = new JComboBox<String>();
		for (String geometry : supportedGeometries) {

			// Add the geometry
			comboGeometryList.addItem(geometry);

		}		

		// Select the correct one
		int indx = supportedGeometries.indexOf(
				metadata.trayGeometry);
		comboGeometryList.setSelectedIndex(supportedGeometries.indexOf(
				metadata.trayGeometry));

		// When a geometry is selected, update the corresponding 
		// experiment in the data model 
		comboGeometryList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("comboBoxChanged")) {

					String geometry =
							(String)
							((JComboBox<String>)
									e.getSource()).getSelectedItem();
					
					// Update the metadata object with the new projects
					experimentMetadata.get(
							currentExperimentIndex).trayGeometry =
							geometry;

				}
			}
		});

		// Add the geometry combo box
		constraints.insets = new Insets(0, 20, 0, 20);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 3;
		panel.add(comboGeometryList, constraints);

		/*
		 *  Label openBIS projects
		 */

		// Create a label for the openBIS projects
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 4;
		JLabel labelProjects = new JLabel("Target openBIS project");
		labelProjects.setHorizontalAlignment(JLabel.CENTER);
		panel.add(labelProjects, constraints);
		
		/*
		 *  Tray openBIS projects
		 */
		
		// Store the project in a JCombo box
		comboProjectList = new JComboBox<OpenBISProjectNode>();
		
		for (OpenBISProjectNode s : openBISProjects) {

			// Add the BDLSRFortessaFCSMetadata object
			comboProjectList.addItem(s);

		}
		
		// Select the correct one
		comboProjectList.setSelectedIndex(openBISProjects.indexOf(
				metadata.openBISProjectNode));

		// When a project is selected, update the corresponding 
		// experiment in the data model 
		comboProjectList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("comboBoxChanged")) {

					// Get the BDLSRFortessaFCSMetadata object
					OpenBISProjectNode projectNode =
							(OpenBISProjectNode)
							((JComboBox<OpenBISProjectNode>)
									e.getSource()).getSelectedItem();
					
					// Update the metadata object with the new projects
					experimentMetadata.get(
							currentExperimentIndex).openBISProjectNode =
							projectNode;

				}
			}
		});
	
		// Add the project combo box
		constraints.insets = new Insets(0, 20, 0, 20);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 5;
		panel.add(comboProjectList, constraints);

		/*
		 *  Spacer
		 */
		
		// Add a spacer
		constraints.gridx = 0;
		constraints.gridy = 6;
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
	private void updateUIElements() {
		
		// Get the active metadata object
		BDLSRFortessaFCSMetadata metadata = experimentMetadata.get(
				currentExperimentIndex);

		// Update the folder name
		labelFolderName.setText(metadata.getFolderName());
		
		// Update the experiment name
		labelExpName.setText(metadata.getExperimentName());
		
		// Update the geometry
		comboGeometryList.setSelectedIndex(
				metadata.supportedTrayGeometries.indexOf(
				metadata.trayGeometry));

		// Update the project
		comboProjectList.setSelectedIndex(openBISProjects.indexOf(
				metadata.openBISProjectNode));
		
		// TODO: Add description
	}
	
	/**
	 * Update metadata and UI
	 */
	public void updateAll(ObserverActionParameters params) {

		// Update the currentExperimentIndex property
		currentExperimentIndex = dataFolders.indexOf(params.node);

		// Update the UI
		updateUIElements();
	}
	

	/**
	 * Collects and stores openBIS projects for mapping
	 * @return list of openBIS nodes
	 */
	private void storeOpenBISProjects() {
		
		// Store the openBIS model
		openBISModel = openBISViewer.getDataModel();
		
        // We extract all projects from the openBIS model and create a list
		// with which we will then create JComboBox associated to each project
		// from the data model
		openBISProjects = new ArrayList<OpenBISProjectNode>();
		
		AbstractOpenBISNode openBISRoot = 
				(AbstractOpenBISNode) openBISModel.getRoot();

		// Iterate over the space nodes (there should be at least one)
		for (int i = 0; i < openBISRoot.getChildCount(); i++) {

			// Get the Space
			AbstractOpenBISNode openBISSpaceNode = 
					(AbstractOpenBISNode) openBISRoot.getChildAt(i);

			// Go over the child Projects
			int n = openBISSpaceNode.getChildCount();

			for (int j = 0; j < n; j++) {

				// Get the node
				OpenBISProjectNode openBISProjectNode = 
						(OpenBISProjectNode) openBISSpaceNode.getChildAt(j);

				// Add it to the list
				openBISProjects.add(openBISProjectNode);

			}
		}
	}

	/**
	 * Collects and stores data folders for mapping
	 * @return list of openBIS nodes
	 */
	private void storeDataFolders() {

		// Reset the dataFolders list
		dataFolders = new ArrayList<FolderNode>();

		// Store the data model
		dataModel = dataViewer.getDataModel();
		
		// We extract all experiments from the data model
		RootNode dataRoot = (RootNode) dataModel.getRoot();
		
		// First level are the folder nodes 
		int dataNChildren = dataRoot.getChildCount();

		for (int i = 0; i < dataNChildren; i++) {

			// Get the FolderNode
			FolderNode folderNode = (FolderNode) dataRoot.getChildAt(i);

				// Store the reference to the ExperimentNode
				dataFolders.add(folderNode);
		}        
	}

}
