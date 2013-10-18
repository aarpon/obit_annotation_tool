package ch.eth.scu.importer.lsrfortessa.gui.editors.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
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
import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor.Experiment;
import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor.Tray;
import ch.eth.scu.importer.lsrfortessa.processors.model.SampleDescriptor;
import ch.eth.scu.importer.processors.model.AbstractDescriptor;
import ch.eth.scu.importer.processors.model.DatasetDescriptor;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 */
public class BDLSRFortessaFCSEditor extends AbstractEditor {

	// List of experiments from the Data Model
	protected List<ExperimentNode> experiments = 
			new ArrayList<ExperimentNode>();
	
	// List of metadata mappers
	protected List<BDLSRFortessaFCSMetadata> metadataMappersList =
			new ArrayList<BDLSRFortessaFCSMetadata>();

	// Indicate which of the List<BDLSRFortessaFCSMetadata> is the 
	// active one
	protected int currentExperimentIndex = -1;

	protected JLabel labelFolderName;
	protected JLabel labelExpName;
	protected Map<JComboBox<String>, Tray> comboGeometryList;
	protected JComboBox<String> comboProjectList;
	protected JTextArea expDescription;

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
		for (BDLSRFortessaFCSMetadata metadata : metadataMappersList) {
			
			// Get the experiment node
			ExperimentNode expNode = 
					(ExperimentNode) metadata.expNode;
			assert(expNode.getType().equals("Experiment"));
			
			// We first start by updating the Experiment descriptor itself
			Experiment expDescr = (Experiment) expNode.getUserObject();
	
			// Set the openBIS experiment identifier
			Map<String, String> expOpenBISAttributes = 
					new Hashtable<String, String>();
			expOpenBISAttributes.put("openBISIdentifier", 
					metadata.getOpenBISExerimentIdentifier());
			expOpenBISAttributes.put("openBISSpaceIdentifier", 
					metadata.getOpenBISSpaceIdentifier());
			expDescr.addOpenBISAttributes(expOpenBISAttributes);

			// Set the description
			Map<String, String> expUserAttributes = 
					new Hashtable<String, String>();
			expUserAttributes.put("description", expDescr.description); 
			expDescr.addUserAttributes(expUserAttributes);

			// Now get the Trays and Specimens children of the Experiment
			for (int i = 0; i < expNode.getChildCount(); i++) {
	
				// Get the i-th child node
				AbstractNode firstLevelSampleNode =
						(AbstractNode) expNode.getChildAt(i);
	
				// Get the Sample Descriptor
				SampleDescriptor firstLevelSample =
						(SampleDescriptor) firstLevelSampleNode.getUserObject();
	
				// Make sure we have a Tray or a Specimen
				assert((firstLevelSample.getType().equals("Tray") ||
	                    firstLevelSample.getType().equals("Specimen")));

				// Set the openBIS space and experiment identifiers
				Map<String, String> firstLevelOpenBISAttributes = 
						new Hashtable<String, String>();
				firstLevelOpenBISAttributes.put(
						"openBISSpaceIdentifier", 
						metadata.getOpenBISSpaceIdentifier());
				firstLevelOpenBISAttributes.put(
						"openBISExperimentIdentifier", 
						metadata.getOpenBISExerimentIdentifier());
				firstLevelSample.addOpenBISAttributes(
						firstLevelOpenBISAttributes);

				// In case we have Trays, we set their geometry
				if (firstLevelSample.getType().equals("Tray")) {
					Tray tray = (Tray) firstLevelSample;
					Map<String, String> firstLevelUserAttributes =
							new Hashtable<String, String>();
					firstLevelUserAttributes.put(
							"trayGeometry", tray.geometry);
					firstLevelSample.addUserAttributes(
							firstLevelUserAttributes);
				}

				// Now go over the children
				for (int j = 0; j < firstLevelSampleNode.getChildCount(); j++ ) {
	
					// Get the j-th child node
					AbstractNode secondLevelSampleNode =
							(AbstractNode) firstLevelSampleNode.getChildAt(j);
	
					// Get the Sample Descriptor
					SampleDescriptor secondLevelSample =
							(SampleDescriptor) secondLevelSampleNode.getUserObject();
	
					// Make sure we have a Specimen or a Tube
					assert((secondLevelSample.getType().equals("Specimen") ||
	                        secondLevelSample.getType().equals("Tube")));
	
					// Set the openBIS space and experiment identifiers
					Map<String, String> secondLevelOpenBISAttributes = 
							new Hashtable<String, String>();
					secondLevelOpenBISAttributes.put(
							"openBISSpaceIdentifier", 
							metadata.getOpenBISSpaceIdentifier());
					secondLevelOpenBISAttributes.put(
							"openBISExperimentIdentifier", 
							metadata.getOpenBISExerimentIdentifier());
					secondLevelSample.addOpenBISAttributes(
							secondLevelOpenBISAttributes);					
	
					// Now go over the children
					for (int k = 0; k < secondLevelSampleNode.getChildCount(); k++ ) {
	
						// Get the j-th child node
						AbstractNode thirdLevelSampleNode =
								(AbstractNode) secondLevelSampleNode.getChildAt(k);
	
						// A third-level node can contain a Tube or an FCS file
						AbstractDescriptor abstractSample = (AbstractDescriptor)
								thirdLevelSampleNode.getUserObject();
	
						// Make sure we have a Well or a FCSFile
						assert((abstractSample.getType().equals("Well") ||
	                            abstractSample.getType().equals("FCSFile")));
	
						if (abstractSample.getType().equals("Well")	) {
	
							// Cast
							SampleDescriptor thirdLevelSample = (SampleDescriptor)
									thirdLevelSampleNode.getUserObject();
	
							// Set the openBIS space and experiment identifiers
							Map<String, String> thirdLevelOpenBISAttributes = 
									new Hashtable<String, String>();
							thirdLevelOpenBISAttributes.put(
									"openBISSpaceIdentifier", 
									metadata.getOpenBISSpaceIdentifier());
							thirdLevelOpenBISAttributes.put(
									"openBISExperimentIdentifier", 
									metadata.getOpenBISExerimentIdentifier());
							thirdLevelSample.addOpenBISAttributes(
									thirdLevelOpenBISAttributes);					
							
							// And now we set up the associated FCS file
	
							// Get the child (one!): the FCS file
							assert(thirdLevelSampleNode.getChildCount() == 1);
	
							AbstractNode fourthLevelSampleNode = (AbstractNode)
									thirdLevelSampleNode.getChildAt(0);
							DatasetDescriptor fcsFile = (DatasetDescriptor)
									fourthLevelSampleNode.getUserObject();
	
							// Set the openBIS space and experiment identifiers
							Map<String, String> fourthLevelOpenBISAttributes = 
									new Hashtable<String, String>();
							fourthLevelOpenBISAttributes.put(
									"openBISExperimentIdentifier", 
									metadata.getOpenBISExerimentIdentifier());
							fcsFile.addOpenBISAttributes(
									fourthLevelOpenBISAttributes);	
	
						} else {
	
							// Here we have an FCS file
	
							// Cast
							DatasetDescriptor fcsFile = (DatasetDescriptor)
									thirdLevelSampleNode.getUserObject();

							// Set the openBIS space and experiment identifiers
							Map<String, String> fourthLevelOpenBISAttributes = 
									new Hashtable<String, String>();
							fourthLevelOpenBISAttributes.put(
									"openBISExperimentIdentifier", 
									metadata.getOpenBISExerimentIdentifier());
							fcsFile.addOpenBISAttributes(
									fourthLevelOpenBISAttributes);	

						}
					}
				}
			}
		}

		return true;
	}	

	/**
	 * Update metadata and UI. Since the number of widgets changes 
	 * between experiments, we force recreation.
	 */
	public void updateUIElements(ObserverActionParameters params) {

		// Update the currentExperimentIndex property
		currentExperimentIndex = experiments.indexOf(params.node);
		try {
			createUIElements(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		// Create all BDLSRFortessaFCSMetadata objects and initially 
		// assign each folder to the first project
		for (ExperimentNode node : experiments) {
			metadataMappersList.add(
					new BDLSRFortessaFCSMetadata(
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
		if (metadataMappersList.size() < (currentExperimentIndex + 1)) {
			return;
		}
		BDLSRFortessaFCSMetadata metadata = metadataMappersList.get(
				currentExperimentIndex);

		// Keep track of the y index of elements
		int gridy = 0;

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

		// Create a label for the folder
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.weightx = 2;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = gridy++;
		labelFolderName = new JLabel(
				metadata.expNode.getParent().toString());
		labelFolderName.setIcon(new ImageIcon(
				this.getClass().getResource("icons/folder.png")));
		panel.add(labelFolderName, constraints);

		/*
		 *  Experiment name
		 */
		
		String expName = metadata.getExperimentName();
		
		// Create a label for the experiment
		constraints.insets = new Insets(0, 20, 0, 20);
		constraints.gridwidth = 2;
		constraints.weightx = 2;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = gridy++;
		labelExpName = new JLabel(expName);
		labelExpName.setIcon(new ImageIcon(
				this.getClass().getResource("icons/experiment.png")));		
		panel.add(labelExpName, constraints);

		/*
		 * Description label
		 */
		
		// Create a label for the experiment description
		constraints.insets = new Insets(0, 20, 0, 20);
		constraints.gridwidth = 2;
		constraints.weightx = 2;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = gridy++;
		JLabel labelExpDescription = new JLabel("Experiment description");
		labelExpDescription.setHorizontalAlignment(JLabel.CENTER);
		panel.add(labelExpDescription, constraints);

		/*
		 * Description text area
		 */
		constraints.gridwidth = 2;
		constraints.weightx = 2;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = gridy++;
		expDescription = new JTextArea(metadata.getExperiment().description);
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
		 *  Tray geometry label
		 */

		// Create a label for the geometry combo box
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.gridwidth = 2;
		constraints.weightx = 2;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = gridy++;
		JLabel labelGeometry = new JLabel("Plate geometry");
		labelGeometry.setHorizontalAlignment(JLabel.CENTER);
		panel.add(labelGeometry, constraints);
		
		/*
		 *  Tray geometry combo
		 */
		
		// Get the number of plates contained in the Experiment
		Map<String, Tray> trays = metadata.getTraysForExperiment();

		// Add one combo box per geometry
		if (comboGeometryList == null) {
			comboGeometryList = 
					new LinkedHashMap<JComboBox<String>, Tray>();
		}
		comboGeometryList.clear();

		// Add geometry comboboxes (or a simple label if current 
		// experiment has no plates).
		if (trays.size() == 0) {
			
			constraints.insets = new Insets(0, 20, 0, 20);
			constraints.gridwidth = 2;
			constraints.weightx = 2;
			constraints.weighty = 0;
			constraints.gridx = 0;
			constraints.gridy = gridy++;
			panel.add(new JLabel("<html><i>No plates in this experiment."),
					constraints);
			
		} else {
			ArrayList<String> supportedGeometries =
					metadata.supportedTrayGeometries;

			for (String trayName : trays.keySet()) {

				// Create a JComboBox with the supported geometries
				JComboBox<String> c = new JComboBox<String>();
				for (String geometry : supportedGeometries) {

					// Add the geometry
					c.addItem(geometry);

				}

				// Get current Tray
				Tray tray = trays.get(trayName);

				// Select current geometry
				c.setSelectedItem(tray.geometry);

				// When a geometry is selected, update the corresponding
				// experiment in the data model
				c.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand().equals("comboBoxChanged")) {

							@SuppressWarnings("unchecked")
							JComboBox<String> obj = (JComboBox<String>) e
									.getSource();

							// Get tray
							Tray tray = comboGeometryList.get(obj);

							// Set the geometry
							tray.geometry = (String) obj.getSelectedItem();

						}
					}
				});

				// Add the geometry combo box
				constraints.insets = new Insets(0, 20, 0, 20);
				constraints.gridwidth = 1;
				constraints.weightx = 1;
				constraints.weighty = 0;
				constraints.gridx = 0;
				constraints.gridy = gridy;
				panel.add(new JLabel(tray.getName()), constraints);

				// Add the geometry combo box
				constraints.insets = new Insets(0, 20, 0, 20);
				constraints.gridwidth = 1;
				constraints.weightx = 1;
				constraints.weighty = 0;
				constraints.gridx = 1;
				constraints.gridy = gridy;
				panel.add(c, constraints);

				// Increase the y grid coordinate
				gridy++;

				comboGeometryList.put(c, tray);
			}
		}
		
		/*
		 *  Label openBIS projects
		 */

		// Create a label for the openBIS projects
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.gridwidth = 2;
		constraints.weightx = 2;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = gridy++;
		JLabel labelProjects = new JLabel("Target openBIS project");
		labelProjects.setHorizontalAlignment(JLabel.CENTER);
		panel.add(labelProjects, constraints);
		
		/*
		 *  Tray openBIS projects
		 */
		
		// Store the project in a JCombo box
		comboProjectList = new JComboBox<String>();

		for (OpenBISProjectNode s : openBISProjects) {

			// Add the project identifier
			comboProjectList.addItem(
					((OpenBISProjectNode)s).getIdentifier());

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
		constraints.gridwidth = 2;
		constraints.weightx = 2;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = gridy++;
		panel.add(comboProjectList, constraints);

		/*
		 *  Spacer
		 */
		
		// Add a spacer
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = gridy++;
		constraints.weightx = 2.0;
		constraints.weighty = 1.0;
		panel.add(new JLabel(""), constraints);
		
		// Now redraw
		panel.validate();
		panel.repaint();

	}

	/**
	 * Collects and stores data folders for mapping
	 * @return list of openBIS nodes
	 */
	private void storeExperiments() {

		// Reset the dataFolders list
		experiments = new ArrayList<ExperimentNode>();

		// Store the data model
		dataModel = dataViewer.getDataModel();
		
		// We extract all experiments from the data model
		RootNode dataRoot = (RootNode) dataModel.getRoot();
		
		// First level are the Experiment nodes 
		int dataNChildren = dataRoot.getChildCount();

		for (int i = 0; i < dataNChildren; i++) {

			// Get the ExperimentNode
			ExperimentNode expNode =
						(ExperimentNode) dataRoot.getChildAt(i);

			// Store the reference to the ExperimentNode
			experiments.add(expNode);
		}        
	}

	/**
	 * We update the experiment description on the fly while the user 
	 * is typing in the Text Area.
	 */
	protected void updateExpDescription() {

		// Get the active metadata object
		BDLSRFortessaFCSMetadata metadata = metadataMappersList.get(
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
		metadataMappersList = new ArrayList<BDLSRFortessaFCSMetadata>();
	}
}
