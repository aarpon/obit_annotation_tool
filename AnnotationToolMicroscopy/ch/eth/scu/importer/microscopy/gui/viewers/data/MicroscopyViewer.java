package ch.eth.scu.importer.microscopy.gui.viewers.data;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.at.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.at.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.microscopy.gui.viewers.data.model.MicroscopyFileNode;
import ch.eth.scu.importer.microscopy.processors.MicroscopyProcessor;
import ch.eth.scu.importer.microscopy.processors.MicroscopyProcessor.Experiment;
import ch.eth.scu.importer.microscopy.processors.MicroscopyProcessor.MicroscopyFile;

/**
 * Simple graphical viewer for the MicroscopyProcessor.
 * @author Aaron Ponti
 */
public class MicroscopyViewer extends AbstractViewer {

	/**
	 * Constructor
	 */
	public MicroscopyViewer() {
		
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

		        		// If the double-clicked on an microscopy file, scan it
		        		// and update the metadata view
		        		String className = nodeInfo.getClass().getSimpleName();
		        		if (className.equals("MicroscopyFile") && 
		        				(e.getClickCount() == 2)) {
		        			clearMetadataTable();
		        			addAttributesToMetadataTable(
		        					((MicroscopyProcessor.MicroscopyFile) 
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
		} else if (className.equals("MicroscopyFile")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((MicroscopyProcessor.MicroscopyFile) 
							nodeInfo).getAttributes());
		} else {
			clearMetadataTable();
		}

        // Get the folder name
        AbstractNode folderNode = getParentNodeByName(node, "Experiment");
        if (folderNode != null && folderNode != lastSelectedExperiment) {
        		
        		// Update the lastSelectedFolder property
        		lastSelectedExperiment = folderNode;
        		
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
		MicroscopyProcessor microscopyProcessor;
		try {
			microscopyProcessor = new MicroscopyProcessor(
					folder.getCanonicalPath());
		} catch (IOException e) {
			outputPane.err("Could not parse the folder " + folder + "!");
			return false;
		}

		// We parse. If parsing fails, we just return (the dataset is invalid).
		if (!microscopyProcessor.parse()) {
			outputPane.err("Could not parse the folder " + folder + "!");
			microscopyProcessor = null;
			return false;
		}

		// Make sure we have a valid dataset
		if (!microscopyProcessor.validator.isValid) {
			DefaultTableModel model = 
					(DefaultTableModel) invalidDatasetsTable.getModel();
			for (File file : microscopyProcessor.validator.invalidFilesOrFolders.keySet()) {
				String filePath;
				try {
					filePath = file.getCanonicalPath();
					// Build a relative path
					int indx = filePath.indexOf(userName);
					if (indx != -1) {
						filePath = filePath.substring(indx);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					filePath = "Unknown";
				}
				model.addRow(new Object[] {filePath,
						microscopyProcessor.validator.invalidFilesOrFolders.get(file)});
			}
			return false;
		}

		// We will append the experiment nodes directly to the root node
		createNodes((RootNode)rootNode, microscopyProcessor.folderDescriptor);
		
		// Inform the user
		outputPane.log("Successfully processed folder \"" + 
				microscopyProcessor.getFolder().getName() + "\"");
		return true;
	}

	/**
	 * Create the nodes for the tree
	 * @param rootNode Root node for the tree
	 * @param folderDescriptor A folder descriptor object.
	 */
	protected void createNodes(DefaultMutableTreeNode rootNode,
			MicroscopyProcessor.UserFolder folderDescriptor) {

		ExperimentNode experimentNode;
		MicroscopyFileNode microscopyFileNode;

		for (String expKey : folderDescriptor.experiments.keySet()) {

			// Get the ExperimentDescriptor
			Experiment e = folderDescriptor.experiments.get(expKey);

			// Add the experiment
			experimentNode = new ExperimentNode(e);
			rootNode.add(experimentNode);

			// Add its images
			for (String microscopyKey: e.microscopyFiles.keySet()) {

				// Get the miroscopy file descriptor
				MicroscopyFile microscopyFile = 
						e.microscopyFiles.get(microscopyKey);
				
				// Add the MicroscopyFile
				microscopyFileNode = new MicroscopyFileNode(microscopyFile);
				experimentNode.add(microscopyFileNode);
			}
		}
	}

}
