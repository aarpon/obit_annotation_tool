package ch.eth.scu.importer.proc.processor.model;

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
