package ch.ethz.scu.obit.common.utils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Aaron Ponti Commodity class for OS-related functions.
 *
 */
public class QueryOS {

    /**
     * Check if the application is running on Mac OS X
     *
     * @return true if on Mac OS X, false otherwise
     */
    static public boolean isMac() {
        String OS = System.getProperty("os.name").toUpperCase();
        return OS.contains("MAC");
    }

    /**
     * Check if the application is running on Windows
     *
     * @return true if on Windows, false otherwise
     */
    static public boolean isWindows() {
        String OS = System.getProperty("os.name").toUpperCase();
        return OS.contains("WINDOWS");
    }

    /**
     * Check if the application is running on Linux
     *
     * @return true if on Linux, false otherwise
     */
    static public boolean isLinux() {
        String OS = System.getProperty("os.name").toUpperCase();
        return OS.contains("LINUX");
    }

    /**
     * Get the application data folder specific for current OS
     *
     * @return full path to the application data folder
     */
    static public File getOSSpecificAppDataFolder() {

        // Initialize the applicationDir variable
        File applicationDataDir;

        // Build the path as a function of the operating system
        if (QueryOS.isMac()) {
            applicationDataDir = new File("/Users/Shared/Library/Application Support/");
        } else if (QueryOS.isWindows()) {
            applicationDataDir = new File(System.getenv("ALLUSERSPROFILE"));
        } else if (QueryOS.isLinux()) {
            applicationDataDir = new File("/usr/local/");
        } else {
            throw new UnsupportedOperationException("Operating system not supported.");
        }

        return applicationDataDir;

    }

    /**
     * Get the user data folder specific for current OS
     *
     * @return full path to the user data folder
     */
    static public File getOSSpecificUserDataFolder() {

        // Initialize the userDir variable
        File userDir;

        // Build the path as a function of the operating system
        if (QueryOS.isMac()) {
            userDir = new File("/Users/" + System.getProperty("user.name") + "/Library/Application Support/");
        } else if (QueryOS.isWindows()) {
            String OS = System.getProperty("os.name").toUpperCase();
            if (OS.contains("XP")) {
                userDir = new File(System.getenv("APPDATA"));
            } else {
                userDir = new File(System.getenv("LOCALAPPDATA"));
            }
        } else if (QueryOS.isLinux()) {
            userDir = new File("/home/" + System.getProperty("user.name") + "/.config/");
        } else {
            throw new UnsupportedOperationException("Operating system not supported.");
        }

        return userDir;
    }

    /**
     * Retrieve the host name
     *
     * @return Host name.
     * @throws UnknownHostException If retrieving the host name was not possible.
     */
    static public String getHostName() throws UnknownHostException {

        // Trying retrieving the host name. If it is not possible,
        // an UnknownHostException exception is launched.
        InetAddress addr;
        addr = InetAddress.getLocalHost();
        return addr.getHostName();
    }

    /**
     * Returns the system file explorer and the commands to perform a select
     * operation.
     *
     * The information is returned into an array of strings 'commandArray', with:
     *
     * commandArray[0]: containing the path to the executable, e.g. "Explorer.exe"
     * commandArray[1]: containing the name of the file manager, e.g. 'Windows
     * Explorer' commandArray[2]: containing the correct select flag, e.g.
     * "/select," for Windows
     *
     * Please notice that for Linux, the function will try to find 'nautilus' and,
     * if this fails, 'dolphin', since these support the '--select' option. If
     * neither file manager is found, the commandArray will contain empty strings.
     *
     * File managers known **not** to support a select flag are:
     *
     * * nemo * thunar * caja * xdg-open
     *
     * * @return String array containing "command", "command name" and "argument".
     *
     * @throws Exception if the operating system is not supported.
     */
    static public String[] getFileExplorerCommand() throws Exception {

        // Initialize the defaults
        String[] commandArray = new String[3];

        if (QueryOS.isWindows()) {

            // Fill the array
            commandArray[0] = "Explorer.exe";
            commandArray[1] = "Windows Explorer";
            commandArray[2] = "/select,";

        } else if (QueryOS.isMac()) {

            // Fill the array
            commandArray[0] = "open";
            commandArray[1] = "Finder";
            commandArray[2] = "-R";

        } else if (QueryOS.isLinux()) {

            // Find the tool first
            if (new File("/usr/bin/nautilus").exists()) {
                commandArray[0] = "/usr/bin/nautilus";
                commandArray[1] = "nautilus";
                commandArray[2] = "--select";
            } else if (new File("/usr/bin/dolphin").exists()) {
                commandArray[0] = "/usr/bin/dolphin";
                commandArray[1] = "dolphin";
                commandArray[2] = "--select";
            } else {
                commandArray[0] = "";
                commandArray[1] = "";
                commandArray[2] = "";
            }
        } else {
            throw new Exception("Operating system not supported!");
        }

        return commandArray;
    }
}
