package ch.eth.scu.importer.gui.panels.editors;

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

import ch.eth.scu.importer.gui.components.viewers.ExperimentNode;
import ch.eth.scu.importer.gui.components.viewers.RootNode;
import ch.eth.scu.importer.gui.components.viewers.XMLFileNode;
import ch.eth.scu.importer.gui.panels.openbis.OpenBISSpaceViewer;
import ch.eth.scu.importer.gui.panels.openbis.OpenBISSpaceViewer.CustomOpenBISNode;
import ch.eth.scu.importer.gui.panels.viewers.AbstractViewer;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.ExperimentDescriptor;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 */
public class BDLSRFortessaEditor extends AbstractEditor {

	protected List<ExperimentNode> experiments = new ArrayList<ExperimentNode>();
	
	/**
	 * Constructor
	 */
	public BDLSRFortessaEditor(AbstractViewer dataViewer, 
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
	 */
	public void render() {

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
		List<CustomOpenBISNode> projects = new ArrayList<CustomOpenBISNode>();
		
		CustomOpenBISNode openBISRoot = 
				(CustomOpenBISNode) openBISModel.getRoot();

		// First level are spaces (which we do not need)
		int openBISNChildren = openBISRoot.getChildCount();
		
		for (int i = 0; i < openBISNChildren; i++) {
			
			// Get the Space
			CustomOpenBISNode openBISSpaceNode = 
					(CustomOpenBISNode) openBISRoot.getChildAt(i);
			
			// Go over the child Projects
			int n = openBISSpaceNode.getChildCount();
			
			for (int j = 0; j < n; j++) {
				
				// Get the node
				CustomOpenBISNode openBISProjectNode = 
						(CustomOpenBISNode) openBISSpaceNode.getChildAt(j);
				
				// Add it to the list
				projects.add(openBISProjectNode);

			}
		}
		
		// Extract the default project code and identifier to associate to the
		// ExperimentDescriptors in the data model
		String defaultProjectCode =
				((CustomOpenBISNode)(projects.get(0))).getCode();
		String defaultProjectIdentifier = 
				((CustomOpenBISNode)(projects.get(0))).getIdentifier();

		// We extract all experiments from the data model
		RootNode dataRoot = (RootNode) dataModel.getRoot();

		// Keep track of the Y position in the layout
		int yPos = 0;
		
		// First level are the XML files 
		int dataNChildren = dataRoot.getChildCount();
		
		for (int i = 0; i < dataNChildren; i++) {
			
			// Get the XMLNode
			XMLFileNode dataXmlNode = (XMLFileNode) dataRoot.getChildAt(i);
			
			// Create a label for the XML file
			constraints.insets = new Insets(10, 10, 10, 10);
			constraints.weightx = 1;
			constraints.weighty = 0;
			constraints.gridx = 0;
			constraints.gridy = yPos++;
			panel.add(new JLabel(dataXmlNode.toString()), constraints);
			
			// Now go over the children (Experiments)
			int n = dataXmlNode.getChildCount();
			
			for (int j = 0; j < n; j++) {
				
				// Get the Experiment node
				ExperimentNode dataExpNode = 
						(ExperimentNode) dataXmlNode.getChildAt(j);
				
				// Store the reference to the ExperimentNode
				experiments.add(dataExpNode);
				
				// Create a label for the experiment
				constraints.insets = new Insets(0, 20, 0, 20);
				constraints.weightx = 1;
				constraints.weighty = 0;
				constraints.gridx = 0;
				constraints.gridy = yPos++;
				panel.add(new JLabel(dataExpNode.toString()), constraints);
				
				// Glue all the info together for the Combo box
				
				// Add a JComboBox for the Glue objects
				JComboBox projCombo = new JComboBox();
				for (CustomOpenBISNode s : projects) {
					projCombo.addItem(new Glue(dataXmlNode, dataExpNode, s));
				}
				projCombo.setSelectedIndex(0);
				
				// By default, set the first project to current 
				// ExperimentDescriptor in the data model
				ExperimentDescriptor expDescr =
						((ExperimentDescriptor)dataExpNode.getUserObject());
				expDescr.setOpenBISCode(defaultProjectCode);
				expDescr.setOpenBISIdentifier(defaultProjectIdentifier);
				
				// When a project is selected, update the corresponding 
				// ExperimentDescriptor in the data model 
				projCombo.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		        		if (e.getActionCommand().equals("comboBoxChanged")) {
		        			
		        			// Get the Glue object
		        			Glue glue = (Glue) 
		        					((JComboBox)(e.getSource())).getSelectedItem();

		        			// Get the data experiment descriptor
		        			ExperimentDescriptor expDescr =
		        					((ExperimentDescriptor)glue.expNode.getUserObject());
		        			
		        			// Get the openBIS project node
		        			CustomOpenBISNode projNode = 
		        					((CustomOpenBISNode)glue.projNode);
		        			
		        			// Now fill the ExperimentDescriptor with the 
		        			// information from the OpenBIS Project node
		        			expDescr.setOpenBISCode(
		        					projNode.getCode());
		        			expDescr.setOpenBISIdentifier(
		        					projNode.getIdentifier());
		        					
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
	 * Glue class to combine all data and openBIS information to allow mapping
	 * following user choice.
	 * @author Aaron Ponti
	 *
	 */
	protected class Glue {
		protected XMLFileNode xmlNode;
		protected ExperimentNode expNode;
		protected CustomOpenBISNode projNode;
		
		protected Glue(XMLFileNode xmlNode, ExperimentNode expNode,
				CustomOpenBISNode projNode) {
			this.xmlNode = xmlNode;
			this.expNode = expNode;
			this.projNode = projNode;
		}
		
		public String toString() {
			return projNode.getIdentifier();
		}
	}
}
