package ch.ethz.scu.obit.updater.gui;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ch.ethz.scu.obit.common.version.VersionInfo;

/**
 * Create the application menu (with all functionalities implemented) and sets
 * it in the tray.
 * 
 * @author Aaron Ponti
 */
public class AnnotationToolUpdaterTray {

    /**
     * Whether we should use a proxy for connection or not
     */
    boolean useProxy = false;

    /**
     * Proxy host name
     */
    String proxyAddress;

    /**
     * Proxy port
     */
    int proxyPort;

    /**
     * Constructor.
     */
    public AnnotationToolUpdaterTray() {

        // TODO: read settings (proxy) from configuration file
        useProxy = false;
        proxyAddress = "proxy.example.com";
        proxyPort = 8080;

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
                        "About", 0, appIcon);
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
        if (versionInt == -1) {
            return;
        }

        // Are the updates
        boolean updates = versionInt > VersionInfo.versionStringToInteger(VersionInfo.version);

        if (updates) {
            JOptionPane.showMessageDialog(null, "A new version (" +
        VersionInfo.versionIntegerToString(versionInt)
                    + ") of the Annotation Tool is ready to be dowloaded!");
        } else {
            JOptionPane.showMessageDialog(null,
                    "Congratulations! You are running the latest " +
            "version of the Annotation Tool!");
        }

    }

    /**
     * Retrieve latest Annotation Tool version from remote server as integer.
     * 
     * @return version as integer, or -1 if the version could not be retrieved.
     */
    private int retrieveRemoteVersion() {

        // Temp
        boolean useProxy = false;

        // Settings
        String proxyAddress = ""; // TODO: read from settings file
        int proxyPort = 8080; // TODO: read from settings file

        URL updateURL;

        // Set the update server URL
        try {
            updateURL = new URL("https://raw.githubusercontent.com/aarpon/obit_annotation_tool/master/updates/VERSION");
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(null,
                    "Malformed URL. This is a bug. Please report it!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        // Retrieve the version
        int versionInt = -1;

        HttpURLConnection conn;
        if (useProxy) {

            // Set the proxy
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(proxyAddress, proxyPort));

            // Open the connection via a proxy
            try {
                conn = (HttpURLConnection) updateURL.openConnection(proxy);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to remote server to check for updates!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return -1;
            }

        } else {

            // Open a direct connection without a proxy
            try {
                conn = (HttpURLConnection) updateURL.openConnection();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to remote server to check for updates!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return -1;
            }
        }

        // Actually connect to the server
        try {
            conn.connect();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to remote server to check for updates!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        // Read the file
        Scanner s;
        try {
            s = new Scanner(conn.getInputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to remote server to check for updates!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        // Read the version
        try {
            versionInt = s.nextInt();
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(null,
                    "This seems to be a bug... Please report it!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            versionInt = -1;
        }

        // Close the connection.
        s.close();

        // Return the extracted version info
        return versionInt;
    }
}
