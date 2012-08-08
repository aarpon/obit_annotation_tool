package ch.eth.scu.importer.gui.components.viewers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.eth.scu.importer.gui.descriptors.AbstractDescriptor;


/**
 * Create an XML document from a JTree
 * @author Aaron Ponti
 *
 */
public class CustomTreeToXML {

	private DocumentBuilder builder;
	private Document document;
	
	/**
	 * Constructor
	 * @param rootNode Root node of the JTree
	 */
	public CustomTreeToXML(CustomTree tree) {

		// Get the root node of the JTree
		CustomTreeNode rootNode = 
				(CustomTreeNode) tree.getModel().getRoot();
		
		// Create a Document
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.newDocument();
			
			Element root = document.createElement("xml");
			root.setAttribute("version", "1");

			addNode(root, rootNode);
				
			document.appendChild(root);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Save the XML document to file
	 * @param filename File name with full path
	 * @return true if saving was successful, false otherwise
	 */
	public boolean saveToFile(String filename) {

		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			OutputStream outputStream = new FileOutputStream(filename);
			t.transform(new DOMSource(document), new StreamResult(outputStream));
		} catch (TransformerException e) {
			e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Add a JTree node as a node to the XML document 
	 * @param parentNode	The XML parent node to which to append current
	 * @param treeNode		The JTree node to append
	 */
    protected void addNode(Element parentNode, CustomTreeNode treeNode) {
    	// DefaultMutableTreeNode (since Java 1.2) returns a raw enumeration.
    	// This causes a warning in Java > 5.
    	@SuppressWarnings("unchecked")
        Enumeration<CustomTreeNode> children = treeNode.children();
        while (children.hasMoreElements()) {
            final CustomTreeNode node = children.nextElement();
            final Element element = createElement(node);
            parentNode.appendChild(element);
            addNode(element, node);
        }
    }

    /**
     * Create an XML node from a JTree node
     * @param node JTree node from which an XML node is to be created  
     * @return an XML node
     */
    protected Element createElement(CustomTreeNode node) {
        final AbstractDescriptor data = (AbstractDescriptor)node.getUserObject();
        String tagName = node.getType();
        String tagAttr = data.toString();
        Element element;
        try {
        	
        	// Create the element
        	element = document.createElement(tagName);
        	
        	// Store all node attributes
        	Map<String, String> attributes = data.getAttributes();
        	for (String key: attributes.keySet() ) {
        		String value = attributes.get(key);
        		element.setAttribute(key, value);
        	}
        	
        	// Store the openBIS code and identifier as attributes 
        	element.setAttribute("openBISCode", data.getOpenBISICode());
        	element.setAttribute("openBISIdentifier", 
        			data.getOpenBISIdentifier());

        	// Set the name as an attribute as well
        	element.setAttribute("name", tagAttr);

        } catch (DOMException e) {
        	System.err.println("Element with name " + tagName + 
        			" could not be created.");
        	element = document.createElement("invalid");
        }
        return element;
    }    

}

