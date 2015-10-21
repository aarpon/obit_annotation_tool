package ch.ethz.scu.obit.at.gui.viewers.data.view;

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

import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.at.gui.viewers.data.model.ExperimentNode;
import ch.ethz.scu.obit.at.gui.viewers.data.model.RootNode;
import ch.ethz.scu.obit.processors.data.model.AbstractDescriptor;
import ch.ethz.scu.obit.processors.data.model.ExperimentDescriptor;


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
	 * @param userName user name.
	 */
	public DataViewerTreeToXML(DataViewerTree tree, String userName) {

		DocumentBuilder builder;
		Document document = null;

		// Get the root node of the JTree
		RootNode rootNode = 
				(RootNode) tree.getModel().getRoot();

		// We create and save an XML file for each Experiment in the
		// data model. Experiments are at children of the Tree root,
		// which is the UserFolder. The name and path of each XML file
		// is obtained from the corresponding ExperimentDescriptor. 

		// Get all children of the rootNode - these are the Experiments
		int nTopLevelChildren = rootNode.getChildCount();

		for (int i = 0; i < nTopLevelChildren; i++) {

			// Get current Experiment
			ExperimentNode expNode = 
					(ExperimentNode) rootNode.getChildAt(i);

			ExperimentDescriptor expDescr = 
					(ExperimentDescriptor) expNode.getUserObject();

			// Construct the properties file name (which we also use
			// as key for the map)
			String key = expDescr.getPropertiesFileNameWithRelPath();

			// Now build the XML document for current experiment
			try {
				builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				document = builder.newDocument();

				// Create root element
				Element root = document.createElement("obitXML");
				root.setAttribute("version", "1");
				root.setAttribute("userName", userName);

				// Create and add the experiment
				Element element = createElement(document, expNode); 
				root.appendChild(element);

				// Add all its children (recursively)
				addNodeChildren(document, element, expNode);

				// Add the whole tree to the document (by adding the root node)
				document.appendChild(root);

			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

			// Store the document
			documents.put(key, document);

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
				Document document = documents.get(key);
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

