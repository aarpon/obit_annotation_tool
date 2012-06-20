package ch.eth.scu.scuimporter.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import ch.eth.scu.scuimporter.gui.panels.BDLSRFortessaViewer;
import ch.eth.scu.scuimporter.gui.panels.OpenBISSpaceViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Graphical user interface to log in to openBIS and choose where to store
 * acquired datasets.
 * @author Aaron Ponti
 *
 */
public class OpenBISImporter extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JLabel metadataEditor;
	private JEditorPane outputPane; 
	private JScrollPane outputWindow;
	private OpenBISSpaceViewer spaceViewer;
	private BDLSRFortessaViewer fortessaViewer;
	private JToolBar toolBar;
	
	/**
	 * Constructor
	 */
	public OpenBISImporter() {

		// Call the frame's constructor
		super("Single-Cell Unit openBIS importer");

		// Try to use the cross-platform look and feel
		try {
			if (System.getProperty("os.name").equals("Mac OS X")) {
				UIManager.setLookAndFeel(
						ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel()
		        );
			} else {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
				UIManager.setLookAndFeel(new MetalLookAndFeel());
			}
		} catch (Exception e) {
			System.err.println("Couldn't set look and feel.");
		}

		// Add a BorderLayout
		setLayout(new BorderLayout());
		
		// Create a toolbar
		toolBar = new JToolBar("Tools");
		toolBar.setFloatable(false);
		addButtons();
		add(toolBar, BorderLayout.NORTH);
		
		fortessaViewer = new BDLSRFortessaViewer();
		add(fortessaViewer, BorderLayout.WEST);
		
		metadataEditor = new JLabel("Metadata editor");
		metadataEditor.setVerticalAlignment(SwingConstants.TOP);
		metadataEditor.setMinimumSize(new Dimension(400,600));
		metadataEditor.setPreferredSize(new Dimension(400,600));
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
	 *  User chose Open or Quit on the context menu
	 */
	public void actionPerformed(ActionEvent e) {

		// React to the context menu
		if (e.getActionCommand().equals("Log in")) {
			spaceViewer.login();
		} else if (e.getActionCommand().equals("Log out")) {
			spaceViewer.logout();
		} else if (e.getActionCommand().equals("Pick file")) {
			fortessaViewer.pickFile();
		} else if (e.getActionCommand().equals("Pick directory")) {
			System.out.println("Not implemented.");
		} else if (e.getActionCommand().equals("Quit")) {
	        if (JOptionPane.showConfirmDialog(this, 
	        		"Do you really want to quit?", "Question",
	        		JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
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
        toolBar.add(createButton("resources/quit.png", "Quit",
        		"Quit", "Quit the application"));

        toolBar.addSeparator();

        // Log in
        toolBar.add(createButton("resources/login.png", "Log in",
        		"Log in", "Log in to openBIS"));

        // Log out
        toolBar.add(createButton("resources/logout.png", "Log out",
        		"Log out", "Log out from openBIS"));

        toolBar.addSeparator();

        // Pick file
        toolBar.add(createButton("resources/fileopen.png", "Pick file",
        		"Pick file", "Pick a file to add to openBIS"));

        // Log out
        toolBar.add(createButton("resources/diropen.png", "Pick dir",
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

	/**
	 * Program entry point
	 * @param args Ignored
	 */
	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new OpenBISImporter();
			}
		});
	}
}
