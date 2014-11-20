package ch.ethz.scu.obit.processors.openbis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;

import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

import ch.ethz.scu.obit.at.gui.dialogs.OpenBISLoginDialog;
import ch.ethz.scu.obit.common.settings.AppSettingsManager;
import ch.ethz.scu.obit.common.settings.UserSettingsManager;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.FacadeFactory;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * Processor that takes care of getting user credentials and logging in
 * to openBIS.
 * @author Aaron Ponti
 */
public class OpenBISProcessor {

	private String openBISURL = "";
	private String userName = "";
	private String userPassword = "";
	private int timeout = 60000;
	private AtomicReference<IOpenbisServiceFacade> facade =
			new AtomicReference<IOpenbisServiceFacade>();
	private AtomicReference<IQueryApiFacade> queryFacade =
			new AtomicReference<IQueryApiFacade>();

	private boolean isLoggedIn = false;
	
	private AggregationServiceDescription createProjectService = null;

	private AtomicReference<String> loginErrorMessage = 
			new AtomicReference<String>("");
	private AtomicReference<String> loginErrorTitle =
			new AtomicReference<String>("");
	private AtomicBoolean loginErrorRecoverable = new AtomicBoolean(true);


	/**
	 * Constructor
	 */
	public OpenBISProcessor() {

		// Get the openBIS URL from the appProperties
		UserSettingsManager manager = new UserSettingsManager();
		if (! manager.load()) {
			JOptionPane.showMessageDialog(null,
					"Could not read application settings!\n" +
			"Please contact your administrator. The application\n" +
			"will now exit!",
			"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		// Set the URL
		this.openBISURL = manager.getSettingValue("OpenBISURL");
		
	}
	
	/**
	 * Returns the user name if successfully logged in, empty string otherwise 
	 * @return user name or empty String if log on was not successful 
	 */
	public String getUserName() {
		if (!isLoggedIn) {
			return "";
		}
		return userName;
	}
	
	/**
	 * Check whether we are currently logged in to openBIS. 
	 * @return true if we are logged in, false otherwise.
	 */
	public boolean isLoggedIn() {
		return isLoggedIn && facade.get() != null;
	}
	
	/**
	 * Check if the facades are still valid.
	 * @return true if they are still valid, false otherwise.
	 */
	public boolean checkSession() {
		try {
			facade.get().checkSession();
		} catch ( InvalidSessionException e ) {
			facade.set(null);
			queryFacade.set(null);
			isLoggedIn = false;
			return false;
		}
		return true;
	}
	
	/**
	 * Asks the user to enter credentials.
	 * @return true if the credentials were entered correctly, false otherwise.
	 */
	private boolean askForCredentials() {

		// Modal dialog: stops here until the dialog is disposed
		// (when a username and password have been provided)
        OpenBISLoginDialog loginDialog = new OpenBISLoginDialog();
		userName = loginDialog.getUsername();
		userPassword = loginDialog.getPassword();
		openBISURL = loginDialog.getOpenBISServer();
		return (! userName.isEmpty());
	}
	
	/**
	 * Login to openBIS. Credentials provided by the user through a dialog.
	 * @return true if login was successful (or if already logged in), 
	 * false otherwise.
	 * @throws InterruptedException 
	 */
	public boolean login() throws InterruptedException {

		// Check that user name and password were set
		if (userName.equals("") || userPassword.equals("")) {
			boolean status = false;
			while (!status) {
				// This is redundant, since the dialog cannot be closed
				// without specifying a user name and password.
				status = askForCredentials();
			}
		}
		
		// Are we already logged in?
		if (isLoggedIn) {
			return true;
		}
		
		// Now save the user settings
		AppSettingsManager manager = new AppSettingsManager();
		UserSettingsManager userManager = new UserSettingsManager(
				manager.getSettingsForServer(openBISURL));
		userManager.save();
		manager = null;
	
		// Create a thread for logging in to openBIS and returning an
		// IOpenbisServiceFacade
		Thread serviceFacadeCreator = new Thread() {
		
			public void run() {

				// Create an IOpenbisServiceFacade to query openBIS for
				// projects, experiments and samples.
				// If the factory returns a valid object, it means that
				// the credentials provided were accepted and the user
				// was successfully logged in.
				try {
					facade.set(OpenbisServiceFacadeFactory.tryCreate(userName,
							userPassword, openBISURL, timeout));
				} catch (UserFailureException e) {
					facade.set(null); reactToUserFailureException();
				} catch (RemoteConnectFailureException e) {
					facade.set(null); reactToRemoteConnectFailureException();
				} catch (RemoteAccessException e) {
					facade.set(null); reactToRemoteAccessException();
				}
			}
		};

		// Create a thread for logging in to openBIS and returning an
		// IQueryApiFacade	
		Thread queryFacadeCreator = new Thread() {
			
			public void run() {

				// Create also an IQueryApiFacade to access the reporting
				// plugins on the server.
				try {
					queryFacade.set(FacadeFactory.create(openBISURL,
						userName, userPassword));
				} catch (UserFailureException e) {
					queryFacade.set(null); reactToUserFailureException();
				} catch (RemoteConnectFailureException e) {
					queryFacade.set(null); reactToRemoteConnectFailureException();
				} catch (RemoteAccessException e) {
					queryFacade.set(null); reactToRemoteAccessException();
				}

			}
		};

		// Should we accept self-signed certificates?
		String acceptSelfSignedCerts = 
				userManager.getSettingValue("AcceptSelfSignedCertificates");

		// Set the force-accept-ssl-certificate option if requested
		// by the administrator
		if (acceptSelfSignedCerts.equals("yes")) {
			System.setProperty("force-accept-ssl-certificate", "true");
		}

		// Try logging in with current credentials
		try {
			
			// Create an IOpenbisServiceFacade in one thread to query
			// openBIS for projects, experiments and samples.
			serviceFacadeCreator.start();

			// Create also an IQueryApiFacade in another thread to access
			// the reporting plug-ins on the server.
			queryFacadeCreator.start();
			
			// Wait for both threads to finish
			serviceFacadeCreator.join();
			queryFacadeCreator.join();

		} catch (InterruptedException e) {

			// Thread interrupted
			facade.set(null);
			queryFacade.set(null);
			reactToInterruptedException();
			
		}

		// Set isLoggedIn to true
		if (facade.get() != null && queryFacade.get() != null) {
			
			// Successful login
			isLoggedIn = true;
			
			// Return success
			return true;
			
		} else {
			
			// Failed login. Inform
			JOptionPane.showMessageDialog(null, loginErrorMessage.get(),
					loginErrorTitle.get(), JOptionPane.ERROR_MESSAGE);
			if (loginErrorRecoverable.get()) {
				
				// We can retry. Make sure to reset user name and password
				userName = "";
				userPassword = "";
				
				// Return false
				return false;
			
			} else {
				
				// The error is not recoverable. Exit here.
				System.exit(1);
				
			}

		}
		
		return false;
	}
	
	/**
	 * Log out from openBIS
	 * @return true if logging out was successful, false otherwise.
	 */
	public boolean logout() throws RemoteAccessException {
		if (facade.get() != null && isLoggedIn && queryFacade.get() != null) {
			facade.get().logout();
			queryFacade.get().logout();
			isLoggedIn = false;
			return true;
		}
		return false;
	}

	/**
	 * Returns the list of Spaces in openBIS (as visible for current user).
	 * @return list of Spaces.
	 */
	public List<SpaceWithProjectsAndRoleAssignments> getSpaces() {
		if (facade.get() == null) {
			return new ArrayList<SpaceWithProjectsAndRoleAssignments>();
		}
		return facade.get().getSpacesWithProjects();
	}			

	/**
	 * Returns the list of Experiment for a project list (as visible for 
	 * current user).
	 * @return list of Experiments.
	 */
	public ArrayList<Experiment> getExperimentsForProjects(
			List<String> expId) {
		if (facade.get() == null) {
			return new ArrayList<Experiment>();
		}
		return (ArrayList<Experiment>)
				facade.get().listExperimentsForProjects(expId);
	}			


	/**
	 * Returns the list of Samples for an Experiment list (as visible for 
	 * current user).
	 * @return list of Samples.
	 */
	public ArrayList<Sample> getSamplesForExperiments(List<String> expId) {
		if (facade == null) {
			return new ArrayList<Sample>();
		}
		return (ArrayList<Sample>)
				facade.get().listSamplesForExperiments(expId);
	}			
		
	/**
	 * Retrieve and store the create_project ingestion service from the
	 * server.
	 * @return true if the service could be retrieved successfully,
	 * false otherwise. If the services was retrieved already, returns true
	 * with no additional actions.
	 */
	public boolean retrieveAndStoreServices() {
		
		// Do we still have the service?
		if (createProjectService != null) {
			return true;
		}

		// Retrieve the create_project ingestion service from the server
		List<AggregationServiceDescription> aggregationServices = 
				queryFacade.get().listAggregationServices();

		// Go over all returned services and store a reference to the
		// create_project ingestion plug-in.
		for (AggregationServiceDescription service : aggregationServices)
        {
			// The 'micr_create_project' and 'flow_create_project' plug-ins
			// are identical and we can use either one; but we have to check
			// for both since we do not know which core technologies are
			// enabled.
			if (service.getServiceKey().equals("flow_create_project") ||
					service.getServiceKey().equals("micr_create_project")) {
				createProjectService = service;
				
				// Found, we can return success
				return true;
			}
        }
		
		// Not found, we return failure
		return false;
	}

	// Call the aggregation plugin to create a project with given code in space
	// i
	/**
	 * Create a project with given code in the specified space.
	 * @param spaceCode Code of the project to be created.
	 * @param projectCode Code of the space where the project will be created.
	 * @return a QueryTableModel with one row containing "success" and "message"
	 * column. You can query the content of the QueryTableModel as follows:
	 * 
	 *  {code}
	 *  String success= "";
	 *	String message = "";
	 *	List<Serializable[]> rows = tableModel.getRows();
	 *	for (Serializable[] row : rows) {
	 *		success = (String)row[0];
	 *		message = (String)row[1];
	 *		if (success.equals("true")) {
	 *			System.out.println(message);
	 *			return true;
	 *		}
	 *	}
	 *	System.err.println(message);
	 */
	public QueryTableModel createProject(String spaceCode, String projectCode) {
		
		// Set the parameters
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("spaceCode", spaceCode.toUpperCase());
		parameters.put("projectCode", projectCode.toUpperCase());
		
		QueryTableModel tableModel = 
			queryFacade.get().createReportFromAggregationService(
					createProjectService, parameters);
		
		return tableModel;
	}
	
	/**
	 * React to a RemoteConnectFailure exception
	 */
	private void reactToRemoteConnectFailureException() {
		loginErrorMessage.set("Could not connect to openBIS.\n"
				+ "Please try again later.\n\n"
				+ "The application will now quit.");
		loginErrorTitle.set("Connection error");
		loginErrorRecoverable.set(false);
	}

	/**
	 * React to a UserFailureException exception
	 */
	private void reactToUserFailureException() {
		loginErrorMessage.set("Login failed. Please try again.");
		loginErrorTitle.set("Authentication error");
		loginErrorRecoverable.set(true);
	}

	/**
	 * React to a RemoteAccessException exception
	 */
	private void reactToRemoteAccessException() {
		loginErrorMessage.set("Could not connect to openBIS: "
				+ "the server appears to be down.\n"
				+ "Please try again later.\n\n"
				+ "The application will now quit.");
		loginErrorTitle.set("Connection error");
		loginErrorRecoverable.set(false);
	}
	
	/**
	 * React to a InterruptedException exception
	 */
	private void reactToInterruptedException() {
		loginErrorMessage.set("Connection thread interrupted!\n\n" +
				"The application will now quit.");
		loginErrorTitle.set("Thread interrupted");
		loginErrorRecoverable.set(false);
        Thread.currentThread().interrupt();
	}
	
}
