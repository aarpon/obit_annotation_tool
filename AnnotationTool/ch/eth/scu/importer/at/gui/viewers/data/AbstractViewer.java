package ch.eth.scu.importer.at.gui.viewers.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import ch.eth.scu.importer.at.gui.pane.OutputPane;
import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.at.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.at.gui.viewers.data.view.DataViewerTree;
import ch.eth.scu.importer.at.gui.viewers.data.view.DataViewerTreeToXML;
import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.lsrfortessa.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.lsrfortessa.processors.model.ExperimentDescriptor;
import ch.eth.scu.importer.processors.model.RootDescriptor;

/**
 * Abstract viewer for processors
 * @author Aaron Ponti
 */
abstract public class AbstractViewer extends Observable
	implements ActionListener, TreeSelectionListener {

	// The valueChanged() method is fired twice when selection is changed  
	// in a JTree, so we keep track of the last processed node to avoid 
	// parsing the same FCS file twice every time the node is changed.
	protected String lastSelectedNode;
	
	// When selecting an entry in the tree, the editor might be refreshed
	// in response. This should happen only if a new folder is selected -
	// as long as one changes between nodes within an experiment no
	// editor refresh is needed.
	protected AbstractNode lastSelectedExperiment;
	
	protected boolean isReady = false;
	protected String userName;
	
	protected JPanel panel;
	protected DataViewerTree tree;
	protected JLabel title;
	protected DefaultMutableTreeNode rootNode;
	protected JScrollPane treeView;
	protected JScrollPane htmlView;
	protected JButton rescanButton;
	protected JLabel metadataView;
	protected JTable metadataViewTable;
	protected JScrollPane metadataViewPane;	
	protected JLabel invalidDatasets;
	protected JTable invalidDatasetsTable;
	protected JScrollPane invalidDatasetsPane;
	protected OutputPane outputPane;
	
	/**
	 * Read-only table model
	 * @author Aaron Ponti
	 *
	 */
	public class ReadOnlyTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		
		/**
		 * Constructor
		 * @param mdData 2D data array
		 * @param mdColumnNames Array of column names
		 */
		public ReadOnlyTableModel(Object[][] mdData, String mdColumnNames[]) {
			super(mdData, mdColumnNames);
		}

		/**
		 * Make sure the Table is non-editable
		 * @param row number
		 * @param column number
		 */
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}

	/**
	 * Parses current dataset (file or folder) by doing the necessary
	 * preparation work and then calling the needed Processor.
	 * 
	 * @param folder	 Full folder (or file) name
	 * @return true if the scanning of the dataset was successful, false
	 * otherwise.
	 */
	abstract public boolean parse(File folder); 
	
	/**
	 * Constructor
	 * The constructor creates the actual panel to be displayed on the UI.
	 */
	public AbstractViewer() {
		
		// Create a new JPanel
		panel = new JPanel();
		
		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		panel.setLayout(gridBagLayout);

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
		constraints.insets = new Insets(5, 5, 5, 5);
		panel.add(title, constraints);
		
		// Create the Tree
		rootNode = new RootNode(new RootDescriptor(new File("/")));
		
		// Create a tree that allows one selection at a time.
		tree = new DataViewerTree(rootNode);
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
		constraints.weighty = 0.45;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 0, 5);
		panel.add(treeView, constraints);

		// Add a rescan button
		rescanButton = new JButton("Scan");
		rescanButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                scan();
            }
        });  

		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		panel.add(rescanButton, constraints);
		
		// Add a simple label
		metadataView = new JLabel("Metadata viewer");
		metadataView.setVerticalAlignment(SwingConstants.TOP);

		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		panel.add(metadataView, constraints);
		
		// Add the table
		Object[][] mdData = { };
		String mdColumnNames[] = { "Name", "Value" };
		metadataViewTable = new JTable(
				new ReadOnlyTableModel(mdData, mdColumnNames));
		metadataViewTable.setShowGrid(false);
		metadataViewTable.setFillsViewportHeight(true);
		metadataViewTable.setAutoCreateRowSorter(true);
		metadataViewPane = new JScrollPane(metadataViewTable);
		
		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.weightx = 1.0;
		constraints.weighty = 0.20;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		panel.add(metadataViewPane, constraints);

		// Add a simple label
		invalidDatasets = new JLabel("Invalid datasets");
		invalidDatasets.setVerticalAlignment(SwingConstants.TOP);

		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		panel.add(invalidDatasets, constraints);
		
		// Add the table
		Object[][] data = { };
		String columnNames[] = { "File or folder", "Issue" };
		invalidDatasetsTable = new JTable(
				new ReadOnlyTableModel(data, columnNames));
		invalidDatasetsTable.setShowGrid(false);
		invalidDatasetsTable.setFillsViewportHeight(true);
		invalidDatasetsTable.setAutoCreateRowSorter(true);
		invalidDatasetsPane = new JScrollPane(invalidDatasetsTable);
        
		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.weightx = 1.0;
		constraints.weighty = 0.05;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		panel.add(invalidDatasetsPane, constraints);
		
		// Set the preferred and minimum size
		panel.setMinimumSize(new Dimension(400, 700));
		panel.setPreferredSize(new Dimension(400, 700));

	}

	/**
	 * Sets the user name to be used for scanning the user folder
	 * @param userName User name
	 * 
	 * This must be called before scan() can be called!
	 * 
	 * @see scan()
	 */
	public void setUserName(String userName) {

		// Store the user name
		this.userName = userName;
	
	}
	
	/**
	 * Sets the reference to the OutputPane
	 * @param outputPane A reference to the main window output pane to be 
	 * used to display information. 
	 */
	public void setOutputPane(OutputPane outputPane) {

		// Store the OutputPane reference
		this.outputPane = outputPane;
	
	}
	
	/**
	 * Scans the datamover incoming directory for datasets to be processed.
	 * At the end of scanning, the function MUST set isReady to true.
	 */
	public void scan() {

		// Clear the tree
		clearTree();
		
		// Notify observers that the scanning is about to start 
		setChanged();
		notifyObservers(new ObserverActionParameters(
				ObserverActionParameters.Action.ABOUT_TO_RESCAN,
				null));

		// Inform
		outputPane.log("Scanning user data folder...");

		// Make sure to clear the table of invalid datasets and
		// metadata
		clearInvalidDatasetsTable();
		clearMetadataTable();
		
		// Get the datamover incoming folder from the application properties
		// to which we append the user name to personalize the working space
		Properties appProperties = AppProperties.readPropertiesFromFile();
		File userDataFolder = new File(
				appProperties.getProperty("UserDataDir") +
				File.separator + userName);
		
		// Does the folder exist? If not, we create it. Please mind,
		// if directory creation fails, the application will quit since
		// this is a non-recoverable problem.
		checkAndCreateFolderOrDie(userDataFolder, "user directory");

		// Prepare a new root node for the Tree
		rootNode = new RootNode(new RootDescriptor(userDataFolder));
		
		// We parse the user folder: the actual processing is done
		// by the processor.
		Boolean status = parse(userDataFolder);

		// Create a tree that allows one selection at a time.
		tree.setModel(new DefaultTreeModel(rootNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);
		
		// Clear the metadata table
		clearMetadataTable();
		
		// Set isReady to globalStatus
		isReady = status;
		
		// Inform the user if isReady is false
		if (!isReady) {
			outputPane.err(
					"Please fix the invalid datasets to continue!");
		}
	
		// Notify observers that the scanning is done 
		setChanged();
		notifyObservers(new ObserverActionParameters(
				ObserverActionParameters.Action.SCAN_COMPLETE,
				null));
	}

    /**
     * Climb the JTree to find given parent node of passed one and return
     * it. The required node must be below the root node (which must have
     * name "RootDescriptor". The name of the node is given as follows:
     * 
     * selectedNode.getUserObject().getClass().getSimpleName()
     * 
     * where the user node contained in the selectedNode is usually a
     * Descriptor generated by one of the processors.
     *  
     * @param selectedNode Node selected in the JTree
     * @return desired node name (e.g. Experiment, Folder>
     */
    protected AbstractNode getParentNodeByName(
    		AbstractNode selectedNode, String nodeName) {

        // Get the class name of the selected node
        String className = selectedNode.getUserObject().getClass().getSimpleName();

        // Did we select the root already?
        AbstractNode parentNode = (AbstractNode) selectedNode.getParent();
        
        if (parentNode == null) {
        		// We indeed are at the root
        		return null;
        }
        
        // Did we already select the desired node?
        if (className.equals(nodeName)) {
            // We are at the selected node, we just return it
            return selectedNode;
        } else {
            // We are somewhere below the requested node: we climb up the
            // tree until we find it and then return it
            parentNode = (AbstractNode) selectedNode.getParent();
            while (parentNode != null) {
                // Are we at the folder node?
                if (parentNode.getUserObject().getClass().getSimpleName().equals(nodeName)) {
                    return parentNode;
                } else {
                    parentNode = (AbstractNode) parentNode.getParent();
                }
            }
        }
        return null;
    }	

    /**
	 * Check for the existence of a folder and tries to create it; if
	 * creation fails, the error is considered to be fatal and the 
	 * application quits.
	 * @param folder Full folder name
	 * @param folderName Colloquial folder name (e.g. "User directory")
	 */
	protected void checkAndCreateFolderOrDie(File folder, String folderName) {
		// Some string manipulation
		String allLower = folderName.toLowerCase();
		String firstUpper = allLower.substring(0, 1).toUpperCase() + 
				allLower.substring(1);

		// Does the folder exist? If not, we create it
		if (!folder.exists()) {
			outputPane.warn(firstUpper + " not found! Creating...");
			boolean failed = false;
			try {
				if (folder.mkdirs()) {
					outputPane.log(firstUpper + " created successfully.");
				} else {
					failed = true;
				}
			} catch (Exception e) {
				failed = true;
			}

			// Inform and exit
			if (failed) {
				outputPane.err("Failed creating " + allLower + "!");
				JOptionPane.showMessageDialog(null,
					    "Failed creating " + allLower + "!\n" +
				"Please contact your administrator. The application\n" +
					    		"will now exit!",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}
	}
	
	/**
	 * Return the Tree's data model.
	 */
	public TreeModel getDataModel() {
		return tree.getModel();
	}

	/**
	 * Initialize the Tree. If a Tree already exists, it is cleared
	 * and replaced.
	 */
	protected void clearTree() {

		// Is there already something in the tree?
		if (tree != null) {
			TreeModel model = tree.getModel();
			if (model != null) {
				DefaultMutableTreeNode rootNode = 
						(DefaultMutableTreeNode) model.getRoot();
				if (rootNode != null) {
					rootNode.removeAllChildren();
					((DefaultTreeModel) model).reload();
					rootNode = null;
					System.gc();
				}
			}
		}

		rootNode = new RootNode(new RootDescriptor(new File("/")));
		
		// Create a tree that allows one selection at a time.
		tree.setModel(new DefaultTreeModel(rootNode));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);
		
		// Notify changes
		((DefaultTreeModel) tree.getModel()).reload();
	}

	/**
	 * Clears the JTable listing the invalid datasets
	 */
	protected void clearInvalidDatasetsTable() {
		if (invalidDatasetsTable != null) {
			ReadOnlyTableModel model =
					(ReadOnlyTableModel) invalidDatasetsTable.getModel();
			for (int i = model.getRowCount() - 1; i >= 0; i--) {
			    model.removeRow(i);
			}
		}
	}

	/**
	 * Clears the JTable listing the metadata (attributes)
	 */
	protected void clearMetadataTable() {
		if (metadataViewTable != null) {
			ReadOnlyTableModel model =
					(ReadOnlyTableModel) metadataViewTable.getModel();
			for (int i = model.getRowCount() - 1; i >= 0; i--) {
			    model.removeRow(i);
			}
		}
	}

	/**
	 * Adds all key-value pairs from an attributes Map to the metadata
	 * view table 
	 * @param attributes Map of attributes returned by the various
	 * processors.
	 */
	protected void addAttributesToMetadataTable(Map<String, String> attributes) {
		ReadOnlyTableModel model =
				(ReadOnlyTableModel) metadataViewTable.getModel();
		for (String key: attributes.keySet() ) {
			String value = attributes.get(key);
			model.addRow(new Object[] {key, value});
		}
		// Sort the table by the first column
		metadataViewTable.getRowSorter().toggleSortOrder(0);
	}

	/**
	 * Create and save an XML representation of the JTree to file
	 * @param outputDirectory Directory where XML property files are saved 
	 * @return true if the XML file could be saved, false otherwise
	 */	
	public boolean saveToXML(String outputDirectory) {
		
		DataViewerTreeToXML treeToXML = new DataViewerTreeToXML(tree);
		return (treeToXML.saveToFile(outputDirectory));
	
	}

	/**
	 * Create and save a file containing a map of pointers to the
	 * experiment setting files at the root of the user folder to
	 * be used by the dropbox. The file name is structure.
	 * @param outputDirectory Directory where XML property files are saved 
	 * @return true if the XML file could be saved, false otherwise
	 */	
	public boolean saveDataStructureMap(String outputDirectory) {

		// Is there a root node?
		if (rootNode == null) {
			return false;
		}
		
		// Are there experiments?
		int nExp = rootNode.getChildCount();
		if (nExp == 0) {
			return false;
		}

		// File name
		File fileName = new File(outputDirectory + File.separator +
				"data_structure.ois");

		try {
			
			// Open file
			BufferedWriter bw =
					new BufferedWriter(new FileWriter(fileName, false));

			// Go over all Experiments
			for (int i = 0; i < nExp; i++) {
				ExperimentNode expNode = 
						(ExperimentNode) rootNode.getChildAt(i);
				ExperimentDescriptor expDescr =
						(ExperimentDescriptor)expNode.getUserObject();
				
				// We need Linux-compatible file separators
				String propertiesFile = expDescr.getPropertiesFileNameWithRelPath();
				propertiesFile = propertiesFile.replace("\\", "/");

				// Write them one per line
				bw.write(propertiesFile);
				bw.newLine();
			}

			// Close file 
			bw.close();
			 
		} catch (IOException e) {
			System.err.println("Failed writing to data structure file!");
			return false;
		}

		// Return success
		return true;
	}

	/**
	 * Returns true if the viewer has completed creation of the data model
	 * @return true if the data model is complete, false otherwise
	 */
	public boolean isReady() { return isReady; }
	
	/**
	 * Return the reference to the JPanel to be added to a container component
	 * @return JPanel reference
	 */
	public JPanel getPanel() {
		return panel;
	}
}
