package ch.eth.scu.importer.microscopy.gui.viewers.data;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.at.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.at.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.microscopy.gui.viewers.data.model.MicroscopyFileNode;
import ch.eth.scu.importer.microscopy.gui.viewers.data.model.MicroscopyFileSeriesNode;
import ch.eth.scu.importer.microscopy.processors.MicroscopyProcessor;
import ch.eth.scu.importer.microscopy.processors.MicroscopyProcessor.Experiment;
import ch.eth.scu.importer.microscopy.processors.MicroscopyProcessor.MicroscopyFile;
import ch.eth.scu.importer.microscopy.processors.MicroscopyProcessor.MicroscopyFileSeries;

/**
 * Simple graphical viewer for the MicroscopyProcessor.
 * @author Aaron Ponti
 */
public class MicroscopyViewer extends AbstractViewer implements TreeWillExpandListener {

	/**
	 * Constructor
	 */
	public MicroscopyViewer() {
		
		// Listen for when a node is about to be opened (for lazy loading)
		tree.addTreeWillExpandListener((TreeWillExpandListener) this);

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
		} else if (className.equals("MicroscopyFileSeries")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((MicroscopyProcessor.MicroscopyFileSeries) 
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

			// Add its files
			for (String microscopyKey: e.microscopyFiles.keySet()) {

				// Get the miroscopy file descriptor
				MicroscopyFile microscopyFile = 
						e.microscopyFiles.get(microscopyKey);
				
				// Add the MicroscopyFile
				microscopyFileNode = new MicroscopyFileNode(microscopyFile);
				experimentNode.add(microscopyFileNode);

				// If files have been expanded already and there are series, 
				// add them too.
				for (String key: microscopyFile.series.keySet()) {

					// Get the miroscopy file descriptor
					MicroscopyFileSeries s = microscopyFile.series.get(key);
					
					// Add the MicroscopyFileSeries
					microscopyFileNode.add(new MicroscopyFileSeriesNode(s));
				}
			
			}
			
		}
	}

	/**
	 * Called when a node in the Tree is about to expand.
	 * @param event A TreeExpansionEvent.
	 * Required by TreeWillExpandListener interface.
	 */	
	@Override
	public void treeWillExpand(TreeExpansionEvent event)
			throws ExpandVetoException {
        TreePath path = event.getPath();
        loadLazyChildren(
        		(AbstractNode) path.getLastPathComponent());
	}

	/**
	 * Called when a node in the Tree is about to collapse.
	 * @param event A TreeExpansionEvent.
	 * Required by TreeWillExpandListener interface.
	 */
	@Override
	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException {
		// We do nothing
	}
	
	/**
	 * Load the childen of the specified node if needed
	 * @param abstractNode
	 */
	private void loadLazyChildren(AbstractNode abstractNode) {
		
		// Get the user object stored in the node
		Object obj = abstractNode.getUserObject();
		
		// Which openBIS object did we get?
		String className = obj.getClass().getSimpleName();

		// Proceed with the loading
		if (className.equals("MicroscopyFile")) {
			
			MicroscopyFileNode node = (MicroscopyFileNode) abstractNode; 
			if (node.isLoaded() == true) {
				outputPane.log("This node is already expanded.");
				return;
			}
			
			// Get the descriptor
			MicroscopyFile microscopyFile = (MicroscopyFile) obj;
			
			// Scan for series
			microscopyFile.scanForSeries();
			
			// Add all series to the tree
			for (String key : microscopyFile.series.keySet()) {
			
				MicroscopyFileSeries s = microscopyFile.series.get(key);
				node.add(new MicroscopyFileSeriesNode(s));
			}
			
			// Mark the node as loaded
			node.setLoaded();
			
			// Inform
			outputPane.log("Scanning metadata from " + node.toString() +
					" completed.");
							
		} else {
			
			// We do nothing for other types
		}
		
	}

}
