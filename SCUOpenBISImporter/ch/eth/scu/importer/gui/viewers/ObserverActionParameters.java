package ch.eth.scu.importer.gui.viewers;

import ch.eth.scu.importer.gui.viewers.data.model.AbstractNode;

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
    public AbstractNode node = null;

    public ObserverActionParameters(
    		ObserverActionParameters.Action action, AbstractNode node) {
        this.action = action;
        this.node  = node;
    }
}
