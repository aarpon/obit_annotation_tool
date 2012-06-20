package ch.eth.scu.scuimporter.gui.panels;

import ch.eth.scu.scuimporter.processor.BDFACSDIVAXMLProcessor;
import ch.eth.scu.scuimporter.processor.BDFACSDIVAXMLProcessor.Experiment;
import ch.eth.scu.scuimporter.processor.BDFACSDIVAXMLProcessor.Experiment.Specimen;
import ch.eth.scu.scuimporter.processor.BDFACSDIVAXMLProcessor.Experiment.Specimen.Tube;
import ch.eth.scu.scuimporter.processor.BDFACSDIVAXMLProcessor.Experiment.Tray;
import ch.eth.scu.scuimporter.processor.FCSProcessor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.io.File;
import java.io.IOException;

/**
 * Simple graphical viewer for the BDDIVAXMLProcessor
 * @author Aaron Ponti
 */
public class BDLSRFortessaViewer extends JPanel
	implements ActionListener, TreeSelectionListener {

	private static final long serialVersionUID = 1L;
	private JEditorPane htmlPane;
	private JTree tree;
	private File file;
	private JLabel title;
	private DefaultMutableTreeNode rootNode;
	private JScrollPane treeView;
	private JScrollPane htmlView;
	private JLabel metadataView;
	private BDFACSDIVAXMLProcessor xmlprocessor = null;
	private FCSProcessor fcsprocessor = null;

	/**
	 * Constructor
	 */
	public BDLSRFortessaViewer() {
		
		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);

		// Common constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.BOTH;
		
		// Add a title JLabel
		title = new JLabel("Data viewer");

		// Add the tree viewer to the layout
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		add(title, constraints);
		
		// Create the root node
		rootNode = new DefaultMutableTreeNode("Pick a file...");

		// Create a tree that allows one selection at a time.
		tree = new JTree(rootNode);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		// Create the scroll pane and add the tree to it. 
		treeView = new JScrollPane(tree);

		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 0.5;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		add(treeView, constraints);

		// Add a simple label
		metadataView = new JLabel("Metadata viewer");
		metadataView.setVerticalAlignment(SwingConstants.TOP);

		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		add(metadataView, constraints);
		
		// Create the HTML viewing pane.
		htmlPane = new JEditorPane();
		htmlPane.setEditable(false);
		htmlView = new JScrollPane(htmlPane);

		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.weightx = 1.0;
		constraints.weighty = 0.5;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		add(htmlView, constraints);

		// Add initial info to the html pane
		htmlPane.setText("\nDisplays 'BD BioSciences FACSDiva\u2122 Software' " +
				"XML files with the associated 'Data File Standard " + 
				"for Flow Cytometry, Version FCS3.0' files generated " +
				"by the BD LSRFortessa flow cytometer.");
		
		// Set sizes
		setMinimumSize(new Dimension(400, 800));
		setPreferredSize(new Dimension(400, 800));
	}

	/**
	 *  Parse the selected XML file. 
	 */
	public boolean parseXML() {

		// Process the file
		try {
			xmlprocessor = new BDFACSDIVAXMLProcessor(file.getCanonicalPath());
		} catch (IOException e) {
			htmlPane.setText("Invalid file!");
			xmlprocessor = null;
			return false;
		}
		if (xmlprocessor.parse() == false) {
			htmlPane.setText("Could not parse file!");
			xmlprocessor = null;
			return false;
		}

		// Create the root node
		rootNode = new DefaultMutableTreeNode(xmlprocessor);

		// Add all the children
		createNodes(rootNode);

		// Create a tree that allows one selection at a time.
		tree.setModel(new DefaultTreeModel(rootNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		// Make sure the other processor is null
		fcsprocessor = null;

		// Clean the html pane
		htmlPane.setText("");

		return true;
	}

	/**
	 *  Parse the selected FCS file. 
	 */
	public boolean parseFCS() {

		// Process the file
		try {
			fcsprocessor = new FCSProcessor(file.getCanonicalPath(), false);
		} catch (IOException e) {
			htmlPane.setText("Invalid file!");
			fcsprocessor = null;
			return false;
		}
		try {
			fcsprocessor.parse();
		} catch (IOException e) {
			htmlPane.setText("Could not parse file!");
			fcsprocessor = null;
			return false;
		}

		// Create the root node
		rootNode = new DefaultMutableTreeNode(fcsprocessor);

		// Create a tree that allows one selection at a time.
		tree.setModel(new DefaultTreeModel(rootNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		// Display the metadata in the html pane
		htmlPane.setText(fcsprocessor.metadataDump());

		// Make sure the other processor is null
		xmlprocessor = null;

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

		// Print the attributes
		String className = nodeInfo.getClass().getName();
		if (className.endsWith("BDFACSDIVAXMLProcessor")) {
			htmlPane.setText(
					((BDFACSDIVAXMLProcessor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("Experiment")) { 
			htmlPane.setText(
					((BDFACSDIVAXMLProcessor.Experiment) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("Tray")) { 
			htmlPane.setText(
					((BDFACSDIVAXMLProcessor.Experiment.Tray) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("Specimen")) { 
			htmlPane.setText(
					((BDFACSDIVAXMLProcessor.Experiment.Specimen) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("Tube")) {
			// Cast
			BDFACSDIVAXMLProcessor.Experiment.Specimen.Tube tube = 
					(BDFACSDIVAXMLProcessor.Experiment.Specimen.Tube) nodeInfo;
			// Display attributes
			String out = tube.attributesToString().replace(", ", "\n");
			// Parse the fcs file and dump its metadata
			String fcsFile = file.getParent() + File.separator + tube.dataFilename;
			FCSProcessor fcs = new FCSProcessor(fcsFile, false);
			try {
				fcs.parse();
				out += "\n\n" + fcs.metadataDump();
			} catch (IOException e1) {
				out += "\n\nCould not parse file " + fcsFile + ".";
			}
			// Display
			htmlPane.setText(out);
		} else if (className.endsWith("FCSProcessor")) {
			// Strictly speaking there is nothing to do, but we
			// refresh the display in case.
			if (fcsprocessor != null) {
				htmlPane.setText(fcsprocessor.metadataDump());
			}
		} else {
			htmlPane.setText("");
		}
	}

	/**
	 * Create the nodes for the tree
	 * @param top Root node
	 */
	private void createNodes(DefaultMutableTreeNode top) {
		DefaultMutableTreeNode experiment = null;
		DefaultMutableTreeNode tray = null;
		DefaultMutableTreeNode specimen = null;
		DefaultMutableTreeNode tube = null;

		for (Experiment e : xmlprocessor.experiments) {

			// Add the experiments
			experiment = new DefaultMutableTreeNode(e);
			top.add(experiment);

			for (Tray t : e.trays) {

				// Add the trays
				tray = new DefaultMutableTreeNode(t);
				experiment.add(tray);

				for (Specimen s : t.specimens) {

					// Add the specimens
					specimen = new DefaultMutableTreeNode(s);
					tray.add(specimen);

					for (Tube tb : s.tubes) {

						// Add the tubes
						tube = new DefaultMutableTreeNode(tb);
						specimen.add(tube);
					}

				}

			}

			for (Specimen s : e.specimens) {

				// Add the specimens
				specimen = new DefaultMutableTreeNode(s);
				experiment.add(specimen);

				for (Tube tb : s.tubes) {

					// Add the tubes
					tube = new DefaultMutableTreeNode(tb);
					specimen.add(tube);
				}

			}


		}
	}

	/**
	 *  User chose Open or Quit on the context menu
	 */
	public void actionPerformed(ActionEvent e) {

		// React to the context menu
		if (e.getActionCommand().equals("Open")) {

		} else if (e.getActionCommand().equals("Quit")) {
			System.exit(0);
		} else {
			return;
		}
	}

	/**
	 * Asks the user to pick a file to be parsed
	 */
	public void pickFile() {

		// Create a file chooser
		final JFileChooser fc = new JFileChooser();

		// Filters
		FileFilter filter = new FileNameExtensionFilter(
				"BD LSRFortessa files (*.xml, *.fcs)", "xml", "fcs");
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(filter);

		// Get a file from an open dialog
		int returnVal = fc.showOpenDialog(htmlPane);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			int dotPos = file.getName().lastIndexOf(".");
			String extension = file.getName().substring(dotPos);
			if (extension.equalsIgnoreCase(".xml")) {
				if (parseXML() == false) {
					file = null;
				}
			} else if (extension.equalsIgnoreCase(".fcs")) {
				if (parseFCS() == false) {
					file = null;
				}
			} else {
				System.err.println("Unknown extension!");
			}
		} else {
			file = null;
			return;
		}
	}

}
