package ch.eth.scu.importer.ui_elements.leicasp5.gui.viewers.data;

import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.processors.leicasp5.LeicaLifProcessor;
import ch.eth.scu.importer.processors.leicasp5.LeicaLifProcessor.ImageDescriptor;
import ch.eth.scu.importer.processors.leicasp5.LeicaLifProcessor.SubImageDescriptor;
import ch.eth.scu.importer.processors.model.RootDescriptor;
import ch.eth.scu.importer.ui_elements.leicasp5.gui.viewers.data.model.ImageNode;
import ch.eth.scu.importer.ui_elements.leicasp5.gui.viewers.data.model.SubImageNode;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.RootNode;

import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

/**
 * Simple graphical viewer for the BDDIVAXMLProcessor
 * @author Aaron Ponti
 */
public class LeicaSP5Viewer extends AbstractViewer
	implements ActionListener, TreeSelectionListener {

	// The valueChanged() method is fired twice when selection is changed in 
	// a JTree, so we keep track of the last processed node to avoid processing
	// the same node twice in a row.
	private String lastSelectedNode;
	
	/**
	 * Constructor
	 */
	public LeicaSP5Viewer() {
		
		// Call the AbstractViewer's constructor (to create the panel)
		super();
	}

	/**
	 *  Parse the selected LIF file. 
	 */
	public boolean parse(File file) {

		// Process the file
		LeicaLifProcessor leicalifprocessor;
		try {
			leicalifprocessor = new LeicaLifProcessor(file.getCanonicalPath());
		} catch (IOException e) {
			System.err.println("Invalid file! (TODO: Use panel)");
			leicalifprocessor = null;
			return false;
		}
		if (!leicalifprocessor.parse()) {
			System.err.println("Could not parse file! (TODO: Use panel)");
			leicalifprocessor = null;
			return false;
		}

		// Add the processor as new child of root
		ImageNode lifImageNode = new ImageNode(leicalifprocessor.image);
		rootNode.add(lifImageNode);
		
		createNodes(lifImageNode, leicalifprocessor.image);
		return true;		
	}

	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
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
		
		// Dump attributes to the html pane 
		String className = nodeInfo.getClass().getName();
		if (className.endsWith("$ImageDescriptor")) {
			// TODO: Display this in the metadata table. The htmlPane 
			// does not longer exist in the AbstractViewer.
//			htmlPane.setText(
//					((ImageDescriptor) nodeInfo).toString());
		} else if (className.endsWith("$SubImageDescriptor")) {
			// TODO: Display this in the metadata table. The htmlPane 
			// does not longer exist in the AbstractViewer.
//			htmlPane.setText(
//					((SubImageDescriptor)nodeInfo).attributesToString().replace(
//							", ", "\n"));
		} else {
			// Clear the metadata table
			clearMetadataTable();
		}
	}

	/**
	 *  React to actions
	 *  @param e An ActionEvent 
	 */
	public void actionPerformed(ActionEvent e) {
	}

	/**
	 * Scans the user subfolder of the datamover incoming directory for
	 * datasets to be processed
	 * @param userName user name that must correspond to the subfolder name in
	 * the dropboxIncomingFolder
	 */
	public void scan() {

		// The user name MUST be set
		assert(this.userName.equals(""));
		
		// Make sure to clear the table of invalid datasets and
		// metadata
		clearInvalidDatasetsTable();
		clearMetadataTable();
		
		// Get the datamover incoming folder from the application properties
		// to which we append the user name to personalize the working space
		Properties appProperties = AppProperties.readPropertiesFromFile();
		File dropboxIncomingFolder = new File(
				appProperties.getProperty("DatamoverIncomingDir") +
				File.separator + userName);
		
		// Prepare a new root node for the Tree
		rootNode = new RootNode(new RootDescriptor("/"));

		// Global status
		boolean globalStatus = true;
		
		// We only consider lif files in the root
		File[] lifFiles = dropboxIncomingFolder.listFiles(
				new FilenameFilter() {
					public boolean accept(File file, String name) {
						return name.toLowerCase().endsWith(".lif");
					}
				});
		
		// Now parse the lif files and append the results to the Tree
		// under the root node  (and store success)
		for (File lifFile : lifFiles) {
			globalStatus = globalStatus & parse(lifFile);
		}
		
		// Create a tree that allows one selection at a time.
		tree.setModel(new DefaultTreeModel(rootNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		// Clear the metadata table
		clearMetadataTable();
		
		// Set isReady to globalStatus
		isReady = globalStatus;

		// Notify observers that the scanning is done 
		setChanged();
		notifyObservers(new ObserverActionParameters(
				ObserverActionParameters.Action.SCAN_COMPLETE,
				null));
		
	}
	
	/**
	 * Create the nodes for the tree
	 * @param top Root node
	 */
	protected void createNodes(AbstractNode lifImageNode, ImageDescriptor image) {
		
		SubImageNode subImageNode;
		
		for (SubImageDescriptor d : image.subImages) {

			// Add the experiments
			subImageNode = new SubImageNode(d);
			lifImageNode.add(subImageNode);

		}
	}

	@Override
	public boolean parse(File folder, String userName) {
		// TODO Auto-generated method stub
		return false;
	}
}
