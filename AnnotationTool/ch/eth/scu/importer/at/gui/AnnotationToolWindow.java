package ch.eth.scu.importer.at.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.springframework.remoting.RemoteAccessException;

import ch.eth.scu.importer.at.gui.editors.data.EditorContainer;
import ch.eth.scu.importer.at.gui.pane.OutputPane;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.data.ViewerFactory;
import ch.eth.scu.importer.at.gui.viewers.openbis.OpenBISViewer;
import ch.eth.scu.importer.common.version.VersionInfo;
import ch.eth.scu.importer.processors.openbis.OpenBISProcessor;


/**
 * Main window of the AnnotationTool application.
 * @author Aaron Ponti
 */
public final class AnnotationToolWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private OpenBISProcessor openBISProcessor; 
    private OpenBISViewer openBISViewer;
	private AbstractViewer metadataViewer;
	private Icon appIcon; 

	/**
	 * Constructor
	 */
	public AnnotationToolWindow() {

		// Call the frame's constructor
		super("openBIS Importer Toolset :: Annotation Tool v" +
		VersionInfo.version + " " + VersionInfo.status);

		// We instantiate the OutputPane so we can start logging to it
        OutputPane outputPane = new OutputPane();

        // Instantiate an OpenBISProcessor
		openBISProcessor = new OpenBISProcessor();
		
		// And now we ask the user to login.
		// Here we will insist on getting valid openBIS credentials, since a
		// valid login is essential for the functioning of the application.
		// The OpenBISProcessor takes care of closing the application if the
		// user just closes the dialog.
		outputPane.log("Logging in to openBIS...");
		boolean status = false;
		while (!status) {
			try {
				status = openBISProcessor.login();
				if (! status) {
					outputPane.err("Failed logged in to openBIS!");
				}
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(null,
						"Interrupted execution of login threads!", "Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}
		outputPane.log("Successfully logged in to openBIS.");
		
		// Use the system default look-and-feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't set look and feel.");
		}

		// Icon
		appIcon = new ImageIcon(
				this.getClass().getResource("icons/icon.png"));
		
		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);		

		// Common constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.BOTH;

		// Add the metadata viewer
		try {
			metadataViewer = ViewerFactory.createViewer();
		} catch (Exception e1) {
			System.err.println("There was a problem instantiating "
					+ "the tools for current acquisition station.");
			System.exit(1);
		}

		// Set constraints and add widget
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 0, 5);
		add(metadataViewer.getPanel(), constraints);

		// Set constraints and add the OutputPane widget (instantiated earlier)
        constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(0, 5, 5, 5);
		add(outputPane, constraints);
		
		// Set the output pane to the viewer
		metadataViewer.setOutputPane(outputPane);
		
		// Add the openBIS viewer
		openBISViewer = new OpenBISViewer(openBISProcessor, outputPane);
		
		// Set constraints and add widget
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 0, 0, 5);
		add(openBISViewer.getPanel(), constraints);
		
		// Add the editor: it is important to create this object as
		// the last one, since it requires non-null references to the 
		// metadata, the openBIS viewers, and the output pane!
        EditorContainer editorContainer = new EditorContainer(
                metadataViewer, openBISViewer, outputPane);

		// Set constraints and add widget
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 0, 0, 5);
		add(editorContainer, constraints);
		
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
		setMinimumSize(new Dimension(1200, 900));
		setPreferredSize(new Dimension(1200, 900));
		pack();
		setLocationRelativeTo(null);

		// Make window visible
		setVisible(true);

	}

	/**
	 * Scans the data folder and the openBIS structure and updates the UI.
	 */
	public void scan() {

		// Scan the openBIS instance
		openBISViewer.scan();

    	// Scan the user data folder for datasets
		metadataViewer.setUserName(openBISViewer.getUserName());
		metadataViewer.scan();
		
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
	 * Quits the application properly
	 */
	private void QuitApplication() {
		// Ask the user for confirmation
        if (JOptionPane.showConfirmDialog(this, 
        		"Do you really want to quit?", "Question",
        		JOptionPane.YES_NO_OPTION,
        		JOptionPane.QUESTION_MESSAGE, appIcon) == 
        		JOptionPane.YES_OPTION) {
        	try {
        		openBISProcessor.logout();
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
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE,
						null, options, options[0]);
				if (n!=0) {
					// Force close
					System.exit(1);
				}
        	}
        }
   }

}
