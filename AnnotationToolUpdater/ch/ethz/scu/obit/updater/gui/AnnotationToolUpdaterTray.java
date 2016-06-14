package ch.ethz.scu.obit.updater.gui;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Font;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import ch.ethz.scu.obit.common.settings.AppUpdaterSettingsManager;
import ch.ethz.scu.obit.common.version.VersionInfo;
import ch.ethz.scu.obit.updater.gui.dialogs.AnnotationToolUpdaterSettingsDialog;

/**
 * Create the application menu (with all functionalities implemented) and sets
 * it in the tray.
 * 
 * @author Aaron Ponti
 */
public class AnnotationToolUpdaterTray {

    /**
     * Settings manager
     */
    AppUpdaterSettingsManager manager;

    /**
     * Settings dialog
     */
    AnnotationToolUpdaterSettingsDialog settingsDialog;

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

        // Read settings or initialize them
        manager = new AppUpdaterSettingsManager();

        // Set the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Couldn't set look and feel.");
        }

        // Tray icon
        final ImageIcon appIcon = new ImageIcon(
                this.getClass().getResource("icons/icon_updater.png"));
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
                if (settingsDialog == null) {
                    settingsDialog = new AnnotationToolUpdaterSettingsDialog(manager);
                } else {
                    settingsDialog.setVisible(true);
                }

            }
        });

        // Clean up and exit
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (settingsDialog != null) {
                    settingsDialog.dispose();
                }
                tray.remove(trayIcon);
                System.exit(0);
            }
        });

        // Add the menu and icon to the tray
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added.");
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
            JOptionPane.showMessageDialog(null, 
                    new UrlTextPane(VersionInfo.versionIntegerToString(versionInt)),
                    "Updates available!", JOptionPane.PLAIN_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Congratulations! You are running the latest " +
            "version of the Annotation Tool!",
            "No updates available",
            JOptionPane.INFORMATION_MESSAGE);
        }

    }

    /**
     * Retrieve latest Annotation Tool version from remote server as integer.
     * 
     * @return version as integer, or -1 if the version could not be retrieved.
     */
    private int retrieveRemoteVersion() {

        // Settings
        boolean useProxy = manager.getSettingValue("useProxyServer").equals("1");
        String proxyAddress = "";
        int proxyPort = 0;
        if (useProxy) {

            // Get the proxy address
            proxyAddress = manager.getSettingValue("proxyServerName");

            // Get the proxy port
            try {
                proxyPort = Integer.parseInt(manager.getSettingValue("proxyServerPort"));
            } catch (NumberFormatException e) {
                proxyPort = 0;
            }
        }
        
        // Set the update server URL
        URL updateURL;
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
    

    /**
     * Custom JTextPane with a link to the download page.
     * @author Aaron Ponti
     *
     */
    class UrlTextPane extends JTextPane {

        private static final long serialVersionUID = 1L;

        public UrlTextPane(String version) {

            // Copy font style
            Font font = (new JLabel()).getFont();
            StringBuffer style = new StringBuffer(
                    "font-family:" + font.getFamily() + ";");
            style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
            style.append("font-size:" + font.getSize() + "pt;");

            // More configuration
            setEditable(false);
            setOpaque(false);
            addHyperlinkListener((HyperlinkListener) new UrlHyperlinkListener());

            // Add the text
            setContentType("text/html");
            setText("<html><body style=\"" + style + "\">" +
                "A new version of the Annotation Tool " + 
                "(" + version +") is ready to be " + 
                "<a href=\"https://github.com/aarpon/obit_annotation_tool/releases/latest\">downloaded</a>!" +
                "</body></html>");
        }

        /**
         * Listener for hyperlink interaction
         * @author Aaron Ponti
         *
         */
        private class UrlHyperlinkListener implements HyperlinkListener {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(event.getURL().toURI());
                    } catch (final IOException e) {
                        JOptionPane.showMessageDialog(null,
                                "Could not connect to download URL!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (final URISyntaxException e) {
                        JOptionPane.showMessageDialog(null,
                                "Could not connect to download URL!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }                
            }
        };
    }
   
}