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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
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

    private AtomicReference<String> loginErrorMessage =
            new AtomicReference<String>("");
    private AtomicReference<String> loginErrorTitle =
            new AtomicReference<String>("");
    private AtomicBoolean loginErrorRecoverable = new AtomicBoolean(true);

    // Cache spaces and projects
    private boolean dataIsCached = false;
    private boolean tagCollectionsAreCached = false;
    private SearchResult<Space> cachedSpacesWithProjects = null;
    private Map<Space, Project> tagsCollectionPerSpace = null;

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
        this.tagCollectionsAreCached = false;
        this.cachedSpacesWithProjects = null;
        this.tagsCollectionPerSpace = null;
    }

    /**
     * Invalidate the data cache to force a rescan of openBIS.
     */
    public void invalidateDataCache() {
        dataIsCached = false;
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
     * Reset the cached data.
     */
    public void resetData() {

        this.dataIsCached = false;
        this.tagCollectionsAreCached = false;
        this.cachedSpacesWithProjects = null;
        this.tagsCollectionPerSpace = null;
    }

    /**
     * Returns the list of Spaces in openBIS (as visible for current user).
     *
     * The spaces (with the contained projects) are cached.
     * On a second call, the cached version is returned. To force a retrieval,
     * call resetData() first.  Also, references to COMMON_ORGANISATION_UNIT
     * projects (common 'tags') are cached.
     *
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

        // Search openBIS
        cachedSpacesWithProjects = v3_api.searchSpaces(v3_sessionToken,
                searchCriteria, spaceFetchOptions);

        // Update cache information
        dataIsCached = true;

        // Cache the space tag collections
        cacheTagCollections();

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

        // We need to retrieve the experiments from openBIS
        ProjectSearchCriteria searchCriteria = new ProjectSearchCriteria();
        searchCriteria.withCode();
        searchCriteria.withPermId().thatEquals(p.getPermId().toString());
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.sortBy().code();

        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withProperties();
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

        if (exp == null) {
            return new ArrayList<Sample>();
        }

        // Search for Experiments with given code and fetch its samples
        ExperimentSearchCriteria searchCriteria = new ExperimentSearchCriteria();
        searchCriteria.withPermId().thatEquals(exp.getPermId().toString());
        ExperimentFetchOptions expFetchOptions = new ExperimentFetchOptions();
        expFetchOptions.sortBy().code();

        // Make sure to retrieve the Sample properties
        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withType();
        sampleFetchOptions.withProperties();
        sampleFetchOptions.withSpace();
        sampleFetchOptions.withProject();
        expFetchOptions.withSamplesUsing(sampleFetchOptions);

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
     * Return the tags for the given Space.
     *
     * A tag is a sample of type ORGANIZATION_UNIT stored in the
     * experiment ORGANIZATION_UNITS_COLLECTION.
     *
     * The tags are cached.
     *
     * @param space Space to be queried.
     * @return list of tags.
     */
    public List<Sample> getTagsForSpace(Space space) {

        // Does the Space have a tags collection?
        Project collection = tagsCollectionPerSpace.get(space);
        if (collection == null) {
            return new ArrayList<Sample>();
        }

        // Retrieve the experiments
        List<Experiment> experiments = getExperimentsForProjects(collection);

        // Get the ORGANIZATION_UNITS_COLLECTION experiment
        Experiment orgUnits = null;
        for (int i = 0; i < experiments.size(); i++) {
            Experiment current = experiments.get(i);
            if (current.getCode().equals("ORGANIZATION_UNITS_COLLECTION")) {
                orgUnits = current;
                break;
            }
        }

        // Get samples
        List<Sample> tags = getSamplesForExperiments(orgUnits);

        // Return them
        return tags;

    }

    /**
     * Return the tags for the given Space.
     *
     * A tag is a sample of type ORGANIZATION_UNIT stored in the
     * experiment ORGANIZATION_UNITS_COLLECTION.
     *
     * The tags are cached.
     *
     * @param space Space to be queried.
     * @return list of tags.
     */
    public List<String> getTagNamesForSpace(Space space) {

        // Does the Space have a tags collection?
        List<Sample> tags = getTagsForSpace(space);
        if (tags == null) {
            return new ArrayList<String>();
        }

        // Create a list of tag names
        ArrayList<String> tagNamesList = new ArrayList<String>();

        for (Sample tag: tags) {

            tagNamesList.add(tag.getCode());
        }

        // Return them
        return tagNamesList;
    }

    /**
     * Check whether the v3 API is initialized properly and the session is active.
     * @return true if the v3 API is initialized properly and the session is active,
     * false otherwise.
     */
    public boolean isLoggedIn() {

        // Check that the v3 API is initialized and the session is active
        // (i.e. we had a valid login).
        return (v3_api != null && !v3_sessionToken.equals(""));

    }

    /**
     * Create a project with given code in the specified space.
     * @param spaceCode Code of the project to be created.
     * @param projectCode Code of the space where the project will be created.
     * @param createDataCollections Set to true to create collection for MICROSCOPY
     *        and FLOW CYTOMETRY data along with the collection for organization
     *        units (tags); set to false to only create the collection for
     *        organization units.
     * @return a list of ProjectPermId for the created projects.
     */
    public List<ProjectPermId> createProject(String spaceCode, String projectCode, boolean createDataCollections) {

        // Create a project with given space and project codes
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode(projectCode);
        projectCreation.setSpaceId(new SpacePermId(spaceCode));

        // Create the project
        List<ProjectCreation> projectCreationList = new ArrayList<ProjectCreation>();
        projectCreationList.add(projectCreation);

        List<ProjectPermId> createdProjects = v3_api.createProjects(v3_sessionToken, projectCreationList);

        // Now create the COLLECTIONs with CODE MICROSCOPY_EXPERIMENTS_COLLECTION,
        // FLOW_EXPERIMENTS_COLLECTION and ORGANIZATION_UNITS_COLLECTION.
        for (ProjectPermId id : createdProjects) {

            // Create collection ORGANIZATION_UNITS_COLLECTION object
            ExperimentCreation orgUnitExpCreation = new ExperimentCreation();
            orgUnitExpCreation.setTypeId(new EntityTypePermId("COLLECTION"));
            orgUnitExpCreation.setProjectId(id);
            orgUnitExpCreation.setCode("ORGANIZATION_UNITS_COLLECTION");
            orgUnitExpCreation.setProperty("$NAME", "Organization Unit Collection");

            // Create the experiment creation list
            List<ExperimentCreation> experimentCreationList = new ArrayList<ExperimentCreation>();
            experimentCreationList.add(orgUnitExpCreation);

            if (createDataCollections == true) {

                // Create collection MICROSCOPY_EXPERIMENTS_COLLECTION object
                ExperimentCreation micrExpCreation = new ExperimentCreation();
                micrExpCreation.setTypeId(new EntityTypePermId("COLLECTION"));
                micrExpCreation.setProjectId(id);
                micrExpCreation.setCode("MICROSCOPY_EXPERIMENTS_COLLECTION");
                micrExpCreation.setProperty("$NAME", "Microscopy experiments collection");

                // Create collection FLOW_SORTERS_EXPERIMENTS_COLLECTION object
                ExperimentCreation flowSorterExpCreation = new ExperimentCreation();
                flowSorterExpCreation.setTypeId(new EntityTypePermId("COLLECTION"));
                flowSorterExpCreation.setProjectId(id);
                flowSorterExpCreation.setCode("FLOW_SORTERS_EXPERIMENTS_COLLECTION");
                flowSorterExpCreation.setProperty("$NAME", "Flow sorters experiments collection");

                // Create collection FLOW_ANALYZERS_EXPERIMENTS_COLLECTION object
                ExperimentCreation flowAnalyzersExpCreation = new ExperimentCreation();
                flowAnalyzersExpCreation.setTypeId(new EntityTypePermId("COLLECTION"));
                flowAnalyzersExpCreation.setProjectId(id);
                flowAnalyzersExpCreation.setCode("FLOW_ANALYZERS_EXPERIMENTS_COLLECTION");
                flowAnalyzersExpCreation.setProperty("$NAME", "Flow analyzers experiment collection");

                // Add to the creation list
                experimentCreationList.add(micrExpCreation);
                experimentCreationList.add(flowSorterExpCreation);
                experimentCreationList.add(flowAnalyzersExpCreation);

            }

            // Create the experiments
            v3_api.createExperiments(v3_sessionToken, experimentCreationList);
        }

        // Invalidate cache
        dataIsCached = false;

        // Return the list of created projects
        return createdProjects;
    }

    /**
     * Create a <b>commong tag</b> with given code in given space.
     * @param space Space where to create the new tag.
     * @param tagCode Code of the tag to be created.
     * @param tagDescr Description for the tag (optional).
     * @return true if the tag could be created, false otherwise.
     *
     * Please notice that is the space does not contain a
     * COMMON_ORGANIZATION_UNITS, this will be created.
     */
    public boolean createTag(Space space, String tagCode, String tagDescr) {

        // Check if the project COMMON_ORGANIZATION_UNITS exists
        Project commonOrganizationUnitProject = tagsCollectionPerSpace.get(space);
        if (commonOrganizationUnitProject == null) {

            // Create the COMMON_ORGANIZATION_UNITS project
            commonOrganizationUnitProject = createTagContainerProjectAndExperiment(space);
            if (commonOrganizationUnitProject == null) {
                return false;
            }
        }

        // Get the ORGANIZATION_UNITS_COLLECTION collection
        ExperimentSearchCriteria searchCriteria = new ExperimentSearchCriteria();
        searchCriteria.withCode().thatEquals("ORGANIZATION_UNITS_COLLECTION");
        searchCriteria.withProject().withPermId().thatEquals(commonOrganizationUnitProject.getPermId().toString());
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        SearchResult<Experiment> experiments = v3_api.searchExperiments(v3_sessionToken,
                searchCriteria, fetchOptions);
        if (experiments.getTotalCount() == 0) {

            // Create collection ORGANIZATION_UNITS_COLLECTION object
            ExperimentCreation orgUnitExpCreation = new ExperimentCreation();
            orgUnitExpCreation.setTypeId(new EntityTypePermId("COLLECTION"));
            orgUnitExpCreation.setProjectId(commonOrganizationUnitProject.getPermId());
            orgUnitExpCreation.setCode("ORGANIZATION_UNITS_COLLECTION");
            orgUnitExpCreation.setProperty("$NAME", "Organization Unit Collection");

            // Create the experiments
            List<ExperimentCreation> experimentCreationList = new ArrayList<ExperimentCreation>();
            experimentCreationList.add(orgUnitExpCreation);
            List<ExperimentPermId> experimentIds = v3_api.createExperiments(v3_sessionToken, experimentCreationList);

            // Now fetch the newly created Experiment
            ExperimentSearchCriteria newExpSearchCriteria = new ExperimentSearchCriteria();
            newExpSearchCriteria.withCode().thatEquals("ORGANIZATION_UNITS_COLLECTION");
            newExpSearchCriteria.withPermId().thatEquals(experimentIds.get(0).toString());
            newExpSearchCriteria.withProject().withPermId().thatEquals(commonOrganizationUnitProject.getPermId().toString());
            ExperimentFetchOptions newExpFetchOptions = new ExperimentFetchOptions();
            experiments = v3_api.searchExperiments(v3_sessionToken, newExpSearchCriteria, newExpFetchOptions);

            if (experiments.getTotalCount() == 0) {
                // Could not create the collection!
                return false;
            }
        }

        // Get the Experiment
        Experiment experiment = experiments.getObjects().get(0);

        // Add the Tag (a sample of type ORGANIZATION_UNIT; the code is auto-generated)
        SampleCreation orgUnitCreation = new SampleCreation();
        orgUnitCreation.setTypeId(new EntityTypePermId("ORGANIZATION_UNIT"));
        orgUnitCreation.setSpaceId(space.getPermId());
        orgUnitCreation.setProjectId(commonOrganizationUnitProject.getPermId());
        orgUnitCreation.setExperimentId(experiment.getPermId());

        // Set name and description as the properties $NAME and DESCRIPTION
        orgUnitCreation.setProperty("$NAME", tagCode);
        orgUnitCreation.setProperty("DESCRIPTION", tagDescr);

        // Create the sample
        List<SampleCreation> sampleCreationList = new ArrayList<SampleCreation>();
        sampleCreationList.add(orgUnitCreation);
        List<SamplePermId> createdSamples = v3_api.createSamples(v3_sessionToken, sampleCreationList);

        if (createdSamples == null || createdSamples.size() == 0) {
            return false;
        }

        return true;
    }

    /**
     * Create the COMMON_ORGANIZATION_UNITS Project in the given Space.
     * @param space Space where to create the COMMON_ORGANIZATION_UNITS Project.
     * @return the created COMMON_ORGANIZATION_UNITS Project or null if creation failed.
     */
    private Project createTagContainerProjectAndExperiment(Space space) {

        // Create a project with given space and project code "COMMON_ORGANIZATION_UNITS"
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode("COMMON_ORGANIZATION_UNITS");
        projectCreation.setSpaceId(space.getPermId());

        // Create the project
        List<ProjectCreation> projectCreationList = new ArrayList<ProjectCreation>();
        projectCreationList.add(projectCreation);

        List<ProjectPermId> createdProjects = v3_api.createProjects(v3_sessionToken, projectCreationList);
        if (createdProjects.size() == 0) {
            return null;
        }

        // Get the Project permId
        ProjectPermId permId = createdProjects.get(0);

        // We need to retrieve the Projects from openBIS
        ProjectSearchCriteria searchCriteria = new ProjectSearchCriteria();
        searchCriteria.withPermId().thatEquals(permId.toString());
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        SearchResult<Project> projects = v3_api.searchProjects(v3_sessionToken,
                searchCriteria, projectFetchOptions);

        if (projects.getTotalCount() == 0) {
            return null;
        }

        // Get the newly created Project object
        Project createdProject = projects.getObjects().get(0);

        List<ExperimentPermId> createdExperiments = null;

        // Now create the COLLECTION with CODE ORGANIZATION_UNITS_COLLECTION
        ExperimentCreation orgUnitExpCreation = new ExperimentCreation();
        orgUnitExpCreation.setTypeId(new EntityTypePermId("COLLECTION"));
        orgUnitExpCreation.setProjectId(permId);
        orgUnitExpCreation.setCode("ORGANIZATION_UNITS_COLLECTION");
        orgUnitExpCreation.setProperty("$NAME", "Organization Unit Collection");

        // Create the experiment
        List<ExperimentCreation> experimentCreationList = new ArrayList<ExperimentCreation>();
        experimentCreationList.add(orgUnitExpCreation);

        createdExperiments = v3_api.createExperiments(v3_sessionToken, experimentCreationList);

        if (createdExperiments == null || createdExperiments.size() == 0) {
            return null;
        }

        // Return the newly created Project
        return createdProject;
    }

    /**
     * Caches the list of tag collections for all spaces.
     */
    private void cacheTagCollections() {

        if (! dataIsCached || cachedSpacesWithProjects.getTotalCount() == 0) {
            this.tagsCollectionPerSpace = null;
            this.tagCollectionsAreCached = false;
            return;
        }

        // Initialize map
        tagsCollectionPerSpace = new HashMap<Space, Project>();

        // Store references to COMMON_ORGANISATION_UNIT projects (tag collections)
        if (cachedSpacesWithProjects.getTotalCount() > 0) {
            for (Space s : cachedSpacesWithProjects.getObjects()) {
                for (Project p : s.getProjects()) {
                    if (p.getCode().equals("COMMON_ORGANIZATION_UNITS")) {
                        tagsCollectionPerSpace.put(s, p);
                    }
                }
            }
        }

        // Set cached flag to true
        this.tagCollectionsAreCached = true;
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
