package ch.ethz.scu.obit.microscopy.gui.viewers.data;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import ch.ethz.scu.obit.at.gui.viewers.ObserverActionParameters;
import ch.ethz.scu.obit.at.gui.viewers.data.AbstractViewer;
import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.at.gui.viewers.data.model.ExperimentNode;
import ch.ethz.scu.obit.microscopy.gui.viewers.data.model.MicroscopyCompositeFileNode;
import ch.ethz.scu.obit.microscopy.gui.viewers.data.model.MicroscopyFileNode;
import ch.ethz.scu.obit.microscopy.gui.viewers.data.model.MicroscopyFileSeriesNode;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.Experiment;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.MicroscopyCompositeFile;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.MicroscopyFile;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.MicroscopyFileSeries;

/**
 * Simple graphical viewer for the MicroscopyProcessor.
 * @author Aaron Ponti
 */
public final class MicroscopyViewer extends AbstractViewer implements TreeWillExpandListener {

	MicroscopyProcessor microscopyProcessor;

	/**
	 * Constructor
	 */
	public MicroscopyViewer() {
		
		// Listen for when a node is about to be opened (for lazy loading)
		tree.addTreeWillExpandListener(this);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Nothing to do here.
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
		if (node == lastSelectedNode) {
			return;
		}
		lastSelectedNode = node;

		// Flag: broadcast project change
		boolean broadcastMicroscopyFileChange = false;
		
		// Keep track of the selected microscopy file node
		AbstractNode fileNode = null;
		
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
			
			// Store reference to the node
			fileNode = (AbstractNode) node;
				
			// A project change must be broadcast
			broadcastMicroscopyFileChange = true;

		} else if (className.equals("MicroscopyCompositeFile")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((MicroscopyProcessor.MicroscopyCompositeFile) 
							nodeInfo).getAttributes());
			
			// Store reference to the node
			fileNode = (AbstractNode) node;
				
			// A project change must be broadcast
			broadcastMicroscopyFileChange = true;

		} else if (className.equals("MicroscopyFileSeries")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((MicroscopyProcessor.MicroscopyFileSeries) 
							nodeInfo).getAttributes());
			
			// Which file is it?
			fileNode = (AbstractNode)
					getParentNodeByName(node, "MicroscopyFile");
			
			// A project change must be broadcast
			broadcastMicroscopyFileChange = true;
			
		} else {
			clearMetadataTable();
		}

        // Get the Experiment name
        AbstractNode expNode = getParentNodeByName(node, "Experiment");
        if (expNode != null) {
        		
            // Notify the editor to update its view
			synchronized (this) {
				setChanged();
				notifyObservers(new ObserverActionParameters(
						ObserverActionParameters.Action.EXPERIMENT_CHANGED,
						expNode));
			}
        }
        
        // Should the file editing elements be updated?
        if (broadcastMicroscopyFileChange) {
        	
        	// Notify the change of file
			synchronized (this) {
				setChanged();
				notifyObservers(new ObserverActionParameters(
						ObserverActionParameters.Action.FILE_CHANGED,
						fileNode));
			}

        }
       
	}

	/**
	 * Scans the datamover incoming directory for datasets to be processed.
	 * At the end of scanning, the function MUST set isReady to true.
	 */
	@Override
	public boolean parse(File folder) {

		// Process the file or folder
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

			// Clear the invalid dataset table
			clearInvalidDatasetsTable();

			// Update the invalid dataset table 
			updateInvalidDatasetTable(
					microscopyProcessor.validator.invalidFilesOrFolders);
			
			// Clear the metadata table
			clearMetadataTable();
			
			// Set the isReady flag to false
			isReady = false;
			
			// Return failure
			return false;
		}

		// We will append the experiment nodes directly to the root node
		createNodes(rootNode, microscopyProcessor.folderDescriptor);
		
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
		MicroscopyCompositeFileNode microscopyCompositeFileNode;

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
			
			// Add its composite files
			for (String microscopyCompositeKey: e.microscopyCompositeFiles.keySet()) {

				// Get the miroscopy file descriptor
				MicroscopyCompositeFile microscopyCompositeFile = 
						e.microscopyCompositeFiles.get(microscopyCompositeKey);
				
				// Add the MicroscopyCompositeFile
				microscopyCompositeFileNode = new MicroscopyCompositeFileNode(microscopyCompositeFile);
				experimentNode.add(microscopyCompositeFileNode);
			
			}

		}
	}

	/**
	 * Update the invalid dataset table.
	 * @param invalidFilesOrFolders Map<File, String> of invalid folders from
	 * the MicroscopyProcessor
	 */
	protected void updateInvalidDatasetTable(Map<File, String> invalidFilesOrFolders) {

		// Get the table model
		DefaultTableModel model = 
				(DefaultTableModel) invalidDatasetsTable.getModel();
		for (File file : invalidFilesOrFolders.keySet()) {
			String filePath;
			try {
				filePath = file.getCanonicalPath();
				// Build a relative path
				int indx = filePath.indexOf(userName);
				if (indx != -1) {
					filePath = filePath.substring(indx);
				}
			} catch (IOException e) {
				filePath = "Unknown";
			}
			model.addRow(new Object[] {filePath, invalidFilesOrFolders.get(file)});
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
	 * @param abstractNode Node to query for children.
	 */
	private synchronized void loadLazyChildren(AbstractNode abstractNode) {
		
		// Get the user object stored in the node
		Object obj = abstractNode.getUserObject();
		
		// Which openBIS object did we get?
		String className = obj.getClass().getSimpleName();

		// Proceed with the loading
		if (className.equals("MicroscopyFile")) {
			
			MicroscopyFileNode node = (MicroscopyFileNode) abstractNode; 
			if (node.isLoaded()) {
				// This node is already expanded.
				return;
			}

			// Disable Scan button
			scanButton.setEnabled(false);
			
            // Notify that the node is being expanded
			synchronized (this) {
				setChanged();
				notifyObservers(new ObserverActionParameters(
						ObserverActionParameters.Action.ABOUT_TO_SCAN_INCREMENTALLY,
						null));
			}

			// Inform
			outputPane.log("Scanning metadata from " + node.toString());
			
			// Get the descriptor
			MicroscopyFile microscopyFile = (MicroscopyFile) obj;
			
			// Scan the series in background

			// Then define and start the worker
			class Worker extends SwingWorker<Boolean, Void> {

				final private MicroscopyFile m;
				final private MicroscopyFileNode n;

				/**
				 * Constructor
				 * 
				 * @param m MicrosopyFile reference
				 # @param n MicroscopyFileNode reference.
				 */
				public Worker(MicroscopyFile m, MicroscopyFileNode n) {
					this.m = m;
					this.n = n;
				}

				@Override
				public Boolean doInBackground() {

					// We parse the user folder: the actual processing is done
					// by the processor.
					return (m.scanForSeries());

				}

				@Override
				public void done() {

					boolean status = false;

					// Retrieve the status
					try {
						status = get();
					} catch (InterruptedException | ExecutionException e) {
						status = false;
					}

					if (!status) {
						// If scanning failed, we update the invalid dataset
						// table and return failure

						// Clear the invalid dataset table
						clearInvalidDatasetsTable();

						// Update the invalid dataset table
						updateInvalidDatasetTable(
								microscopyProcessor.validator.invalidFilesOrFolders);

						// Clear the metadata table
						clearMetadataTable();

						// Set the isReady flag to false
						isReady = false;

						// Inform
						outputPane.err("Scanning metadata from "
								+ n.toString() + " failed!");
						return;
					}

					// Get the tree model for extension
					DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
					
					// Add all series to the tree
					for (String key : m.series.keySet()) {

						MicroscopyFileSeries s = m.series.get(key);
						model.insertNodeInto(new MicroscopyFileSeriesNode(s),
								n, n.getChildCount());
					}

					// Mark the node as loaded
					n.setLoaded();

					// Re-enable Scan button
					scanButton.setEnabled(true);

		            // Notify that the node has finished expanding
					synchronized (this) {
						setChanged();
						notifyObservers(new ObserverActionParameters(
								ObserverActionParameters.Action.INCREMENTAL_SCAN_COMPLETE,
								null));
					}

					// Inform
					outputPane.log("Scanning metadata from " + n.toString()
							+ " completed.");
				}

			}
			;

	        // Run the worker!
	        (new Worker(microscopyFile, node)).execute();
							
		} else {
			
			// We do nothing for other types
		}
		
	}

}
