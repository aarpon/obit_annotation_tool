package ch.ethz.scu.obit.common.version;

/**
 * @author Aaron Ponti
 * Version information.
 */
public class VersionInfo {

    /**
     * Program version
     */
    public static final String version = "2.0.0";

    /**
     * Version status: "alpha", "beta", or "" for a stable release
     */
    public static final String status = "beta";

    /**
     *  Copyright string
     */
    public static final String copyright = "Copyright " +
            Character.toString('\u00A9') +
            " 2012 - 2019, Single Cell Unit, " +
            "D-BSSE, ETH Zurich (Basel)";

    /**
     *  Application settings (XML) file version
     */
    public static final int applicationSettingsVersion = 7;

    /**
     *  Application updater settings (XML) file version
     */
    public static final int applicationUpdaterSettingsVersion = 1;

    /**
     *  User settings (XML) file version
     */
    public static final int userSettingsVersion = 1;

    /**
     *  oBITXML (XML) metadata settings (.oix) file
     */
    public static final int oBITXMLVersion = 2;

    /**
     * Convert the version from string to integer
     * @param version string like "1.0.0"
     * @return version integer like
     */
    public static int versionStringToInteger(String version) {

        // Extract the various parts
        String[] parts = version.split("\\.");
        if (parts.length < 2 || parts.length > 3) {
            return -1;
        }
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = 0;
        if (parts.length == 3) {
            patch = Integer.parseInt(parts[2]);
        }

        // Return the integer representation of the version string
        int versionInt = 1000000 * major + 10000 * minor + 100 * patch;
        return versionInt;
    }

    /**
     * Convert the version from integer to string
     * @param version string like
     * @return version integer like "1.0.0"
     */
    public static String versionIntegerToString(int version) {

        // Process the integer version
        int major = version / 1000000;
        version = version - major * 1000000;
        int minor = version / 10000;
        version = version - minor * 10000;
        int patch = version / 100;

        // Return the string representation of the version integer
        String versionString = "" + major + "." + minor + "." + patch;
        return versionString;
    }

}
