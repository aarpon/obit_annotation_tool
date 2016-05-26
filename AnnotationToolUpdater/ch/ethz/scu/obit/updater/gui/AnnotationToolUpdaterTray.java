package ch.ethz.scu.obit.updater.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.swing.*;

import ch.ethz.scu.obit.common.version.VersionInfo;

/**
 * Create the application menu and sets it in the tray.
 * @author Aaron Ponti
 *
 */
public class AnnotationToolUpdaterTray {

    /**
     * Constructor.
     */
    public AnnotationToolUpdaterTray() {
    	
    	// Set the system look and feel
        try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't set look and feel.");
		}

        // Tray icon
		final ImageIcon appIcon = new ImageIcon(
				this.getClass().getResource("icons/icon_uploader.png"));
        final TrayIcon trayIcon = new TrayIcon(appIcon.getImage());
        trayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();
     
        // Create the menu
        final PopupMenu popup = new PopupMenu();
         
        // Create a popup menu components
        MenuItem checkItem = new MenuItem("Check for updates");
        MenuItem settingsItem = new MenuItem("Settings");
        MenuItem aboutItem = new MenuItem("About");
        MenuItem exitItem = new MenuItem("Exit");
         
        // Add components to popup menu
        popup.add(checkItem);
        popup.add(settingsItem);
        popup.addSeparator();
        popup.add(aboutItem);
        popup.add(exitItem);
        
        // Attach the menu to the tray icon
        trayIcon.setPopupMenu(popup);
         
        // This is called when double-clicking on the tray icon
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	checkForUpdates();
            }
        });
         
        // Check for updates
        checkItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	checkForUpdates();
            }
        });
        
        // Show about dialog
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "AnnotationTool Updater version " + 
                VersionInfo.version + ".",
                "About", 0,
                appIcon);
            }
        });
        
        // Show the settings dialog
        settingsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "This will open the settings dialog.");
            }
        });
         
        // Clean up and exit
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });

        // Add the menu and icon to the tray
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }

    }
    
    /**
     * Check remote server for version update.
     */
    private void checkForUpdates() {

    	// Get remote version
    	int versionInt = retrieveRemoteVersion();

    	// Are the updates
    	boolean updates = versionInt > VersionInfo.versionStringToInteger(VersionInfo.version);
    	
    	if (updates) {
                JOptionPane.showMessageDialog(null,
                        "A new version (" + 
                VersionInfo.versionIntegerToString(versionInt) + 
                ") of the Annotation Tool is ready to be dowloaded!");            		
        	} else {
                JOptionPane.showMessageDialog(null,
                        "Congratulations! You are running the latest "
                        + "version of the Annotation Tool!");
    	}
    	
    }

    /**
     * Retrieve latest Annotation Tool version from remote server as integer.
     */
    private int retrieveRemoteVersion() {
    	
    	int versionInt = -1;
      	
    	try {
        	URL website = new URL("http://huygens-rm.org/updates/VERSION");
			Scanner s = new Scanner(website.openStream());
			versionInt = s.nextInt();
			s.close();
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(null,
					"Malformed URL. This is a bug. Please report it!",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Could not retrieve version information.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}

    	return versionInt;
    }
}
