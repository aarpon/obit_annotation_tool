package ch.eth.scu.importer.gui.viewers.data;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.gui.viewers.data.model.CustomTreeNode;
import ch.eth.scu.importer.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.gui.viewers.data.model.FCSFileNode;
import ch.eth.scu.importer.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.gui.viewers.data.model.SpecimenNode;
import ch.eth.scu.importer.gui.viewers.data.model.TrayNode;
import ch.eth.scu.importer.gui.viewers.data.model.TubeNode;
import ch.eth.scu.importer.gui.viewers.data.model.XMLFileNode;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor;
import ch.eth.scu.importer.processor.FCSProcessor;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.ExperimentDescriptor;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.SpecimenDescriptor;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.TrayDescriptor;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.TubeDescriptor;
import ch.eth.scu.importer.processor.model.RootDescriptor;

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
public class BDLSRFortessaXMLViewer extends AbstractViewer {

	// The valueChanged() method is fired twice when selection is changed in 
	// a JTree, so we keep track of the last processed node to avoid parsing
	// the same FCS file twice every time the node is changed.
	private String lastSelectedNode;
	
	/**
	 * Constructor
	 */
	public BDLSRFortessaXMLViewer() {
		
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
	public boolean parseXML(File subfolder, File xmlFile, String userName) {

		// Process the file
		BDFACSDIVAXMLProcessor xmlprocessor;
		try {
			xmlprocessor = new BDFACSDIVAXMLProcessor(
					xmlFile.getCanonicalPath(), userName);
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
		XMLFileNode xmlNode = 
				new XMLFileNode(xmlprocessor.xmlFile);
		rootNode.add(xmlNode);
		
		// Add all the children
		createNodes(xmlNode, xmlprocessor.xmlFile);
		
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
		if (className.endsWith("XMLFileDescriptor")) {
			htmlPane.setText(
					((BDFACSDIVAXMLProcessor.XMLFileDescriptor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("ExperimentDescriptor")) { 
			htmlPane.setText(
					((BDFACSDIVAXMLProcessor.ExperimentDescriptor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("TrayDescriptor")) { 
			htmlPane.setText(
					((BDFACSDIVAXMLProcessor.TrayDescriptor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("SpecimenDescriptor")) { 
			htmlPane.setText(
					((BDFACSDIVAXMLProcessor.SpecimenDescriptor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("TubeDescriptor")) {
			htmlPane.setText(
					((BDFACSDIVAXMLProcessor.TubeDescriptor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));			
		} else if (className.endsWith("FCSFileDescriptor")) {
			// Cast
			BDFACSDIVAXMLProcessor.FCSFileDescriptor fcsFile = 
					(BDFACSDIVAXMLProcessor.FCSFileDescriptor) nodeInfo;
			String fcsFileName = fcsFile.getFileName();
			FCSProcessor fcs = new FCSProcessor(fcsFileName, false);
			String out = "";
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
			BDFACSDIVAXMLProcessor.XMLFileDescriptor xmlfile) {
		ExperimentNode experiment = null;
		TrayNode tray = null;
		SpecimenNode specimen = null;
		TubeNode tube = null;
		FCSFileNode fcs = null;

		for (ExperimentDescriptor e : xmlfile.experiments) {

			// Add the experiments
			experiment = new ExperimentNode(e);
			top.add(experiment);

			for (TrayDescriptor t : e.trays) {

				// Add the trays
				tray = new TrayNode(t);
				experiment.add(tray);

				for (SpecimenDescriptor s : t.specimens) {

					// Add the specimens
					specimen = new SpecimenNode(s);
					tray.add(specimen);

					for (TubeDescriptor tb : s.tubes) {

						// Add the tubes
						tube = new TubeNode(tb);
						specimen.add(tube);
						
						// Add the fcs files
						fcs = new FCSFileNode(tb.fcsFile);
						tube.add(fcs);
					}

				}

			}

			for (SpecimenDescriptor s : e.specimens) {

				// Add the specimens
				specimen = new SpecimenNode(s);
				experiment.add(specimen);

				for (TubeDescriptor tb : s.tubes) {

					// Add the tubes
					tube = new TubeNode(tb);
					specimen.add(tube);

					// Add the fcs files
					fcs = new FCSFileNode(tb.fcsFile);
					tube.add(fcs);
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
	 * Scans the user subfolder of the datamover incoming directory for
	 * datasets to be processed
	 * @param userName user name that must correspond to the subfolder name in
	 * the dropboxIncomingFolder 
	 */
	public void scan(String userName) {

		// Make sure to clear the table of invalid datasets
		clearTable();
		
		// Get the datamover incoming folder from the application properties
		// to which we append the user name to personalize the working space
		Properties appProperties = AppProperties.readPropertiesFromFile();
		File dropboxIncomingFolder = new File(
				appProperties.getProperty("DatamoverIncomingDir") +
				File.separator + userName);
		
		// Get a list of all subfolders
		File[] rootSubFolders = dropboxIncomingFolder.listFiles(
				new FileFilter() {
					public boolean accept(File file) {
						return file.isDirectory();
					}
				});

		// Prepare a new root node for the Tree
		rootNode = new RootNode(new RootDescriptor("/"));

		// Global status
		boolean globalStatus = true;
		
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
				globalStatus = false;
			} else if (xmlFiles.length > 1) {
				System.err.println("Only on xml expected! Skipping folder " +
			subfolder + ".");
				globalStatus = false;
			} else {
				// And now parse the file and append the results to the Tree
				// under current folder node  (and store success)
				globalStatus = globalStatus &
						parseXML(subfolder, xmlFiles[0], userName);
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
		
		// Set isReady to globalStatus
		isReady = globalStatus;
	
		// Notify observers that the scanning is done 
		setChanged();
		notifyObservers();		
	}

}
