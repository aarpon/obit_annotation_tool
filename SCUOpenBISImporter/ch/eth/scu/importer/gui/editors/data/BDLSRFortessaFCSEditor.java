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

import ch.eth.scu.importer.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.gui.viewers.data.model.FolderNode;
import ch.eth.scu.importer.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.gui.viewers.openbis.OpenBISSpaceViewer;
import ch.eth.scu.importer.gui.viewers.openbis.model.AbstractOpenBISNode;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.Experiment;
import ch.eth.scu.importer.processor.model.AbstractDescriptor;
import ch.eth.scu.importer.processor.model.DatasetDescriptor;
import ch.eth.scu.importer.processor.model.SampleDescriptor;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 */
public class BDLSRFortessaFCSEditor extends AbstractEditor {

	protected List<ExperimentNode> experiments = new ArrayList<ExperimentNode>();

	/**
	 * Constructor
	 */
	public BDLSRFortessaFCSEditor(AbstractViewer dataViewer, 
			OpenBISSpaceViewer openBISViewer) {

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
	 * Renders all widgets on the panel
	 * @throws Exception if some openBIS identifiers cannot be computed
	 */
	public void render() throws Exception {

		// Make sure both viewers have completed their models
		if (dataViewer.isReady() == false || openBISViewer.isReady() == false) {
			return;
		}

		// If we are repainting this panel, let's make sure we delete the
		// child components already displayed.
		if (panel.getComponentCount() > 0) {
			panel.removeAll();
			panel.revalidate(); 
		}

		// Constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		// We get the models
		TreeModel dataModel = dataViewer.getDataModel();
		TreeModel openBISModel = openBISViewer.getDataModel();

		// We extract all projects from the openBIS model and create a list
		// with which we will then create JComboBox associated to each project
		// from the data model
		List<AbstractOpenBISNode> projects = new ArrayList<AbstractOpenBISNode>();

		AbstractOpenBISNode openBISRoot = 
				(AbstractOpenBISNode) openBISModel.getRoot();

		// First level are spaces (which we do not need)
		int openBISNChildren = openBISRoot.getChildCount();
		if (openBISNChildren == 0) {
			return;
		}

		// Since there are children, we can create the UI elements
		for (int i = 0; i < openBISNChildren; i++) {

			// Get the Space
			AbstractOpenBISNode openBISSpaceNode = 
					(AbstractOpenBISNode) openBISRoot.getChildAt(i);

			// Go over the child Projects
			int n = openBISSpaceNode.getChildCount();

			for (int j = 0; j < n; j++) {

				// Get the node
				AbstractOpenBISNode openBISProjectNode = 
						(AbstractOpenBISNode) openBISSpaceNode.getChildAt(j);

				// Add it to the list
				projects.add(openBISProjectNode);

			}
		}

		// We extract all experiments from the data model
		RootNode dataRoot = (RootNode) dataModel.getRoot();

		// Keep track of the Y position in the layout
		int yPos = 0;

		// First level are the XML files 
		int dataNChildren = dataRoot.getChildCount();

		for (int i = 0; i < dataNChildren; i++) {

			// Get the FolderNode
			FolderNode folderNode = (FolderNode) dataRoot.getChildAt(i);

			// Create a label for the XML file
			constraints.insets = new Insets(10, 10, 10, 10);
			constraints.weightx = 1;
			constraints.weighty = 0;
			constraints.gridx = 0;
			constraints.gridy = yPos++;
			panel.add(new JLabel(folderNode.toString()), constraints);

			// Now go over the children (Experiments)
			int nChildren = folderNode.getChildCount();

			for (int j = 0; j < nChildren; j++) {

				// Get the Experiment node
				ExperimentNode dataExpNode = 
						(ExperimentNode) folderNode.getChildAt(j);

				// Store the reference to the ExperimentNode
				experiments.add(dataExpNode);

				// Create a label for the experiment
				constraints.insets = new Insets(0, 20, 0, 20);
				constraints.weightx = 1;
				constraints.weighty = 0;
				constraints.gridx = 0;
				constraints.gridy = yPos++;
				panel.add(new JLabel(dataExpNode.toString()), constraints);

				// Pair all the info together for the Combo box

				// Add a JComboBox for the Pair objects
				JComboBox projCombo = new JComboBox();
				for (AbstractOpenBISNode s : projects) {

					// Add the Pair object
					projCombo.addItem(new Pair(dataExpNode, s));

				}
				projCombo.setSelectedIndex(0);

				// By default, set the first project to current 
				// ExperimentDescriptor in the data model
				performMapping(dataExpNode, projects.get(0));

				// When a project is selected, update the corresponding 
				// ExperimentDescriptor in the data model 
				projCombo.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand().equals("comboBoxChanged")) {

							// Get the Pair object
							Pair pair = (Pair) 
									((JComboBox)(e.getSource())).getSelectedItem();

							// Now perform the full mapping
							performMapping((ExperimentNode) pair.expNode,
									(AbstractOpenBISNode) pair.projNode);

						}
					}
				});
				constraints.insets = new Insets(0, 20, 0, 20);
				constraints.weightx = 1;
				constraints.weighty = 0;
				constraints.gridx = 0;
				constraints.gridy = yPos++;
				panel.add(projCombo, constraints);

			}
		}

		// Add a spacer
		constraints.gridx = 0;
		constraints.gridy = yPos;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		panel.add(new JLabel(""), constraints);

		// In case this was called when then window was already visible (i.e.
		// if the login failed the first time and this panel was drawn without
		// children)
		panel.revalidate();

	}

	/**
	 * Use the pair (ExperimentNode, ProjectNode) to update all data Descriptors
	 * with the correct openBIS information 
	 */
	public void performMapping(ExperimentNode expNode, AbstractOpenBISNode projNode) {

		// The parent of the ExperimentDescriptor is the FolderDescriptor, 
		// which does not need to be updated.

		// We first start by updating the Experiment descriptor itself
		Experiment expDescr = (Experiment) expNode.getUserObject();

		// Set the openBIS project identifier
		expDescr.setOpenBISProjectIdentifier(projNode.getIdentifier());

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
					firstLevelSample.getType().equals("Specimen")) == true);

			// Set the Experiment identifier
			firstLevelSample.setOpenBISExperimentIdentifier(
					expDescr.getOpenBISIdentifier());

			// Set the Sample identifier to "" since immediate children
			// of the Experiment do not have a parent sample container
			firstLevelSample.setOpenBISContainerSampleIdentifier("");

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
						secondLevelSample.getType().equals("Tube")) == true);

				// Set the container sample identifier
				secondLevelSample.setOpenBISContainerSampleIdentifier(
						firstLevelSample.getOpenBISIdentifier());

				// Set the experiment identifier 
				secondLevelSample.setOpenBISExperimentIdentifier(
						expDescr.getOpenBISIdentifier());

				// Now go over the children
				for (int k = 0; k < secondLevelSampleNode.getChildCount(); k++ ) {

					// Get the j-th child node
					AbstractNode thirdLevelSampleNode =
							(AbstractNode) secondLevelSampleNode.getChildAt(k);

					// A third-level node can contain a Tube or an FCS file
					AbstractDescriptor abstractSample = (AbstractDescriptor)
							thirdLevelSampleNode.getUserObject();

					// Make sure we have a Specimen or a Tube
					assert((abstractSample.getType().equals("Tube") ||
							abstractSample.getType().equals("FCSFile"))
							== true);

					if (abstractSample.getType().equals("Tube")) {

						// Cast
						SampleDescriptor thirdLevelSample = (SampleDescriptor)
								thirdLevelSampleNode.getUserObject();

						// Set the container sample identifier
						thirdLevelSample.setOpenBISContainerSampleIdentifier(
								secondLevelSample.getOpenBISIdentifier());

						// Set the experiment identifier 
						thirdLevelSample.setOpenBISExperimentIdentifier(
								expDescr.getOpenBISIdentifier());

						// And now we set up the associated FCS file

						// Get the child (one!): the FCS file
						assert(thirdLevelSampleNode.getChildCount() == 1);

						AbstractNode fourthLevelSampleNode = (AbstractNode)
								thirdLevelSampleNode.getChildAt(0);
						DatasetDescriptor fcsFile = (DatasetDescriptor)
								fourthLevelSampleNode.getUserObject();

						// Set the experiment identifier
						fcsFile.setOpenBISExperimentIdentifier(
								expDescr.getOpenBISIdentifier());

						// Set the sample
						fcsFile.setOpenBISSampleIdentifier(
								thirdLevelSample.getOpenBISIdentifier());

					} else {

						// Here we have an FCS file

						// Cast
						DatasetDescriptor fcsFile = (DatasetDescriptor)
								thirdLevelSampleNode.getUserObject();

						// Set the experiment identifier
						fcsFile.setOpenBISExperimentIdentifier(
								expDescr.getOpenBISIdentifier());

						// Set the sample
						fcsFile.setOpenBISSampleIdentifier(
								secondLevelSample.getOpenBISIdentifier());
					}
				}
			}
		}

	}

	/**
	 * Pair of data and openBIS information to allow mapping following 
	 * user choice.
	 * @author Aaron Ponti
	 */
	protected class Pair {
		protected ExperimentNode expNode;
		protected AbstractOpenBISNode projNode;

		protected Pair(ExperimentNode expNode, AbstractOpenBISNode projNode) {
			this.expNode = expNode;
			this.projNode = projNode;
		}

		public String toString() {
			return projNode.getIdentifier();
		}
	}
}
