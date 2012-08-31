package ch.eth.scu.importer.processor;

import ch.eth.scu.importer.common.properties.AppProperties;
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

	/* Protected instance variables */
	protected File incomingDir;

	/* Public instance variables */
	public XMLFileDescriptor xmlFile;

	/**
	 * Constructor
	 * @param filename Name with full path of the file to be opened.
	 */
	public BDFACSDIVAXMLProcessor(String filename, String userName) {

		// Set the filename
		this.xmlFilename = filename;

		// Store the incoming dir (to build relative paths)
		Properties appProperties = AppProperties.readPropertiesFromFile();
		this.incomingDir = new File(
				appProperties.getProperty("DatamoverIncomingDir") +
				File.separator + userName);

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
		return "BD BioSciences FACSDiva\u2122 Software: experiment" +
				" export (with XML file)";
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
	 * Return a String representation of the BDFACSDIVAXMLProcessor.
	 * @return String containing a description of the BDFACSDIVAXMLProcessor. 
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
	 * Return a textual tree representation of the BDFACSDIVAXMLProcessor 
	 * @return String containing a tree representation of the 
	 * BDFACSDIVAXMLProcessor
	 */	
	public String treeView() {
		return xmlFile.treeView();
	}

	/**
	 * Descriptor representing represents the full XML file.
	 * @author Aaron Ponti
	 *
	 */
	public class XMLFileDescriptor extends AbstractDescriptor {
		
		public boolean success = false;
		public Set<ExperimentDescriptor> experiments = 
				new LinkedHashSet<ExperimentDescriptor>();
		
		public XMLFileDescriptor(String name, Document doc) {
			
			// Set the descriptor name
			this.name = (new File(name)).getName();
			
			// Set the output name
			String outputNameFile = (new File(name)).getParent();
			int indx = outputNameFile.lastIndexOf(File.separator);
			if (indx != -1) {
				outputNameFile = outputNameFile.substring(indx + 1);
			}

			// Process the document
			success = processDoc(doc);
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
					rootNode.getAttributes().getNamedItem(
							"version").getNodeValue());
			attributes.put("releaseVersion",
					rootNode.getAttributes().getNamedItem(
							"release_version").getNodeValue());

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

						str +=  "|____[ Specimen ], name: " + s.getName() + 
								" (" +	s.attributesToString() + ").\n";

						for (TubeDescriptor tb : s.tubes) {

							str +=  "|______[ Tube ], name: " + tb.getName() +
									" (" + tb.attributesToString() + ").\n";
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
	 * Descriptor representing an experiment parsed from the XML.
	 * @author Aaron Ponti
	 *
	 */
	public class ExperimentDescriptor extends AbstractDescriptor {

		// An Experiment can contain TRAYS that in turn contain SPECIMENs 
		// which contain TUBEs, or directly SPECIMENs containing TUBEs.

		/**
		 * Set of Tray's
		 */
		public Set<TrayDescriptor> trays = new LinkedHashSet<TrayDescriptor>();

		/**
		 * Set of Specimen's
		 */
		public Set<SpecimenDescriptor> specimens = 
				new LinkedHashSet<SpecimenDescriptor>();

		/**
		 * Constructor
		 * @param expNode DOM node that refers to an Experiment.
		 */
		public ExperimentDescriptor(org.w3c.dom.Node expNode) {

			// Get the experiment name
			this.name = expNode.getAttributes().getNamedItem(
					"name").getNodeValue();

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
	 * Descriptor representing a tube parsed from the XML.
	 * A Tube is always a child of a Specimen.
	 * @author Aaron Ponti
	 */
	public class TubeDescriptor extends AbstractDescriptor {
		
		public FCSFileDescriptor fcsFile;
		
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
			
			// Associate the FCS file to the Tube
			fcsFile = new FCSFileDescriptor(
					attributes.get("fullDataFilename").toString());
	
		}
	
		/**
		 * Return a String representation of the extracted Tube node.
		 * @return String representation of the Tube node.
		 */
		@Override
		public String toString() {
			return this.name;
		}
	
		/**
		 * Return a simplified class name to use in XML.
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "Tube";
		}
		
	}

	/**
	 * Descriptor representing an FCS file associated to a Tube.
	 * An FCS File is always a child of a Tube.
	 * @author Aaron Ponti
	 */
	public class FCSFileDescriptor extends AbstractDescriptor {

		private String fullFileName = "";
		private String relativeFileName = "";

		/**
		 * Constructor.
		 * @param fcsFileName FCS file name with full path
		 */
		public FCSFileDescriptor(String fcsFileName) {

			// Store the file name
			this.fullFileName = fcsFileName;
			this.name = (new File( fcsFileName )).getName();
			
			// Store the relative file name (to the incoming dir)
			storeRelativePath(fcsFileName);
			
			// Set the attribute relative file name
			attributes.put("relativeFileName", this.relativeFileName);
		}

		/**
		 * Return a String representation of the extracted Tube node.
		 * @return String representation of the Tube node.
		 */
		@Override
		public String toString() {
			String str = this.name;
			return str;
		}

		/**
		 * Return the full FCS file name.
		 * @return full FCS file name.
		 */
		public String getFileName() {
			return this.fullFileName;
		}

		/**
		 * Return a simplified class name to use in XML.
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "FCSFile";
		}

		/**
		 * Return the file name with path relative to the global incoming dir
		 * @return relative file name
		 */
		private void storeRelativePath(String fcsFileName) {
			String incoming = "";
			try {
				incoming = incomingDir.getCanonicalPath();
			} catch (IOException e) {
				System.err.println("Error with incoming folder path " +
						"("+ incomingDir + ")");
			}
			
			// Return the FCS path relative to the incoming dir
			assert (incoming.length() <= fcsFileName.length()); 
			
			this.relativeFileName = 
					fcsFileName.substring(incoming.length());
			if (this.relativeFileName.startsWith(File.separator)) {
				this.relativeFileName = this.relativeFileName.substring(1);
			}
		}
	}

	/**
	 * Descriptor representing a specimen parsed from the XML.
	 * A Specimen can be a child of a Tray or directly of an Experiment.
	 * @author Aaron Ponti
	 */
	public class SpecimenDescriptor extends AbstractDescriptor {
	
		/* Public instance variables */
	
		/**
		 * Set of Tube's
		 */			
		public Set<TubeDescriptor> tubes = new LinkedHashSet<TubeDescriptor>();
	
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
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "Specimen";
		}

	}

	/**
	 * Descriptor representing a tray parsed from the XML.
	 * @author Aaron Ponti
	 */
	public class TrayDescriptor extends AbstractDescriptor {
	
		/* Public instance variables */
	
		/**
		 * Set of Specimen's
		 */
		public Set<SpecimenDescriptor> specimens = 
				new LinkedHashSet<SpecimenDescriptor>();
	
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
