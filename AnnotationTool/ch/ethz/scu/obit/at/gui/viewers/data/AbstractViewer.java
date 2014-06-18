package ch.ethz.scu.obit.at.gui.viewers.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import ch.ethz.scu.obit.at.datamover.ATDataMover;
import ch.ethz.scu.obit.at.gui.pane.OutputPane;
import ch.ethz.scu.obit.at.gui.viewers.ObserverActionParameters;
import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.at.gui.viewers.data.model.ExperimentNode;
import ch.ethz.scu.obit.at.gui.viewers.data.model.RootNode;
import ch.ethz.scu.obit.at.gui.viewers.data.view.DataViewerTree;
import ch.ethz.scu.obit.at.gui.viewers.data.view.DataViewerTreeToXML;
import ch.ethz.scu.obit.common.settings.UserSettingsManager;
import ch.ethz.scu.obit.common.utils.QueryOS;
import ch.ethz.scu.obit.processors.data.model.ExperimentDescriptor;
import ch.ethz.scu.obit.processors.data.model.RootDescriptor;

/**
 * Abstract viewer for processors
 * @author Aaron Ponti
 */
abstract public class AbstractViewer extends Observable
	implements ActionListener, TreeSelectionListener {

	// The valueChanged() method is fired twice when selection is changed  
	// in a JTree, so we keep track of the last processed node to avoid 
	// doing the work twice every time the node is changed.
	protected AbstractNode lastSelectedNode;
	
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
	protected JButton scanButton;
	protected JLabel metadataView;
	protected JTable metadataViewTable;
	protected JScrollPane metadataViewPane;	
	protected JLabel invalidDatasets;
	protected JTable invalidDatasetsTable;
	protected File invalidDataset;
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
		title = new JLabel("<html><b>Data viewer</b></html>");

		// Add the tree viewer to the layout
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.insets = new Insets(0, 0, 5, 5);
		panel.add(title, constraints);
		
		// Create the Tree
		rootNode = new RootNode(new RootDescriptor(new File("/")));
		
		// Create a tree that allows one selection at a time.
		tree = new DataViewerTree(rootNode);
		
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
		constraints.insets = new Insets(5, 0, 0, 5);
		panel.add(treeView, constraints);

		// Add a rescan button
		scanButton = new JButton("Scan");
		scanButton.addActionListener(new ActionListener() {
 
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
		constraints.insets = new Insets(5, 0, 5, 5);
		panel.add(scanButton, constraints);
		
		// Add a simple label
		metadataView = new JLabel("<html><b>Metadata viewer</b></html>");
		metadataView.setVerticalAlignment(SwingConstants.TOP);

		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 0, 5, 5);
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
		constraints.insets = new Insets(5, 0, 5, 5);
		panel.add(metadataViewPane, constraints);

		// Add a simple label
		invalidDatasets = new JLabel("<html><b>Invalid datasets</b></html>");
		invalidDatasets.setVerticalAlignment(SwingConstants.TOP);

		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 0, 5, 5);
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
        
		// Add a context menu
		invalidDatasetsTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (QueryOS.isWindows()) {
					return;
				}
				setListenerOnJTable(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (QueryOS.isMac()) {
					return;
				}
				setListenerOnJTable(e);
			}
		});

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

		// Disable the "scan" button
		scanButton.setEnabled(false);

		// Clear the tree
		clearTree();

		// Notify observers that the scanning is about to start 
		synchronized (this) {
			setChanged();
			notifyObservers(new ObserverActionParameters(
					ObserverActionParameters.Action.ABOUT_TO_RESCAN, null));
		}
		
		// Inform
		outputPane.log("Scanning user data folder...");

		// Make sure to clear the table of invalid datasets and
		// metadata
		clearInvalidDatasetsTable();
		clearMetadataTable();
		
		// Get the datamover incoming folder from the application properties
		// to which we append the user name to personalize the working space
		UserSettingsManager manager = new UserSettingsManager();
		if (! manager.load()) {
			JOptionPane.showMessageDialog(null,
					"Could not read application settings!\n" +
			"Please contact your administrator. The application\n" +
			"will now exit!",
			"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		File userDataFolder = new File(
				manager.getSettingValue("UserDataDir") +
				File.separator + userName);
		
		// Does the folder exist? If not, we create it. Please mind,
		// if directory creation fails, the application will quit since
		// this is a non-recoverable problem.
		checkAndCreateFolderOrDie(userDataFolder, "user directory");

		// Prepare a new root node for the Tree
		rootNode = new RootNode(new RootDescriptor(userDataFolder));
		
		// Then define and start the worker
		class Worker extends SwingWorker<Boolean, Void> {

			final private File userDataFolder;
			final private AbstractViewer ref;

			/**
			 * Constructor
			 * 
			 * @param userDataFolder Folder to be parsed.
			 # @param ref AbstractViewer reference.
			 */
			public Worker(File userDataFolder, AbstractViewer ref) {
				this.userDataFolder = userDataFolder;
				this.ref = ref;
			}

			@Override
			public Boolean doInBackground() {

				// We parse the user folder: the actual processing is done
				// by the processor.
				return (parse(userDataFolder));

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

				// Create a tree that allows one selection at a time.
				tree.setModel(new DefaultTreeModel(rootNode));

				// Listen for when the selection changes.
				tree.addTreeSelectionListener(ref);

				// Clear the metadata table
				clearMetadataTable();

				// Set isReady to globalStatus
				isReady = status;

				// Inform the user if isReady is false
				if (!isReady) {
					outputPane
							.err("Please fix the invalid datasets to continue!");
				}

				// Notify observers that the scanning is done
				synchronized (this) {
					setChanged();
					notifyObservers(new ObserverActionParameters(
							ObserverActionParameters.Action.SCAN_COMPLETE, null));
				}

				// Re-enable the "scan" button
				scanButton.setEnabled(true);

				// Inform
				outputPane.log("Scanning user data folder completed.");

			}

		}
		;
 
        // Run the worker!
        (new Worker(userDataFolder, this)).execute();
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

		// Create a RootNode
		rootNode = new RootNode(new RootDescriptor(new File("/")));

		// Create a tree that allows one selection at a time.
		tree.setModel(new DefaultTreeModel(rootNode));
		
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

		// Sort the table ascending by the first column
		TableRowSorter<? extends TableModel> rowSorter = 
				(TableRowSorter<? extends TableModel>)
				metadataViewTable.getRowSorter();
		List<SortKey> keys = new ArrayList<SortKey>();
		SortKey sortKey = new SortKey(0, SortOrder.ASCENDING);
		keys.add(sortKey);
		rowSorter.setSortKeys(keys);
		rowSorter.sort();
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

	/**
	 * Create a popup menu with actions for handling invalid datasets
	 * @return a JPopupMenu for the passed item
	 */
	private JPopupMenu createInvalidDatasetsPopup() {
		
		// Create the popup menu.
	    JPopupMenu popup = new JPopupMenu();

	    // Show in Explorer/Finder
	    String menuEntry = "";
	    if (QueryOS.isWindows()) {
	    		menuEntry = "Show in Windows Explorer";
	    } else if (QueryOS.isMac()) {
	    		menuEntry = "Show in Finder";
	    } else {
			throw new UnsupportedOperationException(
					"Operating system not supported.");
	    }
	    JMenuItem menuItem = new JMenuItem(menuEntry);
	    menuItem.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	// Build the full path of the invalid dataset
            	UserSettingsManager manager = new UserSettingsManager();
        		if (! manager.load()) {
        			JOptionPane.showMessageDialog(null,
        					"Could not read application settings!\n" +
        			"Please contact your administrator. The application\n" +
        			"will now exit!",
        			"Error", JOptionPane.ERROR_MESSAGE);
        			System.exit(1);
        		}
        		File userDataFolder = new File(
        				manager.getSettingValue("UserDataDir"));

            	// Full path to the invalid dataset
				File fullPath = new File(userDataFolder +
						File.separator + invalidDataset);

				String fullPathStr;
				try {
					fullPathStr = fullPath.getCanonicalPath();
				} catch (IOException e1) {
					outputPane.err("Could not retrieve full path " +
							"of selected invalid dataset!");
					return;
				}

				// Command arguments
            	String command = "";
            	String commandName = "";
            	String commandArgument = "";
				if (QueryOS.isMac()) {
            		command = "open";
            		commandName = "Finder";
					commandArgument = "-R";
				} else if (QueryOS.isWindows()) {
            		command = "Explorer.exe";
            		commandName = "Windows Explorer";
					commandArgument = "/select,";
				} else {
            		throw new UnsupportedOperationException(
            				"Operating system not supported.");
            	} 

				// Inform
				outputPane.log("Showing invalid dataset \"" +
						invalidDataset + "\" in " + commandName);

				// Execute the command
	        	String [] commandArray = new String[3];
	        	commandArray[0] = command;
	        	commandArray[1] = commandArgument;
	        	commandArray[2] = fullPathStr;
	        	try {
					Process p = Runtime.getRuntime().exec(commandArray);
					p.waitFor();
				} catch (IOException e1) {
					outputPane.err("Could not show invalid dataset "
							+ "in " + commandName + "!");
				} catch (InterruptedException e1) {
					outputPane.err("Could not show invalid dataset "
							+ "in " + commandName + "!");
				}

			}
        });

	    // Move to...
	    popup.add(menuItem);
	    menuItem = new JMenuItem("Move to...");
	    menuItem.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	// Create a file chooser
            	final JFileChooser f = new JFileChooser();
            	f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            	int val = f.showDialog(null, "Move to");
            	if (val != JFileChooser.APPROVE_OPTION) {
            		return;
            	}
            	
            	// Build the full path of the file/folder to move
            	UserSettingsManager manager = new UserSettingsManager();
        		if (! manager.load()) {
        			JOptionPane.showMessageDialog(null,
        					"Could not read application settings!\n" +
        			"Please contact your administrator. The application\n" +
        			"will now exit!",
        			"Error", JOptionPane.ERROR_MESSAGE);
        			System.exit(1);
        		}
        		File userDataFolder = new File(
        				manager.getSettingValue("UserDataDir"));
            	File fullPath = new File(userDataFolder +
        				File.separator + invalidDataset);
            	
            	// Build full target
            	File fullTarget = new File(
            			f.getSelectedFile() + File.separator +
            			fullPath.getName());
            	
            	// Move
            	outputPane.log("Moving \"" + fullPath + "\" to \"" +
            			fullTarget + "\"");
                boolean isFile = fullPath.isFile();
                if (! fullPath.renameTo(fullTarget)) {
                    if (isFile) {
                        JOptionPane.showMessageDialog(null,
                                "Could not move \"" + fullPath + "\"!\n\n" +
                                        "Please make sure that the file is not " +
                                        "open in some application.");
                    } else {
                        JOptionPane.showMessageDialog(null,
                            "Could not move \"" + fullPath + "\"!\n\n" +
                            "Please make sure that the folder is not open in " +
                            "the file manager or that any of the contained\n" +
                            "files are not open in some application.");
                    }
                } else {
                    outputPane.warn("Please rescan the data folder when you have " +
                            "fixed all invalid datasets!");
                }

            }
        });
	    popup.add(menuItem);

	    // Delete
	    menuItem = new JMenuItem("Delete");
	    menuItem.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	// Build the full path of the file/folder to delete
            	UserSettingsManager manager = new UserSettingsManager();
        		if (! manager.load()) {
        			JOptionPane.showMessageDialog(null,
        					"Could not read application settings!\n" +
        			"Please contact your administrator. The application\n" +
        			"will now exit!",
        			"Error", JOptionPane.ERROR_MESSAGE);
        			System.exit(1);
        		}
        		File userDataFolder = new File(
        				manager.getSettingValue("UserDataDir"));
            	File fullPath = new File(userDataFolder +
        				File.separator + invalidDataset);
            	
            	// Ask the user for confirmation
                if (JOptionPane.showConfirmDialog(null, 
                		"Are you sure you want to delete\n\"" +
                				invalidDataset + "\" ?", 
                				"Question",
                		JOptionPane.YES_NO_OPTION,
                		JOptionPane.QUESTION_MESSAGE) == 
                		JOptionPane.YES_OPTION) {
                	
                	// Delete
                	outputPane.log("Deleting \"" + fullPath + "\"");
                    boolean isFile = fullPath.isFile();
                	if (!ATDataMover.deleteRecursively(fullPath)) {
                        if (isFile) {
                            JOptionPane.showMessageDialog(null,
                                    "Could not delete \"" + fullPath + "\"!\n\n" +
                                            "Please make sure that the file is not " +
                                            "open in some application.");
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Could not delete \"" + fullPath + "\"!\n\n" +
                                            "Please make sure that the folder is not open in " +
                                            "the file manager or that any of the contained\n" +
                                            "files are not open in some application.");
                        }
                    } else {
                        outputPane.warn("Please rescan the data folder when you have " +
                                "fixed all invalid datasets!");
                    }
                
                }
            }
        });
	    popup.add(menuItem);
	
	    return popup;
	}


	/**
	 * Sets a mouse event listener on the JTable
	 * @param e Mouse event
	 */
	private void setListenerOnJTable(MouseEvent e) {
		invalidDataset = null;
        int r = invalidDatasetsTable.rowAtPoint(e.getPoint());
        if (r >= 0 && r < invalidDatasetsTable.getRowCount()) {
        		invalidDatasetsTable.setRowSelectionInterval(r, r);
        } else {
        		invalidDatasetsTable.clearSelection();
        }

        // Store the selected file
        int rowIndex = invalidDatasetsTable.getSelectedRow();
        if (rowIndex < 0)
            return;
        if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
	        invalidDataset = new File((String)
	        		invalidDatasetsTable.getModel().getValueAt(
	        				rowIndex, 0));
        		JPopupMenu popup = createInvalidDatasetsPopup();
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
	}
}
