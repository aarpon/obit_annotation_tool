package ch.eth.scu.scuimporter.gui.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 * Abstract viewer for processors
 * @author Aaron Ponti
 */
abstract public class AbstractViewer extends JPanel
	implements ActionListener, TreeSelectionListener {

	protected static final long serialVersionUID = 1L;
	protected JEditorPane htmlPane;
	protected JTree tree;
	protected JLabel title;
	protected DefaultMutableTreeNode rootNode;
	protected JScrollPane treeView;
	protected JScrollPane htmlView;
	protected JLabel metadataView;
	
	/**
	 * Create the nodes for the tree
	 * @param top Root node
	 */
	protected abstract void createNodes(DefaultMutableTreeNode top);

	/**
	 * Implement the actionPerformed method.
	 * @param e An ActionEvent
	 */
	abstract public void actionPerformed(ActionEvent e);
	
	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	abstract public void valueChanged(TreeSelectionEvent e); 
	
	/**
	 * Asks the user to pick a file to be parsed
	 */
	abstract public void pickFile();

	/**
	 * Asks the user to pick a directory to be processed
	 */
	abstract public void pickDir();
	
	/**
	 * Constructor
	 * The constructor creates the actual panel to be displayed on the UI.
	 */
	public AbstractViewer() {
		
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
		htmlPane.setText("");
		
		// Set sizes
		setMinimumSize(new Dimension(400, 800));
		setPreferredSize(new Dimension(400, 800));
	}
}