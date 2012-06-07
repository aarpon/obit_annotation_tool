package ch.eth.scu.scuimporter.gui;

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
public class BDLSRFortessaViewer extends JFrame
	implements ActionListener, TreeSelectionListener {

	private static final long serialVersionUID = 1L;
	private JEditorPane htmlPane;
	private JTree tree;
	private File file;
	private DefaultMutableTreeNode rootNode;
	private JScrollPane treeView;
	private JScrollPane htmlView;
	private JSplitPane splitPane;

	private BDFACSDIVAXMLProcessor xmlprocessor = null;
	private FCSProcessor fcsprocessor = null;

	/**
	 * Constructor
	 */
	public BDLSRFortessaViewer() {
		
		super("BD LSRFortessa Experiment Viewer");
		
		// Add a layout manager
		setLayout(new GridLayout(1,1));

		// Try to use the system look and feel
		try {
			if (System.getProperty("os.name").equals("Mac OS X")) {
				UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
			} else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		} catch (Exception e) {
			System.err.println("Couldn't set system look and feel.");
		}
		
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

		// Create the HTML viewing pane.
		htmlPane = new JEditorPane();
		htmlPane.setEditable(false);
		htmlView = new JScrollPane(htmlPane);

		// Add the scroll panes to a split pane.
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setTopComponent(treeView);
		splitPane.setBottomComponent(htmlView);

		// Set sizes
		setPreferredSize(new Dimension(1000, 600));
		htmlView.setMinimumSize(new Dimension(700, 600));
		treeView.setMinimumSize(new Dimension(300, 600));
		splitPane.setDividerLocation(300); 
		splitPane.setPreferredSize(new Dimension(1000, 600));

		// Add split pane to this panel.
		add(splitPane);

		// Add menu
		JMenu menu = new JMenu("File");
		menu.add(makeMenuItem("Open"));
		menu.add(makeMenuItem("Quit"));
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);
		setJMenuBar(menuBar);

		// Add initial info to the html pane
		htmlPane.setText("\nDisplays 'BD BioSciences FACSDiva\u2122 Software' " +
				"XML files with the associated 'Data File Standard " + 
				"for Flow Cytometry, Version FCS3.0' files generated " +
				"by the BD LSRFortessa flow cytometer.");
		
		// Set default close operation 
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Adapt size and make visible 
		pack();
		setVisible(true);

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
		} else if (e.getActionCommand().equals("Quit")) {
			System.exit(0);
		} else {
			return;
		}
	}

	/**
	 * Create menu entries
	 * @param String to be displayed for the menu entry
	 * @return a JMenuItem to be added to the menubar
	 */
	private JMenuItem makeMenuItem(String name) {
		JMenuItem m = new JMenuItem(name);
		m.setActionCommand(name);
		m.addActionListener(this);
		return m;
	}
	
	/**
	 * Program entry point
	 * @param args Ignored
	 */
	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new BDLSRFortessaViewer();
			}
		});
	}

}
