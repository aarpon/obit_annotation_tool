package ch.eth.scu.importer.at.gui.viewers.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;
import java.util.Observable;

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
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import ch.eth.scu.importer.at.gui.pane.OutputPane;
import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.at.gui.viewers.data.view.DataViewerTree;
import ch.eth.scu.importer.at.gui.viewers.data.view.DataViewerTreeToXML;
import ch.eth.scu.importer.processors.model.RootDescriptor;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.RootNode;

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
	protected AbstractNode lastSelectedFolder;
	
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
	 * Scans the datamover incoming directory for datasets to be processed.
	 * At the end of scanning, the function MUST set isReady to true.
	 * setUserName() MUST be called before scan().
	 * 
	 * @see setUserName
	 */
	abstract public void scan();
	
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
		
		// Initialize the Tree
		clearTree();
		
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
		constraints.insets = new Insets(0, 0, 0, 0);
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
				new DefaultTableModel(mdData, mdColumnNames));
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
		String columnNames[] = { "File or folder", "Problem" };
		invalidDatasetsTable = new JTable(
				new DefaultTableModel(data, columnNames));
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
			try {
				folder.mkdirs();
				outputPane.log(firstUpper + " created successfully.");
			} catch (Exception e) {
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

		// Create the root node
		rootNode = new RootNode(new RootDescriptor("/"));

		// Create a tree that allows one selection at a time.
		tree = new DataViewerTree(rootNode);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);
	}

	/**
	 * Clears the JTable listing the invalid datasets
	 */
	protected void clearInvalidDatasetsTable() {
		if (invalidDatasetsTable != null) {
			DefaultTableModel model =
					(DefaultTableModel) invalidDatasetsTable.getModel();
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
			DefaultTableModel model =
					(DefaultTableModel) metadataViewTable.getModel();
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
		DefaultTableModel model =
				(DefaultTableModel) metadataViewTable.getModel();
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
