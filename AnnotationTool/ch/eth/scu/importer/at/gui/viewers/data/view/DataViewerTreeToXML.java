package ch.eth.scu.importer.at.gui.viewers.data.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
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

import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.processors.model.AbstractDescriptor;
import ch.eth.scu.importer.processors.model.PathAwareDescriptor;


/**
 * Create an XML document from a JTree
 * @author Aaron Ponti
 *
 */
public class DataViewerTreeToXML {

	protected Map<String, Document> documents = 
			new Hashtable<String, Document>();
	
	/**
	 * Constructor
	 * @param tree Custom JTree
	 */
	public DataViewerTreeToXML(DataViewerTree tree) {

		DocumentBuilder builder;
		Document document = null;

		// Get the root node of the JTree
		AbstractNode rootNode = 
				(AbstractNode) tree.getModel().getRoot();

		// We create and save an XML file for each top-level children in the
		// data model. The name and path of the XML file is obtained from the 
		// top node.

		// Get all children of the rootNode
		int nTopLevelChildren = rootNode.getChildCount();

		for (int i = 0; i < nTopLevelChildren; i++) {

			// Get current child
			AbstractNode topNode = (AbstractNode) rootNode.getChildAt(i);

			AbstractDescriptor topDescr = 
					(AbstractDescriptor) topNode.getUserObject();

			// Get the top level descriptor: top level descriptor MUST 
			// extend the PathAwareDescriptor abstract class - i.e. it
			// must be mappable to a folder (and has to know its own
			// absolute path)
			assert (topDescr instanceof PathAwareDescriptor);

			// Now go over all children - each child will have its own
			// properties XML file for registration stored in the
			// folder mapped by the firstLevelDescriptor. The name of the
			// properties file will be the name of the experiment.
			int nSecondLevelChildren = topNode.getChildCount();
			for (int j = 0; j < nSecondLevelChildren; j++) {

				// Get current child
				AbstractNode secondLevelNode = 
						(AbstractNode) topNode.getChildAt(j);

				AbstractDescriptor secondLevelDescr = 
						(AbstractDescriptor) secondLevelNode.getUserObject();
				
				// Construct the properties file name (which we also use
				// as key for the map)
				String key =
					((PathAwareDescriptor)topDescr).getRelativePath() +
					File.separator + secondLevelDescr.getName() +
					"_properties.six";
				
				// Now build the XML document
				try {
					builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					document = builder.newDocument();

					Element root = document.createElement("obitXML");
					root.setAttribute("version", "1");
					
					// Add the second level node
					root.appendChild(
							createElement(document, secondLevelNode));
					
					// Now add all children recursively 
					addNodeChildren(document, root, secondLevelNode);

					document.appendChild(root);

				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}

				// Store the document
				documents.put(key, document);

			}

		}

	}

	/**
	 * Save the XML document to file
	 * @param outputDirectory Directory where XML property files are saved
	 * @return true if saving was successful, false otherwise
	 */
	public boolean saveToFile(String outputDirectory) {

		for (String key: documents.keySet()) {

			// Build the filename (with full path)
			String filename = outputDirectory + File.separator + key;

			try {
				Document document = (Document) documents.get(key);
				Transformer t = 
						TransformerFactory.newInstance().newTransformer();
				OutputStream outputStream = new FileOutputStream(filename);
				t.transform(new DOMSource(document), 
						new StreamResult(outputStream));
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} catch (TransformerException e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	/**
	 * Add a all children of JTree node as nodes to the XML document 
	 * @param parentNode		The XML parent node to which to append
	 * @param treeNode		The JTree node to append
	 */
	protected void addNodeChildren(Document document, Element parentNode,
			AbstractNode treeNode) {
		// DefaultMutableTreeNode (since Java 1.2) returns a raw enumeration.
		// This causes a warning in Java > 5.
		@SuppressWarnings("unchecked")
		Enumeration<AbstractNode> children = treeNode.children();
		while (children.hasMoreElements()) {
			final AbstractNode node = children.nextElement();
			final Element element = createElement(document, node);
			parentNode.appendChild(element);
			addNodeChildren(document, element, node);
		}
	}

	/**
	 * Create an XML node from a JTree node
	 * @param node JTree node from which an XML node is to be created  
	 * @return an XML node
	 */
	protected Element createElement(Document document, AbstractNode node) {
		final AbstractDescriptor data = (AbstractDescriptor)node.getUserObject();
		String tagName = node.getType();
		String tagAttr = data.toString();
		Element element;
		try {

			// Create the element
			element = document.createElement(tagName);

			// Store all openBIS node attributes
			Map<String, String> openBISAttributes = data.getOpenBISAttributes();
			for (String key: openBISAttributes.keySet() ) {
				String value = openBISAttributes.get(key);
				element.setAttribute(key, value);
			}

			// Store all user node attributes
			Map<String, String> userAttributes = data.getUserAttributes();
			for (String key: userAttributes.keySet() ) {
				String value = userAttributes.get(key);
				element.setAttribute(key, value);
			}
			
			// Store all node attributes
			Map<String, String> attributes = data.getAttributes();
			for (String key: attributes.keySet() ) {
				String value = attributes.get(key);
				element.setAttribute(key, value);
			}

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

