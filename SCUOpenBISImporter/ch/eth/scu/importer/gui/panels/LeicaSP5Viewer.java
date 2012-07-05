package ch.eth.scu.importer.gui.panels;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.gui.components.ImageNode;
import ch.eth.scu.importer.gui.components.Node;
import ch.eth.scu.importer.gui.components.RootNode;
import ch.eth.scu.importer.gui.components.SubImageNode;
import ch.eth.scu.importer.processor.LeicaLifProcessor;

import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;

/**
 * Simple graphical viewer for the BDDIVAXMLProcessor
 * @author Aaron Ponti
 */
public class LeicaSP5Viewer extends AbstractViewer
	implements ActionListener, TreeSelectionListener {

	private static final long serialVersionUID = 1L;

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
		
		// Add initial info to the html pane
		htmlPane.setText("\nLeica LIF Viewer");
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
			htmlPane.setText("Invalid file!");
			leicalifprocessor = null;
			return false;
		}
		try {
			if (leicalifprocessor.parse() == false) {
				htmlPane.setText("Could not parse file!");
				leicalifprocessor = null;
				return false;
			}
		} catch (DependencyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		// Add the processor as new child of root
		ImageNode lifFileNode = 
				new ImageNode(leicalifprocessor);
		rootNode.add(lifFileNode);
		
		createNodes(lifFileNode, leicalifprocessor);
		return true;		
	}

	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		Node node = (Node) tree.getLastSelectedPathComponent();
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
		if (className.endsWith("LeicaLifProcessor")) {
			htmlPane.setText(
					((LeicaLifProcessor) nodeInfo).toString());
		} else if (className.endsWith("LeicaLifProcessor$ImageDescriptor")) {
			htmlPane.setText(
					((LeicaLifProcessor.ImageDescriptor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else {
			htmlPane.setText("");
		}
	}

	/**
	 *  React to actions
	 *  @param e An ActionEvent 
	 */
	public void actionPerformed(ActionEvent e) {
		return;
	}

	/**
	 * Scans the datamover incoming directory for datasets to be processed
	 */
	public void scan() {
		
		// Get the datamover incoming folder from the application properties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		File dropboxIncomingFolder = new File(
				appProperties.getProperty("DatamoverIncomingDir"));

		// Prepare a new root node for the Tree
		rootNode = new RootNode("/");

		// We only consider lif files in the root
		File[] lifFiles = dropboxIncomingFolder.listFiles(
				new FilenameFilter() {
					public boolean accept(File file, String name) {
						return name.toLowerCase().endsWith(".lif");
					}
				});
		
		// Now parse the lif files and append the results to the Tree
		// under the root node
		for (File lifFile : lifFiles) {
			parse(lifFile);
		}
		
		// Create a tree that allows one selection at a time.
		tree.setModel(new DefaultTreeModel(rootNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		// Clean the html pane
		htmlPane.setText("");
		
	}
	
	/**
	 * Create the nodes for the tree
	 * @param top Root node
	 */
	protected void createNodes(DefaultMutableTreeNode lifFileNode,
			LeicaLifProcessor leicalifprocessor) {
		
		SubImageNode imageDescriptor = null;
		
		for (LeicaLifProcessor.ImageDescriptor d : 
			leicalifprocessor.imageDescriptors) {

			// Add the experiments
			imageDescriptor = new SubImageNode(d);
			lifFileNode.add(imageDescriptor);

		}
	}
}
