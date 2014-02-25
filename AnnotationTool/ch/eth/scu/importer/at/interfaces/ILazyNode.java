package ch.eth.scu.importer.at.interfaces;

public interface ILazyNode {

	/**
	 * Return true if the node is a leaf (i.e. it cannot be expanded) or false
	 * if it is not (and can therefore be expanded). 
	 * This is essential for lazy loading.
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
