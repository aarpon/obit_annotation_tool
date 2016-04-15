package ch.ethz.scu.obit.common.server.longrunning;

import java.util.Date;

/** A wrapper for the object to be stored in the LRCache that adds
 * a time stamp to its creation. 
 * @author Aaron Ponti
 *
 */
class LRCacheValue {
	
	/**
	 * Date of creation.
	 */
	private Date d;
	
	/**
	 * Object to be stored.
	 */
	private Object o;
	
	/**
	 * Constrcutor
	 * @param value Object to be stored.
	 */
    LRCacheValue(Object value) {
    	
    	this.d = new Date();
    	this.o = value;
    }

    /**
     * Get the date
     * @return Date object.
     */
	Date getDate() {
		return d;
	}

	/**
	 * Get the wrapped object.
	 * @return wrapped object.
	 */
	Object getObject() {
		return o;
	}
	
	public String toString() {
		return this.d.toString() + ": " + this.o.toString();
	}
}