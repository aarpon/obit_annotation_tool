package ch.eth.scu.importer.ui_elements.nikonnd2.gui.viewers.data;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.processors.nikonnd2.NikonProcessor;
import ch.eth.scu.importer.processors.nikonnd2.NikonProcessor.Experiment;
import ch.eth.scu.importer.processors.nikonnd2.NikonProcessor.ND2File;
import ch.eth.scu.importer.ui_elements.nikonnd2.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.ui_elements.nikonnd2.gui.viewers.data.model.ND2FileNode;

/**
 * Simple graphical viewer for the NikonProcessor.
 * @author Aaron Ponti
 */
public class NikonViewer extends AbstractViewer {

	/**
	 * Constructor
	 */
	public NikonViewer() {
		
		// Add a mouse listener to the DataViewerTree
		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				TreePath selPath = 
						tree.getPathForLocation(e.getX(), e.getY());
				if (selPath != null) {
					AbstractNode node = 
							(AbstractNode) selPath.getLastPathComponent();
					if (node == null) {
						return;
					}

					// Get the node object
		        		Object nodeInfo = node.getUserObject();

		        		// If the double-clicked on an ND2 file, scan it
		        		String className = nodeInfo.getClass().getSimpleName();
		        		if (className.equals("ND2File") && 
		        				(e.getClickCount() == 2)) {
		        			clearMetadataTable();
		        			addAttributesToMetadataTable(
		        					((NikonProcessor.ND2File) 
		        							nodeInfo).scanFileAndGetAttributes());
		        		}
		         }
		     }
		 };
		 tree.addMouseListener(ml);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
		// Get selected node
		AbstractNode node = (AbstractNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		
		// The valuedChanged() method is called twice when the a node is
		// chosen in the tree. Workaround: do not process the same node 
		// twice in a row  
		if (node.toString().equals(lastSelectedNode)) {
			return;
		}
		lastSelectedNode = node.toString();
		
		// Get the node object
		Object nodeInfo = node.getUserObject();

		// Print the attributes
		String className = nodeInfo.getClass().getSimpleName();
		if (className.equals("Experiment")) {
			clearMetadataTable();
		} else if (className.equals("ND2File")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((NikonProcessor.ND2File) 
							nodeInfo).getAttributes());
		} else {
			clearMetadataTable();
		}

        // Get the folder name
        AbstractNode folderNode = getParentNodeByName(node, "Experiment");
        if (folderNode != null && folderNode != lastSelectedFolder) {
        		
        		// Update the lastSelectedFolder property
        		lastSelectedFolder = folderNode;
        		
            // Notify the editor to update its view
            setChanged();
            notifyObservers(new ObserverActionParameters(
                ObserverActionParameters.Action.EXPERIMENT_CHANGED, folderNode));
        }
	}

	/**
	 * Scans the datamover incoming directory for datasets to be processed.
	 * At the end of scanning, the function MUST set isReady to true.
	 */
	@Override
	public boolean parse(File folder) {

		// Process the file or folder
		NikonProcessor nikonprocessor;
		try {
			nikonprocessor = new NikonProcessor(
					folder.getCanonicalPath());
		} catch (IOException e) {
			outputPane.err("Could not parse the folder " + folder + "!");
			return false;
		}

		// We parse. If parsing fails, we just return (the dataset is invalid).
		if (!nikonprocessor.parse()) {
			outputPane.err("Could not parse the folder " + folder + "!");
			nikonprocessor = null;
			return false;
		}

		// If the subfolder is already annotated, we skip it (but still
		// we return success)
		if (nikonprocessor.validator.isAnnotated) {
			outputPane.log("Dataset \"" +
					nikonprocessor.getFolder().getName() + 
					"\" is annotated.");
			return true;
		}

		// Make sure we have a valid dataset
		if (!nikonprocessor.validator.isValid) {
			DefaultTableModel model = 
					(DefaultTableModel) invalidDatasetsTable.getModel();
			int nError = 0;
			StringBuilder err = new StringBuilder("");
			for (String errorString : nikonprocessor.validator.errorMessages) {
				nError++;
				err.append("(").append(nError).append(") ").append(errorString);
			}
			model.addRow(new Object[] {folder.getName(), err});
			outputPane.err("Dataset \"" + 
					nikonprocessor.getFolder().getName() + 
					"\" failed validation. Please fix or remove.");
			return false;
		}

		// We will append the experiment nodes directly to the root node
		createNodes(rootNode, nikonprocessor.experiments);

		// Inform the user
		outputPane.log("Successfully processed folder \"" + 
				nikonprocessor.getFolder().getName() + "\"");
		return true;
	}

	/**
	 * Create the nodes for the tree
	 * @param rootNode Root node for the tree
	 * @param folderDescriptor A folder descriptor object.
	 */
	protected void createNodes(DefaultMutableTreeNode rootNode,
			Map<String, Experiment> experiments) {

		ExperimentNode experimentNode;
		ND2FileNode nd2FileNode;

		for (String expKey : experiments.keySet()) {

			// Get the ExperimentDescriptor
			Experiment e = experiments.get(expKey);

			// Add the experiment
			experimentNode = new ExperimentNode(e);
			rootNode.add(experimentNode);

			// Add its images
			for (String nd2Key: e.nd2Files.keySet()) {

				// Get the ND2 file descriptor
				ND2File nd2File = e.nd2Files.get(nd2Key);
				
				// Add the ND2File
				nd2FileNode = new ND2FileNode(nd2File);
				experimentNode.add(nd2FileNode);
			}
		}
	}

}
