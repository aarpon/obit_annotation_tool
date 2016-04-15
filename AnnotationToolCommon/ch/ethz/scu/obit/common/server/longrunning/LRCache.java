package ch.ethz.scu.obit.common.server.longrunning;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Store objects in a key-value concurrent map to be used for long-running 
 * plug-ins that are queried for state at regular intervals from a client app.
 * 
 * The key should be a client-specific unique ID. 
 * 
 * The object stored in the map is wrapped into a 
 * 
 * @author Aaron Ponti on a suggestion by:
 * 	Juan Fuentes Serna (ID SIS) <juan.fuentes@id.ethz.ch>
 * 
 */
public class LRCache {

	/**
	 * Logger
	 */
	private static final Logger operationLog = 
			LogFactory.getLogger(LogCategory.OPERATION,
					LRCache.class);

	/**
	 * ConcurrentHashMap to store the passed object for the given key. 
	 */
    private static final Map<String, LRCacheValue> store =
    		new ConcurrentHashMap<String, LRCacheValue>();
    
    /**
     * Maximum age for an entry in the HashMap. If an entry is older than 24 hours,
     * it will be removed from the HashMap.
     */
    private static final int entryMaxAgeInMilliSeconds = 60 * 60 * 24 * 1000;

    /**
     * Store a key-value pair.
     * @param key Key for the Map.
     * @param value An object to be stored.
     */
    public static void set(String key, Object value) {
    	
    	// Wrap the object into a LRCacheValue
    	LRCacheValue v = new LRCacheValue(value);
    	
    	// Log 
    	operationLog.info("LRCache: storing value " + value.toString() + 
    			" for key " + key);
        
    	// Store the value
    	store.put(key, v);
    	
    	// Remove entries from the map older than entryMaxAgeInMilliSeconds
    	clean();
    }

    /**
     * Retrieve the value for a given key.
     * @param key Key for which the value (object) should be retrieved.
     * @return the Object, or null if the key is not found in the Map.
     */
    public static Object get(String key) {
    	
    	// If the object for the given key does not exist, return null,
    	// otherwise return the contained object.
        if (! store.containsKey(key)) {

        	// Return null
            return null;

        } else {

        	// Log 
        	operationLog.info("LRCache: retrieving value for key " + key);
        	
        	// Return the wrapped object
        	return store.get(key).getObject();

        }
    }

    /**
     * Clean old jobs from the map.
     */
    public static void clean() {
    	
    	// Get current date and time
    	Date now = new Date();

    	// Compare all dates from all entries with current and delete the 
    	// old ones
    	for (Map.Entry<String, LRCacheValue> entry : store.entrySet()) {
    		
    		// Remove old entries from the Map
    		if (entry.getValue().getDate().getTime()
    				- now.getTime() > entryMaxAgeInMilliSeconds) {
    			
            	// Log 
            	operationLog.info("LRCache: removing entry " + 
            	entry.getKey() + " from cache");

            	// Remove the entry
    			store.remove(entry.getKey());

    		}
    	}
    }
    
    /**
     * Dumps the content to the hash map to log (for debugging purposes).
     */
    public static void dump() {
    	
    	// Log 
    	operationLog.info("LRCache: " + store.size() + " entries");
    	
    	for (Map.Entry<String, LRCacheValue> entry : store.entrySet()) {
    		
    		// Log 
        	operationLog.info(entry.getKey() + ": " + entry.getValue().toString());

    	}
    }
}
