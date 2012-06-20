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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);

		fortessaViewer = new BDLSRFortessaViewer();
		GridBagConstraints fortessaViewerConstraints = new GridBagConstraints();
		fortessaViewerConstraints.anchor = GridBagConstraints.NORTHWEST;
		fortessaViewerConstraints.fill = GridBagConstraints.BOTH;
		fortessaViewerConstraints.gridwidth = 1;
		fortessaViewerConstraints.gridheight = 1;
		fortessaViewerConstraints.weightx = 0.33;
		fortessaViewerConstraints.weighty = 1.0;
		fortessaViewerConstraints.gridx = 0;
		fortessaViewerConstraints.gridy = 0;
		add(fortessaViewer, fortessaViewerConstraints);

		metadataEditor = new JLabel("Metadata editor");
		metadataEditor.setVerticalAlignment(SwingConstants.TOP);
		metadataEditor.setMinimumSize(new Dimension(300,600));
		metadataEditor.setPreferredSize(new Dimension(300,600));
		GridBagConstraints metadataEditorConstraints = new GridBagConstraints();
		metadataEditorConstraints.anchor = GridBagConstraints.NORTHWEST;
		metadataEditorConstraints.fill = GridBagConstraints.BOTH;
		metadataEditorConstraints.gridwidth = 1;
		metadataEditorConstraints.gridheight = 1;
		metadataEditorConstraints.weightx = 0.33;
		metadataEditorConstraints.weighty = 1.0;
		metadataEditorConstraints.gridx = 1;
		metadataEditorConstraints.gridy = 0;
		add(metadataEditor, metadataEditorConstraints);
		
		spaceViewer = new OpenBISSpaceViewer();
		GridBagConstraints spaceViewerConstraints = new GridBagConstraints();
		spaceViewerConstraints.anchor = GridBagConstraints.NORTHWEST;
		spaceViewerConstraints.fill = GridBagConstraints.BOTH;
		spaceViewerConstraints.gridwidth = 1;
		spaceViewerConstraints.gridheight = 1;
		spaceViewerConstraints.weightx = 0.33;
		spaceViewerConstraints.weighty = 1.0;
		spaceViewerConstraints.gridx = 2;
		spaceViewerConstraints.gridy = 0;
		add(spaceViewer, spaceViewerConstraints);

		// Create the HTML viewing pane.
		outputPane = new JEditorPane();
		outputPane.setEditable(false);
		outputPane.setText("Ready");
		outputWindow = new JScrollPane(outputPane);

		GridBagConstraints outputWindowConstraints = new GridBagConstraints();
		outputWindowConstraints.anchor = GridBagConstraints.NORTHWEST;
		outputWindowConstraints.fill = GridBagConstraints.BOTH;
		outputWindowConstraints.gridwidth = 3;
		outputWindowConstraints.gridheight = 1;
		outputWindowConstraints.weightx = 1.0;
		outputWindowConstraints.weighty = 0.2;
		outputWindowConstraints.gridx = 0;
		outputWindowConstraints.gridy = 1;
		add(outputWindow, outputWindowConstraints);
		
		// Add menus
		addMenus();
		
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
		setMinimumSize(new Dimension(1200, 800));
		pack();
		setLocationRelativeTo(null);
		setResizable(true);
		
		// Ask the user to login
		spaceViewer.login();

		// Make window visible
		setVisible(true);
	}
	
	private void addMenus() {
		JMenuBar menuBar = new JMenuBar();
		JMenu mainMenu = new JMenu("Importer");
		mainMenu.add(makeMenuItem("Quit", KeyEvent.VK_Q));
		menuBar.add(mainMenu);
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(makeMenuItem("Pick file", KeyEvent.VK_F));
		fileMenu.add(makeMenuItem("Pick directory", KeyEvent.VK_D));
		menuBar.add(fileMenu);
		JMenu openBISMenu = new JMenu("openBIS");
		openBISMenu.add(makeMenuItem("Log in", KeyEvent.VK_I));
		openBISMenu.add(makeMenuItem("Log out", KeyEvent.VK_O));
		menuBar.add(openBISMenu);
		setJMenuBar(menuBar);
	}
	
	/**
	 * Create menu entries
	 * @param String to be displayed for the menu entry
	 * @return a JMenuItem to be added to the menubar
	 */
	private JMenuItem makeMenuItem(String name, int accelerator) {
		JMenuItem m = new JMenuItem(name);
		m.setAccelerator(KeyStroke.getKeyStroke(accelerator,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m.setActionCommand(name);
		m.addActionListener(this);
		return m;
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
			spaceViewer.logout();
			System.exit(0);
		} else {
			return;
		}
	}

	/**
	 * Program entry point
	 * @param args Ignored
	 */
	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		//System.setProperty("apple.laf.useScreenMenuBar", "true");

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new OpenBISImporter();
			}
		});
	}
}
