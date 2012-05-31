package ch.eth.scu.scuimporter;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import ch.eth.scu.scuimporter.processor.BDDIVAXMLProcessor;
import ch.eth.scu.scuimporter.processor.BDDIVAXMLProcessor.Experiment;
import ch.eth.scu.scuimporter.processor.BDDIVAXMLProcessor.Experiment.Specimen;
import ch.eth.scu.scuimporter.processor.BDDIVAXMLProcessor.Experiment.Specimen.Tube;
import ch.eth.scu.scuimporter.processor.BDDIVAXMLProcessor.Experiment.Tray;

import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * Simple graphical viewer for the BDDIVAXMLProcessor
 * @author Aaron Ponti
 */
public class BDDIVAXMLViewer extends JPanel
                      implements TreeSelectionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3780551586693882867L;
	private JEditorPane htmlPane;
    private JTree tree;
    
    private BDDIVAXMLProcessor processor;

    public BDDIVAXMLViewer() {
        
    	// Set the layout
    	super(new GridLayout(1,2));

    	// TODO The user should pick the file from a menu
        String filename = "/work/fcs/Kymb 090512 Experiment Export/Kymb 090512.xml";

        // Process the file
		processor = new BDDIVAXMLProcessor(filename);
		processor.parse();
 
        // Create the root node (file name)
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(processor);
        
        // Add all the children
        createNodes(top);

        // Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode(
        		TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        // Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

        // Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        JScrollPane htmlView = new JScrollPane(htmlPane);

        // Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(htmlView);

        Dimension minimumSize = new Dimension(800, 600);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(300); 
        splitPane.setPreferredSize(new Dimension(800, 600));

        // Add split pane to this panel.
        add(splitPane);
    }

    /** Required by TreeSelectionListener interface. */
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
        if (className.endsWith("BDDIVAXMLProcessor")) {
        	htmlPane.setText(
        			((BDDIVAXMLProcessor) 
        					nodeInfo).attributesToString());
        } else if (className.endsWith("Experiment")) { 
        	htmlPane.setText(
        			((BDDIVAXMLProcessor.Experiment) 
        					nodeInfo).attributesToString());
        } else if (className.endsWith("Tray")) { 
        	htmlPane.setText(
        			((BDDIVAXMLProcessor.Experiment.Tray) 
        					nodeInfo).attributesToString());
        } else if (className.endsWith("Specimen")) { 
        	htmlPane.setText(
        			((BDDIVAXMLProcessor.Experiment.Specimen) 
        					nodeInfo).attributesToString());
        } else if (className.endsWith("Tube")) { 
        	htmlPane.setText(
        			((BDDIVAXMLProcessor.Experiment.Specimen.Tube) 
        					nodeInfo).attributesToString());
        } else {
        	htmlPane.setText("No attributes.");
        }
    }
    
    private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode experiment = null;
        DefaultMutableTreeNode tray = null;
        DefaultMutableTreeNode specimen = null;
        DefaultMutableTreeNode tube = null;

        for (Experiment e : processor.experiments) {

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
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
    	// Try to use the system look and feel
        try {
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        	System.err.println("Couldn't use system look and feel.");
        }

        // Create and set up the window.
        JFrame frame = new JFrame("BD DIVA XML File Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add content to the window.
        frame.getContentPane().add(new BDDIVAXMLViewer());

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
