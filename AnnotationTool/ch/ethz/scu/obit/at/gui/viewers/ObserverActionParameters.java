package ch.ethz.scu.obit.at.gui.viewers;

import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;

/**
 * Lists the actions supported by the observer pattern.
 *
 * This is used by the viewers (data and openBIS) to notify the editors that something has to be done.
 * @author Aaron Ponti
 */
public class ObserverActionParameters {

    public enum Action {
        ABOUT_TO_RESCAN,             /** About to start a rescan of data or
                                         openBIS -> metadata must be reset */
        SCAN_COMPLETE,               /** Scan of the data folder or the openBIS
                                         space is complete */
        EXPERIMENT_CHANGED,          /** The user selected a new experiment (or
                                         one of its children) in the data 
                                         viewer */
        ABOUT_TO_SCAN_INCREMENTALLY, /** Some data node is being expanded 
                                         (lazy loading) -> metadata does not
                                         need to be reset */
        INCREMENTAL_SCAN_COMPLETE,   /** Incremental scan is complete. */
        READY_TO_SEND                /** The data is ready to be sent to
                                         openBIS. */   
    }

	public ObserverActionParameters.Action action;
    public AbstractNode node = null;

    public ObserverActionParameters(
    		ObserverActionParameters.Action action, AbstractNode node) {
        this.action = action;
        this.node  = node;
    }
}
