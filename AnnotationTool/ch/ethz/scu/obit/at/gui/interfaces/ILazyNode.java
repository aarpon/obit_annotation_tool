package ch.ethz.scu.obit.at.gui.interfaces;

/**
 * @author Aaron Ponti
 * Interface for a lazy-loaded node.
 */
public interface ILazyNode {

	/**
	 * Return true if the node is a leaf (i.e. it cannot be expanded) or false
	 * if it is not (and can therefore be expanded). 
	 * This is essential for lazy loading.
	 * @return true if the node is a leaf, false otherwise.
	 */
	public boolean isLeaf();
	
	/**
	 * Return true if the node children were (lazy) loaded already.
	 * @return true if the children were loaded, false otherwise.
	 */
	public boolean isLoaded();

	/**
	 * Call this to flag a node as loaded and prevent its reloading.
	 */
	public void setLoaded();
}
