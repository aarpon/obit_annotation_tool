package ch.eth.scu.importer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import ch.eth.scu.importer.gui.editors.data.EditorContainer;
import ch.eth.scu.importer.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.gui.viewers.data.ViewerFactory;
import ch.eth.scu.importer.gui.viewers.openbis.OpenBISViewer;

/**
 * Main window of the SCUOpenBISImporter application.
 * @author Aaron Ponti
 */
public class OpenBISImporterWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private EditorContainer editorContainer;
	private JTextArea outputPane; 
	private JScrollPane outputWindow;
	private OpenBISViewer openBISViewer;
	private AbstractViewer metadataViewer;
	private JToolBar toolBar;
	
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

		// Add a BorderLayout
		setLayout(new BorderLayout());
		
		// Create a toolbar
		toolBar = new JToolBar("Tools");
		toolBar.setFloatable(false);
		addButtons();
		add(toolBar, BorderLayout.NORTH);

		// Add the viewer
		try {
			metadataViewer = ViewerFactory.createViewer();
		} catch (Exception e1) {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		} 
		add(metadataViewer.getPanel(), BorderLayout.WEST);
		
		openBISViewer = new OpenBISViewer();
		add(openBISViewer.getPanel(), BorderLayout.EAST);

		// Create the HTML viewing pane.
		outputPane = new JTextArea();
		outputPane.setRows(5);
		outputPane.setEditable(false);
		outputPane.setText("Ready");
		outputWindow = new JScrollPane(outputPane);
		add(outputWindow, BorderLayout.SOUTH);

		// Add the editor: it is important to create this object as the last
		// one, since it requires non-null references to both the metadata and
		// the openBIS viewers!
		editorContainer = new EditorContainer(metadataViewer, openBISViewer);
		add(editorContainer, BorderLayout.CENTER);

		// Add observers to the viewers
		metadataViewer.addObserver(editorContainer.getEditor());
		openBISViewer.addObserver(editorContainer.getEditor());
		
		// We do not want the window to close without some clean up
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	QuitApplication();
	        }
	    });

		// Set up the frame and center on screen
		setMinimumSize(new Dimension(1250, 700));
		setMaximumSize(new Dimension(1500, 1200));
		pack();
		setLocationRelativeTo(null);
		
		// Ask the user to login.
		// Here we will insist on getting valid openBIS credentials, since a
		// valid login is essential for the functioning of the application.
		// If the user justs closes the dialog, we close the application.
		boolean status = false;
		while (!status) {
			status = openBISViewer.login();
		}

		// Now we can scan the openBIS instance
		openBISViewer.scan();

		// Scan the datamover incoming folder for datasets
		metadataViewer.scan(openBISViewer.getUserName());

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
			boolean status = openBISViewer.login();
			if (status) {
				openBISViewer.scan();
			}
		} else if (e.getActionCommand().equals("Log out")) {
			openBISViewer.logout();
		} else if (e.getActionCommand().equals("Scan")) {
			metadataViewer.scan(openBISViewer.getUserName());
		} else if (e.getActionCommand().equals("Quit")) {
				QuitApplication();
		} else {
			outputPane.append("Action command: " + e.getActionCommand() + 
					" from "  + e.getSource().getClass() + "\n");
		}
	}

	/**
	 * Add buttons to the toolbar
	 */
	private void addButtons() {

		// Quit
        toolBar.add(createButton("./icons/quit.png", "Quit",
        		"Quit", "Quit the application"));

        toolBar.addSeparator();

        // Log in
        toolBar.add(createButton("./icons/login.png", "Log in",
        		"Log in", "Log in to openBIS"));

        // Log out
        toolBar.add(createButton("./icons/logout.png", "Log out",
        		"Log out", "Log out from openBIS"));

        toolBar.addSeparator();

        // Scan incoming file
        toolBar.add(createButton("./icons/scan.png", "Scan",
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
        	openBISViewer.logout();
        	System.exit(0);
        }
   }

}
