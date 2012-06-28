package ch.eth.scu.importer.gui.panels;

import ch.eth.scu.importer.processor.LeicaLifProcessor;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.io.File;
import java.io.IOException;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;

/**
 * Simple graphical viewer for the BDDIVAXMLProcessor
 * @author Aaron Ponti
 */
public class LeicaSP5Viewer extends AbstractViewer
	implements ActionListener, TreeSelectionListener {

	private static final long serialVersionUID = 1L;

	// Processors
	private LeicaLifProcessor leicalifprocessor = null;

	private File file;
	
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
	 *  Parse the selected XML file. 
	 */
	public boolean parse() {

		// Process the file
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

		// Create the root node
		rootNode = new DefaultMutableTreeNode(leicalifprocessor);

		// Add all the children
		createNodes(rootNode);

		// Create a tree that allows one selection at a time.
		tree.setModel(new DefaultTreeModel(rootNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		// Clean the html pane
		htmlPane.setText("");

		return true;
	}

	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}

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
	 * Create the nodes for the tree
	 * @param top Root node
	 */
	protected void createNodes(DefaultMutableTreeNode top) {
		DefaultMutableTreeNode imageDescriptor = null;
		
		for (LeicaLifProcessor.ImageDescriptor d : 
			leicalifprocessor.imageDescriptors) {

			// Add the experiments
			imageDescriptor = new DefaultMutableTreeNode(d);
			top.add(imageDescriptor);

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
	 * Asks the user to pick a file to be parsed
	 */
	public void pickFile() {

		// Create a file chooser
		final JFileChooser fc = new JFileChooser();

		// Filters
		FileFilter filter = new FileNameExtensionFilter(
				"Leica LIF files (*.lif)", "lif");
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(filter);

		// Get a file from an open dialog
		int returnVal = fc.showOpenDialog(htmlPane);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			if (parse() == false) {
				file = null;
			}
		} else {
			file = null;
			return;
		}
	}

	@Override
	public void pickDir() {
		// TODO Auto-generated method stub
		
	}
}
