package ch.eth.scu.importer.processor;

import ch.eth.scu.importer.processor.model.AbstractDescriptor;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * BDFACSDIVAXMLProcessor parses "BD BioSciences FACSDiva" XML files.
 * @author Aaron Ponti
 */
public class BDFACSDIVAXMLProcessor extends AbstractProcessor {

	/* Private instance variables */
	private String xmlFilename;
	private DocumentBuilder parser = null;
	private Document doc = null;

	/* Public instance variables */
	public XMLFileDescriptor xmlFile;

	/**
	 * Constructor
	 * @param filename Name with full path of the file to be opened.
	 */
	public BDFACSDIVAXMLProcessor(String filename) {

		// Set the filename
		this.xmlFilename = filename;

		// Instantiate the factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true); 	// Ignore comments 
		factory.setCoalescing(true); 		// Convert CDATA to Text nodes 
		factory.setNamespaceAware(false); 	// No name spaces
		factory.setValidating(false);		// Don't validate DTD

		// Create and store a parser
		try {
			parser = factory.newDocumentBuilder();
		} catch ( ParserConfigurationException pce) {
			pce.printStackTrace();
			parser = null;
		}
	}

	/**
	 * Return information regarding the file format.
	 * @return descriptive String for the Processor.
	 */
	public String info() {
		return "BD BioSciences FACSDiva\u2122 Software";
	}

	/**
	 * Parse the file to extract data and metadata. 
	 * @return true if parsing was successful, false otherwise.
	 */
	@Override
	public boolean parse() {

		// Check the parser
		if (parser == null) {
			System.out.println("Invalid parser. Aborting.");
			return false;
		}

		// Parse the file
		try {
			doc = parser.parse(new File(xmlFilename));
			//doc.getDocumentElement().normalize();
		} catch (SAXException se) {
			System.out.println("The file appears to contain invalid XML.");
			doc = null;
			return false;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			doc = null;
			return false;
		}	

		// Extract info
		xmlFile = new XMLFileDescriptor(xmlFilename,doc);
		return xmlFile.success;

	}

	/**
	 * Return a String representation of the DBDIVAXMLProcessor.
	 * @return String containing a description of the DBDIVAXMLProcessor. 
	 */
	public String toString() {
		return (new File(xmlFilename)).getName();
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override
	public String getType() {
		return "BDFACSDIVA";
	}	

	/**
	 * Return a textual tree representation of the DBDIVAXMLProcessor 
	 * @return String containing a tree representation of the DBDIVAXMLProcessor
	 */	
	public String treeView() {
		return xmlFile.treeView();
	}

	/**
	 * Class that represents the full XML file.
	 * @author Aaron Ponti
	 *
	 */
	public class XMLFileDescriptor extends AbstractDescriptor {
		
		public boolean success = false;
		public ArrayList<ExperimentDescriptor> experiments = 
				new ArrayList<ExperimentDescriptor>();
		
		public XMLFileDescriptor(String name, Document doc) {
			
			// Set the descriptor name
			this.name = (new File(name)).getName();
			
			// Set the output name
			String outputNameFile = (new File(name)).getParent();
			int indx = outputNameFile.lastIndexOf(File.separator);
			if (indx != -1) {
				outputNameFile = outputNameFile.substring(indx + 1);
			}
			this.outputName = outputNameFile + "_properties.xml";

			// Process the document
			success = processDoc(doc);
		}

		/**
		 * Get XML output name
		 * 
		 * This is the name of the XML output file where to save the data model
		 * @return the name of the XML output file 
		 */
		public String getOutputName() {
			return outputName;
		}
		
		@Override
		public String getType() {
			return "XMLFile";
		}
		
		/**
		 * Parse and extract relevant information from the XML file.
		 * @return true if the parsing was successful, false otherwise.
		 */
		private boolean processDoc(Document doc) {

			// Nothing to parse, return
			if (doc == null) {
				System.out.println("Document not parsed. Aborting.");
				return false;
			}

			// Get the root node
			Node rootNode = doc.getDocumentElement();

			// Store the version information
			attributes.put("version", 
					rootNode.getAttributes().getNamedItem("version").getNodeValue());
			attributes.put("releaseVersion",
					rootNode.getAttributes().getNamedItem("release_version").getNodeValue());

			// Get children
			NodeList children = rootNode.getChildNodes();

			// Process them
			for ( int i = 0; i < children.getLength(); i++ ) {

				Node n = children.item(i);

				if (n.getNodeName().equals("experiment")) {

					// Tray
					experiments.add(new ExperimentDescriptor(n));

				} else {

					// Skip
				}
			}

			return true;
		}
		
		/**
		 * Return a textual tree representation of the XML file
		 * @return String containing a tree representation of the XML file
		 */	
		protected String treeView() {

			String str = "[XML file] " + toString() + " (" +
					attributesToString() + ").\n|\n";        

			for (ExperimentDescriptor e : experiments) {

				str +=  "[ Experiment ], name: " + e.getName() + " (" +
						e.attributesToString() + ").\n";

				for (TrayDescriptor t : e.trays) {

					str +=  "|__[ Tray ], name: " + t.getName() + " (" +
							t.attributesToString() + ").\n";

					for (SpecimenDescriptor s : t.specimens) {

						str +=  "|____[ Specimen ], name: " + s.getName() + " (" +
								s.attributesToString() + ").\n";

						for (TubeDescriptor tb : s.tubes) {

							str +=  "|______[ Tube ], name: " + tb.getName() + " (" +
									tb.attributesToString() + ").\n";
						}
					}

				}

				for (SpecimenDescriptor s : e.specimens) {

					str +=  "|__[ Specimen ], name: " + s.getName() + "(" +
							s.attributesToString() + ").\n";

					for (TubeDescriptor tb : s.tubes) {

						str +=  "|____[ Tube ], name: " + tb.getName() + " (" +
								tb.attributesToString() + ").\n";
					}
				}

			}
			
			return str;
		}

	}
	
	/**
	 * Class that represents an experiment parsed from the XML.
	 * @author Aaron Ponti
	 *
	 */
	public class ExperimentDescriptor extends AbstractDescriptor {

		// An Experiment can contain TRAYS that in turn contain SPECIMENs 
		// which contain TUBEs, or directly SPECIMENs containing TUBEs.

		/**
		 * ArrayList of Tray's
		 */
		public ArrayList<TrayDescriptor> trays = new ArrayList<TrayDescriptor>();

		/**
		 * ArrayList of Specimen's
		 */
		public ArrayList<SpecimenDescriptor> specimens = new ArrayList<SpecimenDescriptor>();

		/**
		 * Constructor
		 * @param expNode DOM node that refers to an Experiment.
		 */
		public ExperimentDescriptor(org.w3c.dom.Node expNode) {

			// Get the experiment name
			this.name = expNode.getAttributes().getNamedItem("name").getNodeValue();

			// Get children
			NodeList children = expNode.getChildNodes();

			// Process them
			for ( int i = 0; i < children.getLength(); i++ ) {

				Node n = children.item(i);

				if (n.getNodeName().equals("date")) {

					// Experiment date
					attributes.put("date", n.getTextContent());

				} else if (n.getNodeName().equals("owner_name")) {

					// Experiment owner
					attributes.put("owner_name", n.getTextContent());

				} else if (n.getNodeName().equals("tray")) {

					// Tray
					trays.add(new TrayDescriptor(n));

				} else if (n.getNodeName().equals("specimen")) {

					// Specimen  
					specimens.add(new SpecimenDescriptor(n));

				} else {

					// Skip
				}
			}
		}

		/**
		 * Return a simplified class name to use in XML.
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "Experiment";
		}
		
	}

	/**
	 * Class that represents a tube parsed from the XML.
	 * A Tube is always a child of a Specimen.
	 * @author Aaron Ponti
	 */
	public class TubeDescriptor extends AbstractDescriptor {
		
		/**
		 * Constructor.
		 * @param tubeNode DOM node that refers to a Tube.
		 */
		public TubeDescriptor(org.w3c.dom.Node tubeNode) {
	
			// Get the attributes
			NamedNodeMap attrs = tubeNode.getAttributes();
	
			name = attrs.getNamedItem("name").getNodeValue();
	
			// Get children
			NodeList children = tubeNode.getChildNodes();
	
			// Process them
			for ( int i = 0; i < children.getLength(); i++ ) {
	
				Node n = children.item(i);
	
				if (n.getNodeName().equals("date")) {
	
					// Tube date
					attributes.put("date", n.getTextContent());
	
				} else if (n.getNodeName().equals("data_filename")) {
	
					// Tube file name
					attributes.put("dataFilename", n.getTextContent());
					
					// Store also the full file name for quick retrieval
					File path = new File(xmlFilename);
					attributes.put("fullDataFilename",
							( path.getParent() + 
							File.separator + 
							attributes.get("dataFilename").toString()));
	
				} else {
	
					// Skip
				}
			}
	
		}
	
		/**
		 * Return a String representation of the extracted Tube node.
		 * @return String representation of the Tube node.
		 */
		@Override
		public String toString() {
			String str =  name + "   (" + attributes.get("dataFilename") + ")";
			return str;
		}
	
		/**
		 * Return a simplified class name to use in XML.
		 * @return simplidied class name.
		 */
		@Override		
		public String getType() {
			return "Tube";
		}
		
	}

	/**
	 * Class that represents a specimen parsed from the XML.
	 * A Specimen can be a child of a Tray or directly of an Experiment.
	 * @author Aaron Ponti
	 */
	public class SpecimenDescriptor extends AbstractDescriptor {
	
		/* Public instance variables */
	
		/**
		 * ArrayList of Tube's
		 */			
		public ArrayList<TubeDescriptor> tubes = new ArrayList<TubeDescriptor>();
	
		/**
		 * Constructor.
		 * @param specimenNode DOM node that refers to a Specimen.
		 */
		public SpecimenDescriptor(org.w3c.dom.Node specimenNode) {
	
			// Get the attributes
			NamedNodeMap attrs = specimenNode.getAttributes();
	
			name = attrs.getNamedItem("name").getNodeValue();
	
			// Get children
			NodeList children = specimenNode.getChildNodes();
	
			// Process them
			for ( int i = 0; i < children.getLength(); i++ ) {
	
				Node n = children.item(i);
	
				if (n.getNodeName().equals("tube")) {
	
					// Tray
					tubes.add(new TubeDescriptor(n));
	
				} else {
	
					// Skip
				}
			}
	
		}
	
		/**
		 * Return a simplified class name to use in XML.
		 * @return simplidied class name.
		 */
		@Override		
		public String getType() {
			return "Specimen";
		}

	}

	/**
	 * Class that represents a tray parsed from the XML.
	 * @author Aaron Ponti
	 */
	public class TrayDescriptor extends AbstractDescriptor {
	
		/* Public instance variables */
	
		/**
		 * ArrayList of Specimen's
		 */
		public ArrayList<SpecimenDescriptor> specimens = new ArrayList<SpecimenDescriptor>();
	
		/**
		 * Constructor
		 * @param trayNode DOM node that refers to a Tray.
		 */
		public TrayDescriptor(org.w3c.dom.Node trayNode) {
	
			// Get the attributes
			NamedNodeMap attrs = trayNode.getAttributes();

			// Name
			this.name = attrs.getNamedItem("name").getNodeValue();

			// Store the other attributes
			attributes.put("tray_type", 
					attrs.getNamedItem("tray_type").getNodeValue());
			attributes.put("rows", 
					attrs.getNamedItem("rows").getNodeValue());
			attributes.put("cols", 
					attrs.getNamedItem("cols").getNodeValue());
			attributes.put("orientation", 
					attrs.getNamedItem("Orientation").getNodeValue());
	
			// Get children
			NodeList children = trayNode.getChildNodes();
	
			// Process them
			for ( int i = 0; i < children.getLength(); i++ ) {
	
				Node n = children.item(i);
	
				if (n.getNodeName().equals("specimen")) {
	
					// Specimen  
					specimens.add(new SpecimenDescriptor(n));
	
				} else {
	
					// Skip
				}
	
			}
	
		}
	
		/**
		 * Return a simplified class name to use in XML.
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "Tray";
		}

	}
	
}
