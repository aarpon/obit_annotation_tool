package ch.eth.scu.importer.at.gui;

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
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.springframework.remoting.RemoteAccessException;

import ch.eth.scu.importer.at.gui.editors.data.EditorContainer;
import ch.eth.scu.importer.at.gui.pane.OutputPane;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.data.ViewerFactory;
import ch.eth.scu.importer.at.gui.viewers.openbis.OpenBISViewer;

/**
 * Main window of the AnnotationTool application.
 * @author Aaron Ponti
 */
public class AnnotationToolWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private EditorContainer editorContainer;
	private OutputPane outputPane; 
	private JScrollPane outputWindow;
	private OpenBISViewer openBISViewer;
	private AbstractViewer metadataViewer;
	private JToolBar toolBar;
	
	private static final String version = "0.0.0";
	
	/**
	 * Constructor
	 */
	public AnnotationToolWindow() {

		// Call the frame's constructor
		super("openBIS Importer Toolset :: Annotation Tool v" + version);

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
		// TODO: Add tools...
		add(toolBar, BorderLayout.NORTH);

		// Add the metadata viewer
		try {
			metadataViewer = ViewerFactory.createViewer();
		} catch (Exception e1) {
			System.err.println("Unknown acquisition station! Aborting.");
			System.exit(1);
		} 
		add(metadataViewer.getPanel(), BorderLayout.WEST);

		// Create the HTML viewing pane.
		outputPane = new OutputPane();
		outputWindow = new JScrollPane(outputPane);
		add(outputWindow, BorderLayout.SOUTH);

		// Set the output pane to the viewer
		metadataViewer.setOutputPane(outputPane);
		
		// Add the openBIS viewer
		openBISViewer = new OpenBISViewer(outputPane);
		add(openBISViewer.getPanel(), BorderLayout.EAST);
		
		// Add the editor: it is important to create this object as the last
		// one, since it requires non-null references to the metadata and
		// the openBIS viewers, and the output pane!
		editorContainer = new EditorContainer(
				metadataViewer, openBISViewer, outputPane);
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
		setMaximumSize(new Dimension(1500, 1500));
		pack();
		setLocationRelativeTo(null);

		// Ask the user to login.
		// Here we will insist on getting valid openBIS credentials, since a
		// valid login is essential for the functioning of the application.
		// If the user just closes the dialog, we close the application.
		boolean status = false;
		while (!status) {
			status = openBISViewer.login();
		}

		// Now we can scan the openBIS instance
		openBISViewer.scan();

		// Scan the datamover incoming folder for datasets
		metadataViewer.setUserName(openBISViewer.getUserName());
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
		if (e.getActionCommand().equals("Quit")) {
				QuitApplication();
		} else {
			// Nothing
		}
	}

	/**
	 * Add buttons to the toolbar
	 * 
	 * TODO: Currently this is not in use!
	 */
	private void addButtons() {

		// Quit
        toolBar.add(createButton("icons/quit.png", "Quit",
        		"Quit", "Quit the application"));

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
        	try {
        		openBISViewer.logout();
        		System.exit(0);
        	} catch (RemoteAccessException e) {
       
        		// Inform user that logging out was unsuccessful
				// Give the user the option to wait or close
				// the application
				Object[] options = {"Wait", "Force close"};
				int n = JOptionPane.showOptionDialog(this,
						"Could not log out from openBIS: " + 
							"the server is no longer reachable.\n" +
							"Do you want to wait and retry later or to " +
							"force close the application?",
						"Connection error",
						JOptionPane.ERROR_MESSAGE,
						JOptionPane.YES_NO_OPTION,
						null, options, options[0]);
				if (n==0) {
					// Wait
					return;
				} else {
					// Force close
					System.exit(1);
				}
        	}
        }
   }

}
