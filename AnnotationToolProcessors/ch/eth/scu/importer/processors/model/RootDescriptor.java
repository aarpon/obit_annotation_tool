package ch.eth.scu.importer.processors.model;

/**
 * The RootDescriptor is the top node in the data model tree.
 * 
 * Its children MUST be of type FirstLevelDescriptor.
 * 
 * @author Aaron Ponti
 *
 */
public class RootDescriptor extends AbstractDescriptor{

	/**
	 * Constructor.
	 * @param rootString String to be displayed at the root node.
	 */
	public RootDescriptor(String rootString) {
		this.name = rootString;
	}
	
	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Root";
	}

}
