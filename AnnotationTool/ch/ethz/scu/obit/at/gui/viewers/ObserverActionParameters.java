package ch.ethz.scu.obit.at.gui.viewers;

import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;

/**
 * Lists the actions supported by the observer pattern.
 *
 * This is used by the viewers (data and openBIS) to notify the editors that 
 * something has to be done. The matching editors and viewers (per hardware 
 * category) might decide which of the following actions to implement.
 * 
 * @author Aaron Ponti
 */
public class ObserverActionParameters {

    /**
     * @author Aaron Ponti
     * Possible actions.
     *
     */
    public enum Action {
        /**
         * Action for the observers.
         */
        ABOUT_TO_RESCAN,             /** About to start a rescan of data or openBIS: metadata must be reset */
        SCAN_COMPLETE,               /** Scan of the data folder or the openBIS space is complete */
        EXPERIMENT_CHANGED,          /** The user selected a new experiment (or one of its children) in the data viewer */
        FILE_CHANGED,                /** The user selected another file in the data viewer */
        ABOUT_TO_SCAN_INCREMENTALLY, /** Some data node is being expanded  (lazy loading): metadata does not need to be reset */
        INCREMENTAL_SCAN_COMPLETE,   /** Incremental scan is complete. */
        READY_TO_SEND                /** The data is ready to be sent to openBIS. */   
    }

	/**
	 * Action
	 */
	public ObserverActionParameters.Action action;
    
	/**
     * (Viewer) Node that triggered the action.  
     */
    public AbstractNode node = null;

    /**
     * Constructor
     * @param action Action to be passed to the obeservers (editors).
     * @param node Node that triggered the action (from the viewers).
     */
    public ObserverActionParameters(
    		ObserverActionParameters.Action action, AbstractNode node) {
        this.action = action;
        this.node  = node;
    }
}
