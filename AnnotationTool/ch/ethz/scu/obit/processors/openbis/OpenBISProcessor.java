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
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

/**
 * Processor that takes care of getting user credentials and logging in
 * to openBIS.
 * @author Aaron Ponti
 */
public class OpenBISProcessor {

    GlobalSettingsManager globalSettingsManager;

    private String openBISURL = "";
    private String userName = "";
    private String userPassword = "";
    private int timeout = 60000;

    private IApplicationServerApi v3_api;
    private String v3_sessionToken;

    //    private AggregationServiceDescription createMetaProjectService = null;

    private AtomicReference<String> loginErrorMessage =
            new AtomicReference<String>("");
    private AtomicReference<String> loginErrorTitle =
            new AtomicReference<String>("");
    private AtomicBoolean loginErrorRecoverable = new AtomicBoolean(true);

    // Cache spaces and projects
    private boolean dataIsCached = false;
    private boolean dataWithExperiments = false;
    private SearchResult<Space> cachedSpacesWithProjects = null;

    /**
     * Constructor
     * @param globalSettingsManager global settings manager
     */
    public OpenBISProcessor(GlobalSettingsManager globalSettingsManager) {

        // Store the reference to the global settings manager
        this.globalSettingsManager = globalSettingsManager;

        // Set the currently active server
        this.openBISURL = globalSettingsManager.getActiveServer();

        // Initialize cache
        this.dataIsCached = false;
        this.dataWithExperiments = false;
        this.cachedSpacesWithProjects = null;
    }

    /**
     * Returns the user name if successfully logged in, empty string otherwise
     * @return user name or empty String if log on was not successful
     */
    public String getUserName() {
        if (!isLoggedIn()) {
            return "";
        }
        return userName;
    }

    /**
     * Asks the user to enter credentials.
     * @return true if the credentials were entered correctly, false otherwise.
     */
    private boolean askForCredentials() {

        // Modal dialog: stops here until the dialog is disposed
        // (when a username and password have been provided)
        OpenBISLoginDialog loginDialog = new OpenBISLoginDialog(globalSettingsManager);
        userName = loginDialog.getUsername();
        userPassword = loginDialog.getPassword();
        openBISURL = loginDialog.getOpenBISServer();
        return (! userName.isEmpty());
    }

    /**
     * Login to openBIS. Credentials provided by the user through a dialog.
     * @return true if login was successful (or if already logged in),
     * false otherwise.
     * @throws InterruptedException If login was interrupted.
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
        if (isLoggedIn()) {
            return true;
        }

        // Create a thread for logging in to openBIS using the V3 API
        Thread v3APILogin = new Thread() {

            @Override
            public void run() {

                // The V3 API uses a slightly different URL
                String v3_apiURL = openBISURL  + "/openbis" + IApplicationServerApi.SERVICE_URL;

                // Log in using V3 API
                v3_api = HttpInvokerUtils.createServiceStub(
                        IApplicationServerApi.class, v3_apiURL, timeout);

                // login to obtain a session token
                try {

                    // Try logging in
                    v3_sessionToken = v3_api.login(userName, userPassword);

                    if (v3_sessionToken != null) {
                        // Register a class to log out from openBIS on close or
                        // runtime shutdown
                        Runtime.getRuntime().addShutdownHook(new V3APILogoutOnShutdown(
                                v3_api, v3_sessionToken));
                    } else {

                        // Login failed for bad credentials
                        v3_api = null;
                        v3_sessionToken = "";
                        reactToUserFailureException();
                    }

                } catch (RemoteConnectFailureException e) {

                    // Login failed for connection issues
                    v3_api = null;
                    v3_sessionToken = "";
                    reactToRemoteConnectFailureException();

                } catch (RemoteAccessException e) {

                    // Login failed for connection issues
                    v3_api = null;
                    v3_sessionToken = "";
                    reactToRemoteAccessException();

                }
            }
        };

        // Log in using the V3 API
        v3APILogin.start();

        // Wait for all threads to finish
        v3APILogin.join();

        if (isLoggedIn() == true) {

            // Authentication successful
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
                //                isLoggedIn = false;
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
     * @throws RemoteAccessException If remote access failed.
     */
    public boolean logout() throws RemoteAccessException {
        if (isLoggedIn()) {
            v3_api.logout(v3_sessionToken);
            return true;
        }
        return false;
    }

    /**
     * Retrieves and returns the list of metaprojects for current session.
     * @return list of metaprojects.
     */
    public List<String> getMetaprojects() {
        return new ArrayList<String>();

        //        if (infoService.get() == null) {
        //            return new ArrayList<String>();
        //        }
        //        List<Metaproject> metaprojects = infoService.get().listMetaprojects(
        //                queryFacade.get().getSessionToken());
        //        metaprojects.sort(new Comparator<Metaproject>() {
        //
        //            @Override
        //            public int compare(Metaproject m1, Metaproject m2) {
        //                return m1.getName().toLowerCase().compareTo(m2.getName().toLowerCase());
        //            }
        //        });
        //        List<String> tags = new ArrayList<String>();
        //        for (Metaproject m : metaprojects) {
        //            tags.add(m.getName());
        //        }
        //        return tags;
    }

    /**
     * Reset the cached data.
     */
    public void resetData() {

        this.dataIsCached = false;
        this.dataWithExperiments = false;
        this.cachedSpacesWithProjects = null;
    }

    /**
     * Returns the list of Spaces in openBIS (as visible for current user).
     * @return list of Spaces.
     */
    public SearchResult<Space> getSpacesWithProjectsAndExperiments() {

        // Do we have a valid session?
        if (v3_api == null) {
            return new SearchResult<Space>(new ArrayList<Space>(), 0);
        }

        // Is the data cached and with experiments?
        if (dataIsCached && dataWithExperiments) {
            return cachedSpacesWithProjects;
        }

        // Space
        SpaceSearchCriteria searchCriteria = new SpaceSearchCriteria();
        searchCriteria.withCode();
        SpaceFetchOptions spaceFetchOptions = new SpaceFetchOptions();
        spaceFetchOptions.sortBy().code();

        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();


        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withExperimentsUsing(experimentFetchOptions);

        spaceFetchOptions.withProjectsUsing(projectFetchOptions);

        cachedSpacesWithProjects = v3_api.searchSpaces(v3_sessionToken,
                searchCriteria, spaceFetchOptions);

        // Update cache information
        dataIsCached = true;
        dataWithExperiments = true;

        return cachedSpacesWithProjects;
    }

    /**
     * Returns the list of Spaces in openBIS (as visible for current user).
     * @return list of Spaces.
     */
    public SearchResult<Space> getSpacesWithProjects() {

        // Do we have a valid session?
        if (v3_api == null) {
            return new SearchResult<Space>(new ArrayList<Space>(), 0);
        }

        // Is the data cached?
        if (dataIsCached) {

            // We return whether it has experiments or not,
            // since spaces and projects will be there.
            return cachedSpacesWithProjects;
        }

        // Space
        SpaceSearchCriteria searchCriteria = new SpaceSearchCriteria();
        searchCriteria.withCode();
        SpaceFetchOptions spaceFetchOptions = new SpaceFetchOptions();
        spaceFetchOptions.sortBy().code();
        spaceFetchOptions.withProjects();

        cachedSpacesWithProjects = v3_api.searchSpaces(v3_sessionToken,
                searchCriteria, spaceFetchOptions);

        // Update cache information
        dataIsCached = true;
        dataWithExperiments = false;

        return cachedSpacesWithProjects;
    }

    /**
     * Returns the list of Experiment for a project list (as visible for
     * current user).
     * @param p Project to the queried for experiments.
     * @return list of Experiments.
     */
    public List<Experiment> getExperimentsForProjects(Project p) {

        // Do we have a valid session?
        if (! isLoggedIn()) {
            return new ArrayList<Experiment>();
        }

        if (dataWithExperiments == true) {

            // Retrieve the experiments. We can call getExperiments() since the
            // the Experiments were fetched when searching for projects in
            // getSpacesWithProjectsAndExperiments().
            return p.getExperiments();

        } else {

            // We need to retrieve the experiments from openBIS
            ProjectSearchCriteria searchCriteria = new ProjectSearchCriteria();
            searchCriteria.withCode();
            searchCriteria.withPermId().thatEquals(p.getPermId().toString());
            ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
            projectFetchOptions.sortBy().code();

            ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
            projectFetchOptions.withExperimentsUsing(experimentFetchOptions);

            SearchResult<Project> projects = v3_api.searchProjects(v3_sessionToken,
                    searchCriteria, projectFetchOptions);

            if (projects.getTotalCount() == 0) {
                return new ArrayList<Experiment>();
            }

            // Get the project and extract the experiments
            Project proj = projects.getObjects().get(0);
            return proj.getExperiments();

        }

    }


    /**
     * Returns the list of Samples for an Experiment list (as visible for
     * current user).
     * @param expId List of experiment ids.
     * @return list of Samples.
     */
    public List<Sample> getSamplesForExperiments(Experiment exp) {
        if (! isLoggedIn()) {
            return new ArrayList<Sample>();
        }

        // Search for Experiments with given code and fetch its samples
        ExperimentSearchCriteria searchCriteria = new ExperimentSearchCriteria();
        searchCriteria.withPermId().thatEquals(exp.getPermId().toString());
        ExperimentFetchOptions expFetchOptions = new ExperimentFetchOptions();
        expFetchOptions.sortBy().code();
        expFetchOptions.withSamplesUsing(new SampleFetchOptions());

        SearchResult<Experiment> experiments = v3_api.searchExperiments(v3_sessionToken,
                searchCriteria, expFetchOptions);

        // Make sure that we found only one experiment
        assert(experiments.getTotalCount() == 1);

        // Get the experiment
        Experiment experiment = experiments.getObjects().get(0);

        // Get the samples
        List<Sample> samples = experiment.getSamples();

        // Return the samples
        return samples;
    }

    /**
     * Check whether the v3 API is initialized properly and the session is active.
     * @return true if the v3 API is initialized properly and the session is active,
     * false otherwise.
     */
    public boolean isLoggedIn() {

        // Check that the v3 API is initialized and the session is active
        // (i.e. we had a valid login).
        return (v3_api != null && v3_sessionToken != "");

    }

    /**
     * Create a project with given code in the specified space.
     * @param spaceCode Code of the project to be created.
     * @param projectCode Code of the space where the project will be created.
     * @return a list of ProjectPermId for the created projects.
     */
    public List<ProjectPermId> createProject(String spaceCode, String projectCode) {

        // Create a project with given space and project codes
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode(projectCode);
        projectCreation.setSpaceId(new SpacePermId(spaceCode));

        // Create the project
        List<ProjectCreation> projectCreationList = new ArrayList<ProjectCreation>();
        projectCreationList.add(projectCreation);

        List<ProjectPermId> createdProjects = v3_api.createProjects(v3_sessionToken, projectCreationList);

        // Now create the COLLECTIONs with CODE MICROSCOPY_EXPERIMENTS_COLLECTION and
        // FLOW_EXPERIMENTS_COLLECTION
        for (ProjectPermId id : createdProjects) {

            // Create collection MICROSCOPY_EXPERIMENTS_COLLECTION object
            ExperimentCreation micrExpCreation = new ExperimentCreation();
            micrExpCreation.setTypeId(new EntityTypePermId("COLLECTION"));
            micrExpCreation.setProjectId(id);
            micrExpCreation.setCode("MICROSCOPY_EXPERIMENTS_COLLECTION");
            micrExpCreation.setProperty("Name", "Microscopy Experiment Collection");

            // Create collection FLOW_CYTOMETRY_EXPERIMENTS_COLLECTION object
            ExperimentCreation flowExpCreation = new ExperimentCreation();
            flowExpCreation.setTypeId(new EntityTypePermId("COLLECTION"));
            flowExpCreation.setProjectId(id);
            flowExpCreation.setCode("FLOW_CYTOMETRY_EXPERIMENTS_COLLECTION");
            flowExpCreation.setProperty("Name", "Flow Cytometry Experiment Collection");

            // Create collection ORGANIZATION_UNITS_COLLECTION object
            ExperimentCreation orgUnitExpCreation = new ExperimentCreation();
            orgUnitExpCreation.setTypeId(new EntityTypePermId("COLLECTION"));
            orgUnitExpCreation.setProjectId(id);
            orgUnitExpCreation.setCode("ORGANIZATION_UNITS_COLLECTION");
            orgUnitExpCreation.setProperty("Name", "Organization Unit Collection");

            // Create the experiments
            List<ExperimentCreation> experimentCreationList = new ArrayList<ExperimentCreation>();
            experimentCreationList.add(micrExpCreation);
            experimentCreationList.add(flowExpCreation);
            experimentCreationList.add(orgUnitExpCreation);

            List<ExperimentPermId> createdExperiments = v3_api.createExperiments(v3_sessionToken, experimentCreationList);
            System.out.println(createdExperiments);

        }

        // Return the list of created projects
        return createdProjects;
    }

    /**
     * Create a metaproject (tag) with given code for current user.
     * @param metaprojectCode Code of the metaproject (tag) to be created.
     * @param metaprojectDescr Description for the metaproject (optional).
     * @return a QueryTableModel with one row containing "success" and "message"
     * column. You can query the content of the QueryTableModel as follows:
     *
     * <pre>
     * {@code
     *String success= "";
     *String message = "";
     *List<Serializable[]> rows = tableModel.getRows();
     *for (Serializable[] row : rows) {
     *	success = (String)row[0];
     *	message = (String)row[1];
     *	if (success.equals("true")) {
     *		System.out.println(message);
     *		return true;
     *	}
     *}
     *System.err.println(message);
     *}
     * </pre>
     */
    public boolean createMetaProject(String metaprojectCode,
            String metaprojectDescr) {

        // Set the parameters
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userName", this.userName);
        parameters.put("metaprojectCode", metaprojectCode);
        parameters.put("metaprojectDescr", metaprojectDescr);

        //            QueryTableModel tableModel =
        //                    queryFacade.get().createReportFromAggregationService(
        //                            createMetaProjectService, parameters);

        return false;
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

    // Add a class that extends thread that is to be called when program is exiting
    public class V3APILogoutOnShutdown extends Thread {

        IApplicationServerApi v3_api = null;
        String v3_sessionToken = "";

        public V3APILogoutOnShutdown(IApplicationServerApi v3_api, String v3_sessionToken) {
            this.v3_api = v3_api;
            this.v3_sessionToken = v3_sessionToken;
        }

        @Override
        public void run() {
            if (v3_api != null && (v3_sessionToken != null || !v3_sessionToken.equals(""))) {
                try {
                    System.out.println("Logging out from openBIS (v3 API)...");
                    v3_api.logout(v3_sessionToken);
                } catch (UserFailureException e) {
                    System.out.println("Could not log oug from openBIS: invalid session token.");
                } catch (Exception e) {
                    System.out.println("Could not log oug from openBIS: " + e.getMessage());
                }
            }
        }
    }

}
