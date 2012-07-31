package ch.eth.scu.importer.gui.panels;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.gui.components.ExperimentNode;
import ch.eth.scu.importer.gui.components.CustomTreeNode;
import ch.eth.scu.importer.gui.components.RootNode;
import ch.eth.scu.importer.gui.components.SpecimenNode;
import ch.eth.scu.importer.gui.components.TrayNode;
import ch.eth.scu.importer.gui.components.TubeNode;
import ch.eth.scu.importer.gui.components.XMLNode;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor;
import ch.eth.scu.importer.processor.FCSProcessor;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.Experiment;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.Specimen;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.Tray;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.Tube;

import java.awt.event.*;

import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.io.FileFilter;

/**
 * Simple graphical viewer for the BDDIVAXMLProcessor
 * @author Aaron Ponti
 */
public class BDLSRFortessaViewer extends AbstractViewer
	implements ActionListener, TreeSelectionListener {

	private static final long serialVersionUID = 1L;

	// The valueChanged() method is fired twice when selection is changed in 
	// a JTree, so we keep track of the last processed node to avoid parsing
	// the same FCS file twice every time the node is changed.
	private String lastSelectedNode;
	
	/**
	 * Constructor
	 */
	public BDLSRFortessaViewer() {
		
		// Call the AbstractViewer's constructor (to create the panel)
		super();
		
		// Add initial info to the html pane
		htmlPane.setText(
				"\nDisplays 'BD BioSciences FACSDiva\u2122 Software' " +
				"XML files with the associated 'Data File Standard " + 
				"for Flow Cytometry, Version FCS3.0' files generated " +
				"by the BD LSRFortessa flow cytometer.");	
	}

	/**
	 *  Parse the selected XML file and appends the resulting tree to the root
	 */
	public boolean parseXML(File subfolder, File xmlFile) {

		// Process the file
		BDFACSDIVAXMLProcessor xmlprocessor;
		try {
			xmlprocessor = new BDFACSDIVAXMLProcessor(
					xmlFile.getCanonicalPath());
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

		// Add the processor as new child of current folder node
		XMLNode xmlNode = 
				new XMLNode(xmlprocessor, xmlprocessor.getType());
		rootNode.add(xmlNode);
		
		// Add all the children
		createNodes(xmlNode, xmlprocessor);
		
		return true;
	}

	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		CustomTreeNode node = (CustomTreeNode) tree.getLastSelectedPathComponent();
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
					((BDFACSDIVAXMLProcessor.Tray) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("Specimen")) { 
			htmlPane.setText(
					((BDFACSDIVAXMLProcessor.Specimen) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("Tube")) {
			// Cast
			BDFACSDIVAXMLProcessor.Tube tube = 
					(BDFACSDIVAXMLProcessor.Tube) nodeInfo;
			// Display attributes
			String out = tube.attributesToString().replace(", ", "\n");
			// Parse the fcs file and dump its metadata
			String fcsFile = tube.fullDataFilename;
			FCSProcessor fcs = new FCSProcessor(fcsFile, false);
			try {
				fcs.parse();
				out += "\n\n" + fcs.metadataDump();
			} catch (IOException e1) {
				out += "\n\nCould not parse file " + fcsFile + ".";
			}
			// Display
			htmlPane.setText(out);
		} else {
			htmlPane.setText("");
		}
	}

	/**
	 * Create the nodes for the tree
	 * @param top Root node
	 */
	protected void createNodes(CustomTreeNode top,
			BDFACSDIVAXMLProcessor xmlprocessor) {
		ExperimentNode experiment = null;
		TrayNode tray = null;
		SpecimenNode specimen = null;
		TubeNode tube = null;

		for (Experiment e : xmlprocessor.experiments) {

			// Add the experiments
			experiment = new ExperimentNode(e, e.getType());
			top.add(experiment);

			for (Tray t : e.trays) {

				// Add the trays
				tray = new TrayNode(t, t.getType());
				experiment.add(tray);

				for (Specimen s : t.specimens) {

					// Add the specimens
					specimen = new SpecimenNode(s, s.getType());
					tray.add(specimen);

					for (Tube tb : s.tubes) {

						// Add the tubes
						tube = new TubeNode(tb, tb.getType());
						specimen.add(tube);
					}

				}

			}

			for (Specimen s : e.specimens) {

				// Add the specimens
				specimen = new SpecimenNode(s, s.getType());
				experiment.add(specimen);

				for (Tube tb : s.tubes) {

					// Add the tubes
					tube = new TubeNode(tb, tb.getType());
					specimen.add(tube);
				}

			}


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
	 * Scan the datamover incoming folder
	 */
	public void scan() {
		
		// Get the datamover incoming folder from the application properties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		File dropboxIncomingFolder = new File(
				appProperties.getProperty("DatamoverIncomingDir"));
		
		// Get a list of all subfolders
		File[] rootSubFolders = dropboxIncomingFolder.listFiles(
				new FileFilter() {
					public boolean accept(File file) {
						return file.isDirectory();
					}
				});

		// Prepare a new root node for the Tree
		rootNode = new RootNode("/", "root");

		// Go over all folders and check that there is an xml file inside
		for (File subfolder : rootSubFolders) {
			File[] xmlFiles = subfolder.listFiles(
					new FilenameFilter() {
						public boolean accept(File file, String name) {
							return name.toLowerCase().endsWith(".xml");
						}
					});
			
			// Make sure there is only one xml file per folder
			if (xmlFiles.length == 0) {
				System.err.println("No xml found in folder " + subfolder + 
						". Skipping.");
			} else if (xmlFiles.length > 1) {
				System.err.println("Only on xml expected! Skipping folder " +
			subfolder + ".");
			} else {
				// And now parse the file and append the results to the Tree
				// under current folder node
				parseXML(subfolder, xmlFiles[0]);
			}
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

}