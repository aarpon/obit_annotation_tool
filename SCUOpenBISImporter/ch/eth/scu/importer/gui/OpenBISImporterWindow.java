package ch.eth.scu.importer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import ch.eth.scu.importer.common.properties.AppProperties;
import ch.eth.scu.importer.common.properties.DropboxProperties;
import ch.eth.scu.importer.gui.panels.editors.EditorContainer;
import ch.eth.scu.importer.gui.panels.openbis.OpenBISSpaceViewer;
import ch.eth.scu.importer.gui.panels.viewers.AbstractViewer;
import ch.eth.scu.importer.gui.panels.viewers.BDLSRFortessaViewer;
import ch.eth.scu.importer.gui.panels.viewers.LeicaSP5Viewer;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.util.Properties;

/**
 * Main window of the SCUOpenBISImporter application.
 * @author Aaron Ponti
 */
public class OpenBISImporterWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private EditorContainer editorContainer;
	private JTextArea outputPane; 
	private JScrollPane outputWindow;
	private OpenBISSpaceViewer spaceViewer;
	private AbstractViewer metadataViewer;
	private JToolBar toolBar;
	private DropboxProperties dropboxProperties;
	
	private static final String version = "0.0.0";
	
	/**
	 * Constructor
	 */
	public OpenBISImporterWindow() {

		// Call the frame's constructor
		super("Single-Cell Unit openBIS Importer v" + version);

		// Use the system default look-and-feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't set look and feel.");
		}

		// Get the application properties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		
		// Get the Dropbox properties
		dropboxProperties = new DropboxProperties();
		
		// Add a BorderLayout
		setLayout(new BorderLayout());
		
		// Create a toolbar
		toolBar = new JToolBar("Tools");
		toolBar.setFloatable(false);
		addButtons();
		add(toolBar, BorderLayout.NORTH);

		// Add the viewer
		String acqStation = appProperties.getProperty("AcquisitionStation");	
		if (acqStation.equals("LSRFortessa")) {
			metadataViewer = new BDLSRFortessaViewer();
		} else if (acqStation.equals("LeicaSP5")) {
			metadataViewer = new LeicaSP5Viewer();
		} else {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		}
		add(metadataViewer, BorderLayout.WEST);
		
		// Add the editor
		editorContainer = new EditorContainer();
		add(editorContainer, BorderLayout.CENTER);

		spaceViewer = new OpenBISSpaceViewer(dropboxProperties);
		add(spaceViewer, BorderLayout.EAST);

		// Create the HTML viewing pane.
		outputPane = new JTextArea();
		outputPane.setRows(5);
		outputPane.setEditable(false);
		outputPane.setText("Ready");
		outputWindow = new JScrollPane(outputPane);
		add(outputWindow, BorderLayout.SOUTH);

		// We do not want the window to close without some clean up
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	QuitApplication();
	        }
	    });

		// Set up the frame and center on screen
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		
		// Ask the user to login
		spaceViewer.login();

		// Scan the datamover incoming folder for datasets
		metadataViewer.scan();
		
		// Make window visible
		setVisible(true);
	}

	/**
	 * Implements the actionPerformed callback.
	 * @param e The ActionEvent 
	 */
	public void actionPerformed(ActionEvent e) {

		// React to the context menu
		if (e.getActionCommand().equals("Log in")) {
			spaceViewer.login();
		} else if (e.getActionCommand().equals("Log out")) {
			spaceViewer.logout();
		} else if (e.getActionCommand().equals("Scan")) {
			metadataViewer.scan();
		} else if (e.getActionCommand().equals("Quit")) {
				QuitApplication();
		} else {
			outputPane.append("Action command: " + e.getActionCommand() + 
					" from "  + e.getSource().getClass() + "\n");
			return;
		}
	}

	/**
	 * Add buttons to the toolbar
	 */
	private void addButtons() {

		// Quit
        toolBar.add(createButton("quit.png", "Quit",
        		"Quit", "Quit the application"));

        toolBar.addSeparator();

        // Log in
        toolBar.add(createButton("login.png", "Log in",
        		"Log in", "Log in to openBIS"));

        // Log out
        toolBar.add(createButton("logout.png", "Log out",
        		"Log out", "Log out from openBIS"));

        toolBar.addSeparator();

        // Scan incoming file
        toolBar.add(createButton("fileopen.png", "Scan",
        		"Scan", "Scan data mover incoming folder"));
     }
	
	/**
	 * Create an action button to be added to the toolbar
	 * @param imgLocation		relative path to the image
	 * @param actionCommand		command to be used 
	 * @param toolTipText		tooltip String for the button 
	 * @return a JButton ready to be added to the toolbar
	 */
	private JButton createButton( String imgLocation,
								  String text,
								  String actionCommand,
								  String toolTipText) {

		// Create, initialize and return the button.
		JButton button = new JButton(text);
		button.setMargin(new Insets(3, 3, 3, 3));
		button.setBorderPainted(false);
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
		button.setIcon(new ImageIcon(
					this.getClass().getResource(imgLocation)));
		return button;
	}
	
	/**
	 * Quits the application properly
	 */
	private void QuitApplication() {
        if (JOptionPane.showConfirmDialog(this, 
        		"Do you really want to quit?", "Question",
        		JOptionPane.YES_NO_OPTION,
        		JOptionPane.QUESTION_MESSAGE) == 
        		JOptionPane.YES_OPTION) {
        	spaceViewer.logout();
        	System.exit(0);
        }
   }

}
