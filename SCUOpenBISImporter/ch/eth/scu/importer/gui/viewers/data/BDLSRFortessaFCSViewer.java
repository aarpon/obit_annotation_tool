package ch.eth.scu.importer.gui.viewers.data;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.gui.viewers.data.model.FCSFileNode;
import ch.eth.scu.importer.gui.viewers.data.model.FolderNode;
import ch.eth.scu.importer.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.gui.viewers.data.model.SpecimenNode;
import ch.eth.scu.importer.gui.viewers.data.model.TrayNode;
import ch.eth.scu.importer.gui.viewers.data.model.TubeNode;
import ch.eth.scu.importer.processor.FCSProcessor;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.Specimen;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.Tray;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.Experiment;
import ch.eth.scu.importer.processor.BDFACSDIVAFCSProcessor.Tube;
import ch.eth.scu.importer.processor.model.RootDescriptor;

import java.awt.event.*;

import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
	
	// When selecting an entry in the tree, the editor might be refreshed
	// in response. This should happen only if a new folder is selected -
	// as long as one changes between nodes within an experiment no
	// editor refresh is needed.
	private AbstractNode lastSelectedFolder;

	/**
	 * Constructor
	 */
	public BDLSRFortessaFCSViewer() {
		
		// Call the AbstractViewer's constructor (to create the panel)
		super();

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

		if (!divafcsprocessor.parse()) {
			System.err.println("Could not parse the folder! (TODO: Use panel!)");
			divafcsprocessor = null;
			return false;
		}

		// Make sure we have a valid dataset
		if (!divafcsprocessor.validator.isValid) {
			DefaultTableModel model = 
					(DefaultTableModel) invalidDatasetsTable.getModel();
			int nError = 0;
			StringBuilder err = new StringBuilder("");
			for (String errorString : divafcsprocessor.validator.errorMessages) {
				nError++;
				err.append("(").append(nError).append(") ").append(errorString);
			}
			model.addRow(new Object[] {folder.getName(), err});
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
			addAttributesToMetadataTable(
                    ((BDFACSDIVAFCSProcessor.Experiment)
                            nodeInfo).getAttributes());
		} else if (className.equals("Tray")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDFACSDIVAFCSProcessor.Tray) 
							nodeInfo).getAttributes());
		} else if (className.equals("Specimen")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDFACSDIVAFCSProcessor.Specimen)
							nodeInfo).getAttributes());
		} else if (className.equals("Tube")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDFACSDIVAFCSProcessor.Tube)
							nodeInfo).getAttributes());
		} else if (className.equals("FCSFile")) {
			// Cast
			BDFACSDIVAFCSProcessor.FCSFile fcsFile =
					(BDFACSDIVAFCSProcessor.FCSFile) nodeInfo;
			String fcsFileName = fcsFile.getFileName();
			FCSProcessor fcs = new FCSProcessor(fcsFileName, false);
			try {
				fcs.parse();
				clearMetadataTable();
				addAttributesToMetadataTable(fcs.getAllKeywords());
			} catch (IOException e1) {
				System.err.println("Could not parse file " + fcsFile + ". (TODO: Use panel)");
			}
		} else {
			clearMetadataTable();
		}

        // Get the folder name
        AbstractNode folderNode = getFolderNode(node);
        if (folderNode != null && folderNode != lastSelectedFolder) {
        		
        		// Update the lastSelectedFolder property
        		lastSelectedFolder = folderNode;
        		
            // Notify the editor to update its view
            setChanged();
            notifyObservers(new ObserverActionParameters(
                ObserverActionParameters.Action.EXPERIMENT_CHANGED, folderNode));
        }
	}

    /**
     * Climb the JTree to find the folder node and return its name
     * @param selectedNode Node selected in the JTree
     * @return folder node name
     */
    protected AbstractNode getFolderNode(AbstractNode selectedNode) {

        // Get the class name of the selected node
        String className = selectedNode.getUserObject().getClass().getSimpleName();

        // Try to get the folder name
        if (className.equals("RootDescriptor")) {
            // We are above the folder node, we return ""
            return null;
        } else if (className.equals("Folder")) {
            // We are at the folder node , we return its name
            return selectedNode;
        } else {
            // We are somewhere below the folder node: we climb up the tree
            // until we find it and then return its name
            AbstractNode parentNode = (AbstractNode) selectedNode.getParent();
            while (parentNode != null) {
                // Are we at the folder node?
                if (parentNode.getUserObject().getClass().getSimpleName().equals("Folder")) {
                    return parentNode;
                } else {
                    parentNode = (AbstractNode) parentNode.getParent();
                }
            }
        }
        return null;
    }

    /**
         * Create the nodes for the tree
         * @param top Root node for the tree
         * @param folderDescriptor A folder descriptor object.
         */
	protected void createNodes(AbstractNode top,
			BDFACSDIVAFCSProcessor.Folder folderDescriptor) {
		
		ExperimentNode experiment;
		TrayNode tray;
		SpecimenNode specimen;
		TubeNode tube;
		FCSFileNode fcs;

		for (String expKey : folderDescriptor.experiments.keySet()) {

			// Get the ExperimentDescriptor
			Experiment e = 
					folderDescriptor.experiments.get(expKey);

			// Add the experiments
			experiment = new ExperimentNode(e);
			top.add(experiment);

			for (String trayKey: e.trays.keySet()) {

				// Get the TrayDescriptor
				Tray t  = e.trays.get(trayKey);
				
				// Add the trays
				tray = new TrayNode(t);
				experiment.add(tray);

				for (String specKey : t.specimens.keySet()) {

					// Get the SpecimenDescriptor
					Specimen s = t.specimens.get(specKey);
					
					// Add the specimens
					specimen = new SpecimenNode(s);
					tray.add(specimen);

					for (String tubeKey : s.tubes.keySet()) {

						// Get the TubeDescriptor
						Tube tb  = s.tubes.get(tubeKey);
						
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
				Specimen s = e.specimens.get(specKey);
				
				// Add the specimens
				specimen = new SpecimenNode(s);
				experiment.add(specimen);

				for (String tubeKey : s.tubes.keySet()) {

					// Get the TubeDescriptor
					Tube tb = s.tubes.get(tubeKey);
					
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
    }

	/**
	 * Scans the user subfolder of the datamover incoming directory for
	 * datasets to be processed
	 * @param userName user name that must correspond to the subfolder name in
	 * the dropboxIncomingFolder
	 */
	public void scan(String userName) {

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
	 * Adds all key-value pairs from an attributes Map to the metadata
	 * view table 
	 * @param attributes Map of attributes returned by the various
	 * processors.
	 */
	private void addAttributesToMetadataTable(Map<String, String> attributes) {
		DefaultTableModel model =
				(DefaultTableModel) metadataViewTable.getModel();
		for (String key: attributes.keySet() ) {
			String value = attributes.get(key);
			model.addRow(new Object[] {key, value});
		}
	}
}
