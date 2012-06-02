package ch.eth.scu.scuimporter.gui;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import ch.eth.scu.scuimporter.processor.BDFACSDIVAXMLProcessor;
import ch.eth.scu.scuimporter.processor.BDFACSDIVAXMLProcessor.Experiment;
import ch.eth.scu.scuimporter.processor.BDFACSDIVAXMLProcessor.Experiment.Specimen;
import ch.eth.scu.scuimporter.processor.BDFACSDIVAXMLProcessor.Experiment.Specimen.Tube;
import ch.eth.scu.scuimporter.processor.BDFACSDIVAXMLProcessor.Experiment.Tray;
import ch.eth.scu.scuimporter.processor.FCSProcessor;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;

/**
 * Simple graphical viewer for the BDDIVAXMLProcessor
 * @author Aaron Ponti
 */
public class BDLSRFortessaViewer extends JPanel
                      implements TreeSelectionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JEditorPane htmlPane;
    private JTree tree;
    private File file;
    private DefaultMutableTreeNode rootNode;
    private JScrollPane treeView;
    private JScrollPane htmlView;
    private JSplitPane splitPane;
    private JPopupMenu popupMenu;
    
    private BDFACSDIVAXMLProcessor xmlprocessor = null;
    private FCSProcessor fcsprocessor = null;
    
    private final Action openAction = new OpenAction();

    public BDLSRFortessaViewer() {

    	// Add a layout manager
    	setLayout(new GridLayout(1,1));
    	
        // Create the root node
        rootNode = new DefaultMutableTreeNode("Right-click to pick a file...");
        
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
        
        // Add popup menu with Open file action
        popupMenu = new JPopupMenu();
        addPopup(tree, popupMenu);
        JMenuItem mntmOpenFile = new JMenuItem("Open file...");
        mntmOpenFile.setAction(openAction);
        popupMenu.add(mntmOpenFile);
        
        // Add initial info to the html pane
        htmlPane.setText("\nDisplays 'BD BioSciences FACSDiva\u2122 Software' " +
        		"XML files with the associated 'Data File Standard " + 
        		"for Flow Cytometry, Version FCS3.0' files generated " +
        		"by the BD LSRFortessa flow cytometer." );
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
		if (xmlprocessor.parse() == false ) {
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
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
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

        // Create and set up the window.
        JFrame frame = new JFrame("BD LSRFortessa Experiment Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add content to the window.
        frame.getContentPane().add(new BDLSRFortessaViewer());

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
    
    // Add a pop-up menu
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	// Open file action
	private class OpenAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public OpenAction() {
			putValue(NAME, "Open file...");
			putValue(SHORT_DESCRIPTION, 
					"Open and parse a BD LSR Fortessa files.");
		}
		
		// User chose Open file on the context menu
		public void actionPerformed(ActionEvent e) {
			
			// React to the context menu
			if (!e.getActionCommand().equals("Open file...")) {
				return;
			}
			
			// Create a file chooser
			final JFileChooser fc = new JFileChooser();
			
			// Filters
			FileFilter filter = new FileNameExtensionFilter(
					"BD LSRFortessa files (*.xml, *.fcs)", "xml", "fcs" );
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
		}
	}
}
