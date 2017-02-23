package ch.ethz.scu.obit.updater.gui;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
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
	 * Tray icon
	 */
	TrayIcon trayIcon;

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
		final ImageIcon appIcon = new ImageIcon(this.getClass().getResource("icons/icon_updater.png"));
		trayIcon = new TrayIcon(appIcon.getImage());
		trayIcon.setImageAutoSize(true);
		final SystemTray tray = SystemTray.getSystemTray();

		// Create the menu
		final PopupMenu popup = new PopupMenu();

		// Create a popup menu components
		MenuItem checkItem = new MenuItem("Check for updates");
		Menu downloadSection = new Menu("Download latest version");
		MenuItem download32bitItem = new MenuItem("32 bit");
		MenuItem download64bitItem = new MenuItem("64 bit");
		downloadSection.add(download32bitItem);
		downloadSection.add(download64bitItem);
		MenuItem settingsItem = new MenuItem("Settings");
		MenuItem aboutItem = new MenuItem("About");
		MenuItem exitItem = new MenuItem("Exit");

		// Add components to popup menu
		popup.add(checkItem);
		popup.add(downloadSection);
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

		// Download the 32 bit installer
		download32bitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloadInstaller(false);
			}
		});

		// Download the 64 bit installer
		download64bitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloadInstaller(true);
			}
		});

		// Show about dialog
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "AnnotationTool Updater version " + VersionInfo.version + ".",
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
			JOptionPane.showMessageDialog(null, "Could not retrieve the latest version!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Are the updates
		boolean updates = versionInt > VersionInfo.versionStringToInteger(VersionInfo.version);

		if (updates) {
			JOptionPane.showMessageDialog(null, new UrlTextPane(VersionInfo.versionIntegerToString(versionInt)),
					"Updates available!", JOptionPane.PLAIN_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					"Congratulations! You are running the latest " + "version of the Annotation Tool!",
					"No updates available", JOptionPane.INFORMATION_MESSAGE);
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
			JOptionPane.showMessageDialog(null, "Malformed URL. This is a bug. Please report it!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return -1;
		}

		// Retrieve the version
		int versionInt = -1;

		HttpURLConnection conn;
		if (useProxy) {

			// Set the proxy
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort));

			// Open the connection via a proxy
			try {
				conn = (HttpURLConnection) updateURL.openConnection(proxy);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Could not connect to remote server to check for updates!", "Error",
						JOptionPane.ERROR_MESSAGE);
				return -1;
			}

		} else {

			// Open a direct connection without a proxy
			try {
				conn = (HttpURLConnection) updateURL.openConnection();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Could not connect to remote server to check for updates!", "Error",
						JOptionPane.ERROR_MESSAGE);
				return -1;
			}
		}

		// Actually connect to the server
		try {
			conn.connect();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not connect to remote server to check for updates!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return -1;
		}

		// Read the file
		Scanner s;
		try {
			s = new Scanner(conn.getInputStream());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not connect to remote server to check for updates!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return -1;
		}

		// Read the version
		try {
			versionInt = s.nextInt();
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(null, "This seems to be a bug... Please report it!", "Error",
					JOptionPane.ERROR_MESSAGE);
			versionInt = -1;
		}

		// Close the connection.
		s.close();

		// Return the extracted version info
		return versionInt;
	}

	/**
	 * Download latest Annotation Tool in either 32- or 64-bit version.
	 * @param is64bit True if the 64 bit version should be installed, false for the 32 bit version.
	 */
	private void downloadInstaller(boolean is64bit) {

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

		// Build the download URL
		URL downloadURL;
		String versionString = VersionInfo.versionIntegerToString(retrieveRemoteVersion());

		String platform;
		if (is64bit == true) {
			platform = "64bit";
		} else {
			platform = "32bit";
		}

		String installerFileName = "obit_annotation_tool_" + versionString + "_" + platform + ".zip";
		try {
			downloadURL = new URL("https://github.com/aarpon/obit_annotation_tool/releases/download/" + versionString
					+ "/" + installerFileName);
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(null, "Malformed URL. This is a bug. Please report it!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Ask for target directory
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Save to...");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		File outputDir;
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			outputDir = chooser.getSelectedFile();
		} else {
			return;
		}

		// Output file
		File outputFile = new File(outputDir, installerFileName);

		// Download the file
		HttpURLConnection conn;
		if (useProxy) {

			// Set the proxy
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort));

			// Open the connection via a proxy
			try {
				conn = (HttpURLConnection) downloadURL.openConnection(proxy);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Could not connect to remote server for download!", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

		} else {

			// Open a direct connection without a proxy
			try {
				conn = (HttpURLConnection) downloadURL.openConnection();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Could not connect to remote server for download!", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		// Actually connect to the server
		try {
			conn.connect();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not connect to remote server for download!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Inform the user that the download is in process
		trayIcon.displayMessage("", "Downloading...", MessageType.INFO);

		// Read the file
		InputStream input;
		try {
			input = conn.getInputStream();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not download installer file!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		byte[] buffer = new byte[4096];
		int n = -1;

		OutputStream output;
		try {
			output = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Could not download installer file!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			while ((n = input.read(buffer)) != -1) {
				output.write(buffer, 0, n);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not download installer file!", "Error",
					JOptionPane.ERROR_MESSAGE);

			// We do not return yet since we still need to try and close the
			// stream
		}

		try {
			output.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not download installer file!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Inform the user that the download is in process
		trayIcon.displayMessage("", "Installer successfully downloaded to " + outputFile + "!", MessageType.INFO);

	}

	/**
	 * Custom JTextPane with a link to the download page.
	 * 
	 * @author Aaron Ponti
	 *
	 */
	class UrlTextPane extends JTextPane {

		private static final long serialVersionUID = 1L;

		public UrlTextPane(String version) {

			// Copy font style
			Font font = (new JLabel()).getFont();
			StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
			style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
			style.append("font-size:" + font.getSize() + "pt;");

			// More configuration
			setEditable(false);
			setOpaque(false);
			addHyperlinkListener((HyperlinkListener) new UrlHyperlinkListener());

			// Add the text
			setText("A new version of the Annotation Tool " + "(" + version + ") is ready to be downloaded!");
		}

		/**
		 * Listener for hyperlink interaction
		 * 
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
						JOptionPane.showMessageDialog(null, "Could not connect to download URL!", "Error",
								JOptionPane.ERROR_MESSAGE);
					} catch (final URISyntaxException e) {
						JOptionPane.showMessageDialog(null, "Could not connect to download URL!", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
	}

}
