package ch.ethz.scu.obit.common.version;

/**
 * @author Aaron Ponti
 * Version information.
 */
public class VersionInfo {

	/**
	 * Program version
	 */
	public static final String version = "0.8.0";
	
	/**
	 * Version status: "alpha", "beta", or "" for a stable release
	 */
	public static final String status = "";

	/**
	 *  Copyright string
	 */
	public static final String copyright = "Copyright " +
			Character.toString('\u00A9') + 
			" 2012 - 2015, Single Cell Unit, " +
			"D-BSSE, ETH Zurich (Basel)";

	/**
	 *  Application settings (XML) file version
	 */
	public static final int applicationSettingsVersion = 5;

	/**
	 *  User settings (XML) file version
	 */
	public static final int userSettingsVersion = 1;

}
