package ch.eth.scu.importer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import ch.eth.scu.importer.gui.panels.AbstractViewer;
import ch.eth.scu.importer.gui.panels.BDLSRFortessaViewer;
import ch.eth.scu.importer.gui.panels.LeicaSP5Viewer;
import ch.eth.scu.importer.gui.panels.MetadataEditor;
import ch.eth.scu.importer.gui.panels.OpenBISSpaceViewer;
import ch.eth.scu.importer.properties.AppProperties;

import java.awt.BorderLayout;
import java.util.Properties;

/**
 * Main window of the SCUOpenBISImporter application.
 * @author Aaron Ponti
 *
 */
public class OpenBISImporterWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private MetadataEditor metadataEditor;
	private JEditorPane outputPane; 
	private JScrollPane outputWindow;
	private OpenBISSpaceViewer spaceViewer;
	private AbstractViewer metadataViewer;
	private JToolBar toolBar;
	
	private static final String version = "0.0.0";
	
	/**
	 * Constructor
	 */
	public OpenBISImporterWindow() {

		// Call the frame's constructor
		super("Single-Cell Unit openBIS Importer v" + version);

		// Try to use the cross-platform look and feel
		try {
			if (System.getProperty("os.name").equals("Mac OS X")) {
				UIManager.setLookAndFeel(
						ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel()
		        );
			} else {
				UIManager.setLookAndFeel(
						UIManager.getCrossPlatformLookAndFeelClassName());
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
				UIManager.setLookAndFeel(new MetalLookAndFeel());
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		} catch (Exception e) {
			System.err.println("Couldn't set look and feel.");
		}

		// Get the application properties
		Properties appProperties = AppProperties.readPropertiesFromFile();
		String acqStation = appProperties.getProperty("AcquisitionStation");
		
		// Add a BorderLayout
		setLayout(new BorderLayout());
		
		// Create a toolbar
		toolBar = new JToolBar("Tools");
		toolBar.setFloatable(false);
		addButtons();
		add(toolBar, BorderLayout.NORTH);
		
		if (acqStation.equals("LSRFortessa")) {
			metadataViewer = new BDLSRFortessaViewer();
		} else if (acqStation.equals("LeicaSP5")) {
			metadataViewer = new LeicaSP5Viewer();
		} else {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		}
		add(metadataViewer, BorderLayout.WEST);
		
		metadataEditor = new MetadataEditor();
		add(metadataEditor, BorderLayout.CENTER);

		spaceViewer = new OpenBISSpaceViewer();
		add(spaceViewer, BorderLayout.EAST);

		// Create the HTML viewing pane.
		outputPane = new JEditorPane();
		outputPane.setEditable(false);
		outputPane.setText("Ready");
		outputWindow = new JScrollPane(outputPane);
		add(outputWindow, BorderLayout.SOUTH);

		// Set exit on close
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Make sure to logout when exiting 
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	spaceViewer.logout();
	        	System.exit(0);
	        }
	    });

		// Set up the frame and center on screen
		//setMinimumSize(new Dimension(1200, 800));
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		
		// Ask the user to login
		spaceViewer.login();

		// Make window visible
		setVisible(true);
	}

	/**
	 *  Implements the actionPerformed callback.
	 *  @param ActionEvent
	 */
	public void actionPerformed(ActionEvent e) {

		// React to the context menu
		if (e.getActionCommand().equals("Log in")) {
			spaceViewer.login();
		} else if (e.getActionCommand().equals("Log out")) {
			spaceViewer.logout();
		} else if (e.getActionCommand().equals("Pick file")) {
			metadataViewer.pickFile();
		} else if (e.getActionCommand().equals("Pick directory")) {
			System.out.println("Not implemented.");
		} else if (e.getActionCommand().equals("Quit")) {
	        if (JOptionPane.showConfirmDialog(this, 
	        		"Do you really want to quit?", "Question",
	        		JOptionPane.YES_NO_OPTION,
	        		JOptionPane.QUESTION_MESSAGE) == 
	        		JOptionPane.YES_OPTION) {
	        	spaceViewer.logout();
	        	System.exit(0);
	        }
		} else {
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

        // Pick file
        toolBar.add(createButton("fileopen.png", "Pick file",
        		"Pick file", "Pick a file to add to openBIS"));

        // Log out
        toolBar.add(createButton("diropen.png", "Pick dir",
        		"Log directory", "Pick a directory to add to openBIS"));
     }
	
	/**
	 * Create an action button to be added to the toolbar
	 * @param imgLocation		relative path to the image
	 * @param actionCommand		command to be used 
	 * @param toolTipText		tooltip String for the button 
	 * @return a JButton ready to be added to the toolbar
	 */
	private JButton createButton(	String imgLocation,
									String text,
									String actionCommand,
									String toolTipText) {

		// Create, initialize and the button.
		JButton button = new JButton(text);
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
		button.setIcon(new ImageIcon(
					this.getClass().getResource(imgLocation)));
		return button;
	}

}
