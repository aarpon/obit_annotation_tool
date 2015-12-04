package ch.ethz.scu.obit.common.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store objects in a key-value concurrent map to be used for long-running 
 * plug-ins that are queried for state at regular intervals from a client app.
 * 
 * The key should be a client-specific unique ID. 
 * 
 * @author Juan Fuentes Serna (ID SIS) <juan.fuentes@id.ethz.ch>
 * Modified by Aaron Ponti
 * 
 */
public class LongCallsResults
{
    private static final Map<String, Object> store =
    		new ConcurrentHashMap<String, Object>();

    /**
     * Store a key-value pair.
     * @param key Key for the Map.
     * @param value An object to be stored.
     */
    public static void set(String key, Object value) {
        store.put(key, value);
    }

    /**
     * Retrieve the value for a given key.
     * @param key Key for which the value (object) should be retrieved.
     * @return the Object, or null if the key is not found in the Map.
     * 
     * @TODO Fix the behavior: the key should be not be deleted after every
     * query, but only when requested.
     * 
     */
    public static Object get(String key) {
        if (! store.containsKey(key)) {
            return null;
        } else {
            Object result = store.get(key);
            store.remove(key);
            return result;
        }
    }

    /**
     * Remove the key-value pair for a given key.
     * @param key Key for which the key-value pair should be removed.
     */
    public static void remove(String key) {
        if (store.containsKey(key)) {
            store.remove(key);
        }
    }
}
