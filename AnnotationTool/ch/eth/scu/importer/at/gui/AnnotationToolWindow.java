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
import ch.eth.scu.importer.common.version.VersionInfo;

/**
 * Main window of the AnnotationTool application.
 * @author Aaron Ponti
 */
public class AnnotationToolWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

    private OpenBISViewer openBISViewer;
	private AbstractViewer metadataViewer;
	private JToolBar toolBar;
	private Icon appIcon; 

	/**
	 * Constructor
	 */
	public AnnotationToolWindow() {

		// Call the frame's constructor
		super("openBIS Importer Toolset :: Annotation Tool v" +
		VersionInfo.version + " " + VersionInfo.status);

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

		// Create a toolbar
		toolBar = new JToolBar("Tools");
		toolBar.setFloatable(false);
		// TODO: Add tools...

		// Set constraints and add widget
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		add(toolBar, constraints);
		
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
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		add(metadataViewer.getPanel(), constraints);

		// Create the HTML viewing pane.
        OutputPane outputPane = new OutputPane();
        outputPane.setMaximumSize(new Dimension(1500, 200));
        JScrollPane outputWindow = new JScrollPane(outputPane);
        //outputWindow.setMaximumSize(new Dimension(1500, 200));
        
		// Set constraints and add widget
        constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(0, 5, 5, 5);
		add(outputWindow, constraints);
		
		// Set the output pane to the viewer
		metadataViewer.setOutputPane(outputPane);
		
		// Add the openBIS viewer
		openBISViewer = new OpenBISViewer(outputPane);
		
		// Set constraints and add widget
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(0, 5, 5, 5);
		add(openBISViewer.getPanel(), constraints);
		
		// Add the editor: it is important to create this object as
		// the last one, since it requires non-null references to the 
		// metadata, the openBIS viewers, and the output pane!
        EditorContainer editorContainer = new EditorContainer(
                metadataViewer, openBISViewer, outputPane);

		// Set constraints and add widget
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(0, 5, 5, 0);
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

		// Ask the user to login.
		// Here we will insist on getting valid openBIS credentials, since a
		// valid login is essential for the functioning of the application.
		// If the user just closes the dialog, we close the application.
		boolean status = false;
		while (!status) {
			try {
				status = openBISViewer.login();
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(null,
					    "Interrupted execution of login threads!",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
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
	private JButton createButton(String imgLocation,
			String text, String actionCommand, String toolTipText) {

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
		// Ask the user for confirmation
        if (JOptionPane.showConfirmDialog(this, 
        		"Do you really want to quit?", "Question",
        		JOptionPane.YES_NO_OPTION,
        		JOptionPane.QUESTION_MESSAGE, appIcon) == 
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
