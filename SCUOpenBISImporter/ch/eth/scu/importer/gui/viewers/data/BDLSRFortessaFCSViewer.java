package ch.eth.scu.importer.gui.viewers.data;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.gui.viewers.data.model.CustomTreeNode;
import ch.eth.scu.importer.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.gui.viewers.data.model.FCSFileNode;
import ch.eth.scu.importer.gui.viewers.data.model.FolderNode;
import ch.eth.scu.importer.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.gui.viewers.data.model.SpecimenNode;
import ch.eth.scu.importer.gui.viewers.data.model.TrayNode;
import ch.eth.scu.importer.gui.viewers.data.model.TubeNode;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor;
import ch.eth.scu.importer.processor.FCSProcessor;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.ExperimentDescriptor;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.SpecimenDescriptor;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.TrayDescriptor;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.TubeDescriptor;
import ch.eth.scu.importer.processor.model.RootDescriptor;

import java.awt.event.*;

import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.io.FileFilter;

/**
 * Simple graphical viewer for the BDDIVAXMLProcessor
 * @author Aaron Ponti
 */
public class BDLSRFortessaFCSViewer extends AbstractViewer {

	// The valueChanged() method is fired twice when selection is changed in 
	// a JTree, so we keep track of the last processed node to avoid parsing
	// the same FCS file twice every time the node is changed.
	private String lastSelectedNode;

	/**
	 * Constructor
	 */
	public BDLSRFortessaFCSViewer() {
		
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
	 *  Parse the FCS folder and append the resulting tree to the root
	 */
	public boolean parse(File folder, String userName) {

		// Process the file
		BDFACSDIVAFCSProcessor divafcsprocessor;
		try {
			divafcsprocessor = new BDFACSDIVAFCSProcessor(
					folder.getCanonicalPath(), userName);
		} catch (IOException e) {
			return false;
		}

		if (divafcsprocessor.parse() == false) {
			htmlPane.setText("Could not parse the folder!");
			divafcsprocessor = null;
			return false;
		}

		// Make sure we have a clean FCS export
		if (divafcsprocessor.isCleanFCSExport() == false) {
			System.err.println("The dataset \"" + divafcsprocessor.toString() +
					"\" is not a clean FCS export and will be skipped.");
			DefaultTableModel model = 
					(DefaultTableModel) invalidDatasetsTable.getModel();
			model.addRow(new Object[] { folder.getName(), "Bad export mode."});
			return false;
		}

		// Create a folder note as a child of the root node
		FolderNode folderNode = 
				new FolderNode(divafcsprocessor.folderDescriptor);
		rootNode.add(folderNode);
		
		// We will append the experiment nodes directly to the root node
		createNodes(folderNode, divafcsprocessor.folderDescriptor);
		
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
		if (className.endsWith("ExperimentDescriptor")) { 
			htmlPane.setText(
					((BDFACSDIVAFCSProcessor.ExperimentDescriptor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("TrayDescriptor")) { 
			htmlPane.setText(
					((BDFACSDIVAFCSProcessor.TrayDescriptor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("SpecimenDescriptor")) { 
			htmlPane.setText(
					((BDFACSDIVAFCSProcessor.SpecimenDescriptor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));
		} else if (className.endsWith("TubeDescriptor")) {
			htmlPane.setText(
					((BDFACSDIVAFCSProcessor.TubeDescriptor) 
							nodeInfo).attributesToString().replace(
									", ", "\n"));			
		} else if (className.endsWith("FCSFileDescriptor")) {
			// Cast
			BDFACSDIVAFCSProcessor.FCSFileDescriptor fcsFile = 
					(BDFACSDIVAFCSProcessor.FCSFileDescriptor) nodeInfo;
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
	 * @param rootNode Root node
	 */
	protected void createNodes(CustomTreeNode top,
			BDFACSDIVAFCSProcessor.FolderDescriptor folderDescriptor) {
		
		ExperimentNode experiment = null;
		TrayNode tray = null;
		SpecimenNode specimen = null;
		TubeNode tube = null;
		FCSFileNode fcs = null;

		for (String expKey : folderDescriptor.experiments.keySet()) {

			// Get the ExperimentDescriptor
			ExperimentDescriptor e = 
					folderDescriptor.experiments.get(expKey);

			// Add the experiments
			experiment = new ExperimentNode(e);
			top.add(experiment);

			for (String trayKey: e.trays.keySet()) {

				// Get the TrayDescriptor
				TrayDescriptor t  = e.trays.get(trayKey);
				
				// Add the trays
				tray = new TrayNode(t);
				experiment.add(tray);

				for (String specKey : t.specimens.keySet()) {

					// Get the SpecimenDescriptor
					SpecimenDescriptor s = t.specimens.get(specKey);
					
					// Add the specimens
					specimen = new SpecimenNode(s);
					tray.add(specimen);

					for (String tubeKey : s.tubes.keySet()) {

						// Get the TubeDescriptor
						TubeDescriptor tb  = s.tubes.get(tubeKey);
						
						// Add the tubes
						tube = new TubeNode(tb);
						specimen.add(tube);
						
						// Add the fcs files
						fcs = new FCSFileNode(tb.fcsFile);
						tube.add(fcs);
					}

				}

			}

			for (String specKey : e.specimens.keySet()) {

				// Get the SpecimenDescriptor
				SpecimenDescriptor s = e.specimens.get(specKey);
				
				// Add the specimens
				specimen = new SpecimenNode(s);
				experiment.add(specimen);

				for (String tubeKey : s.tubes.keySet()) {

					// Get the TubeDescriptor
					TubeDescriptor tb = s.tubes.get(tubeKey);
					
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
		rootNode = new RootNode(new RootDescriptor("/" + userName));

		// Parse all subfolders (and store success)
		boolean globalStatus = true;
		if (rootSubFolders != null) { 
			for (File subfolder : rootSubFolders) {
				globalStatus = globalStatus & parse(subfolder, userName);
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
