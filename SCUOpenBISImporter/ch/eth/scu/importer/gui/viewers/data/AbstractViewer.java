package ch.eth.scu.importer.gui.viewers.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Observable;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import ch.eth.scu.importer.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.gui.viewers.data.view.DataViewerTree;
import ch.eth.scu.importer.gui.viewers.data.view.DataViewerTreeToXML;
import ch.eth.scu.importer.processor.model.RootDescriptor;

/**
 * Abstract viewer for processors
 * @author Aaron Ponti
 */
abstract public class AbstractViewer extends Observable
	implements ActionListener, TreeSelectionListener {

	protected boolean isReady = false;

	protected JPanel panel;
	protected DataViewerTree tree;
	protected JLabel title;
	protected DefaultMutableTreeNode rootNode;
	protected JScrollPane treeView;
	protected JScrollPane htmlView;
	protected JLabel metadataView;
	protected JTable metadataViewTable;
	protected JScrollPane metadataViewPane;	
	protected JLabel invalidDatasets;
	protected JTable invalidDatasetsTable;
	protected JScrollPane invalidDatasetsPane;
	
	/**
	 * Scans the datamover incoming directory for datasets to be processed.
	 * At the end of scanning, the function MUST set isReady to true.
	 */
	abstract public void scan(String userName);
	
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
		panel.add(treeView, constraints);

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
		panel.add(metadataView, constraints);
		
		// Add the table
		Object[][] mdData = { };
		String mdColumnNames[] = { "Name", "Value" };
		metadataViewTable = new JTable(
				new DefaultTableModel(mdData, mdColumnNames));
		metadataViewTable.setFillsViewportHeight(true);
		metadataViewTable.setAutoCreateRowSorter(true);
		metadataViewPane = new JScrollPane(metadataViewTable);
		
		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 3;
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
		constraints.gridy = 4;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		panel.add(invalidDatasets, constraints);
		
		// Add the table
		Object[][] data = { };
		String columnNames[] = { "Folder", "Problem" };
		invalidDatasetsTable = new JTable(
				new DefaultTableModel(data, columnNames));
		invalidDatasetsTable.setFillsViewportHeight(true);
		invalidDatasetsTable.setAutoCreateRowSorter(true);
		invalidDatasetsPane = new JScrollPane(invalidDatasetsTable);
        
		// Add to the layout
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.weightx = 1.0;
		constraints.weighty = 0.05;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		panel.add(invalidDatasetsPane, constraints);
		
		// Set the preferred and minimum size
		panel.setMinimumSize(new Dimension(400, 700));
		panel.setPreferredSize(new Dimension(450, 700));

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
