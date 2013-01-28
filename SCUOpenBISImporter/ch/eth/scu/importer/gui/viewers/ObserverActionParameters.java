package ch.eth.scu.importer.gui.viewers;

/**
 * Lists the actions supported by the observer pattern.
 *
 * This is used by the viewers (data and openBIS) to notify the editors that something has to be done.
 * @author Aaron Ponti
 */
public class ObserverActionParameters {

    public enum Action {
        SCAN_COMPLETE,              /** Scan of the data folder or the openBIS space is complete */
        EXPERIMENT_CHANGED          /** The user selected an experiment (or one of its children) in the data viewer */
    }

	public ObserverActionParameters.Action action;
    public String param = "";

    public ObserverActionParameters(ObserverActionParameters.Action action, String param) {
        this.action = action;
        this.param  = param;
    }
}
