package dk.meem.DomXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import org.w3c.dom.xpath.XPathExpression;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

public class DomXMLReader {
	private Document doc;
	private String ns1;
	private String ns2;
	
	public DomXMLReader(String defaultNS, String targetNS) {
		this.ns1 = defaultNS;
		this.ns2 = targetNS;
	}
	
	/* Get value of attribute names attname,
	 * if node n has this attribute.
	 */
	public String getAttributeValue(Node n, String attname) {
		if (n.hasAttributes()) {
			Node anode = n.getAttributes().getNamedItem(attname);
			if (anode != null) {
				return anode.getNodeValue();
			}
		}

		return null;
	}

	public void dumpNode(Node n) {
		//System.out.println(n.getNamespaceURI());
		System.out.println("NAME=" + n.getLocalName() + " VALUE=" + n.getNodeValue());
		if (n.hasAttributes()) {
			NamedNodeMap nm = n.getAttributes();
			for (int j = 0; j < nm.getLength(); j++) {
				System.out.println("    " + nm.item(j).getLocalName() + " > " + nm.item(j).getNodeValue());
			}
		}
	}
	
	public NodeList readDOM1(String xpathstr) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		// there's no default implementation for NamespaceContext...seems kind of silly, no?
		xpath.setNamespaceContext(new NamespaceContext() {
		    public String getNamespaceURI(String prefix) {
		        if (prefix == null) throw new NullPointerException("Null prefix");
		        else if (ns1.equals(prefix)) return "http://www.w3.org/2001/XMLSchema";
		        else if (ns2.equals(prefix)) return "http://data.gov.dk/schemas/matrikel/1/replikering";
		        return XMLConstants.NULL_NS_URI;
		    }

		    // This method isn't necessary for XPath processing.
		    public String getPrefix(String uri) {
		        throw new UnsupportedOperationException();
		    }

		    // This method isn't necessary for XPath processing either.
		    public Iterator getPrefixes(String uri) {
		        throw new UnsupportedOperationException();
		    }
		});

		// note that all the elements in the expression are prefixed with our namespace mapping!
		XPathExpression expr = xpath.compile(xpathstr); //"/nons:schema/nons:element[@name][@type]");

		// assuming you've got your XML document in a variable named doc...
		NodeList nlist = (NodeList) expr.evaluate(this.doc, XPathConstants.NODESET);

		return nlist;
	}

	/* This is the same as readDOM1, but using an external class.
	 * For that reason I prefer the above version.
	 */
	public void readDOM2() throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		HashMap<String, String> prefMap = new HashMap<String, String>() {{
		    put("nons", "http://www.w3.org/2001/XMLSchema");
		    put("r", "http://data.gov.dk/schemas/matrikel/1/replikering");
		}};
		SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
		xpath.setNamespaceContext(namespaces);
		XPathExpression expr = xpath.compile("/nons:schema/nons:complexType");
		NodeList nlist = (NodeList) expr.evaluate(this.doc, XPathConstants.NODESET);
		for (int i=0; i<nlist.getLength(); i++) {
			dumpNode(nlist.item(i));
			System.out.println();
		}
	}

	public void createDOM(String filename) throws ParserConfigurationException, SAXException, IOException {

		// Instantiate the Factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			dbf.setNamespaceAware(true);

			// optional, but recommended
			// process XML securely, avoid attacks like XML External Entities (XXE)
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			// parse XML file
			DocumentBuilder db = dbf.newDocumentBuilder();

			this.doc = db.parse(new File(filename));
	}
	
	/* Just for testing.
	 * 
	 */
	public void gothruAllNodes(Document doc) {
			// optional, but recommended
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
			System.out.println("------");

			NodeList nl1 = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "element");
			System.out.println("Length of nodelist1: " + nl1.getLength());

			for (int i = 0; i < nl1.getLength(); i++) {
				Node node = nl1.item(i);
				NamedNodeMap nm = node.getAttributes();
				System.out.println(node.getNodeName());
				for (int j = 0; j < nm.getLength(); j++) {
					System.out.println("    " + nm.item(j).getLocalName() + " > " + nm.item(j).getNodeValue());
				}
			}

			// get <staff>
			NodeList list = doc.getElementsByTagName("staff");

			for (int temp = 0; temp < list.getLength(); temp++) {

				Node node = list.item(temp);

				if (node.getNodeType() == Node.ELEMENT_NODE) {

					Element element = (Element) node;

					// get staff's attribute
					String id = element.getAttribute("id");

					// get text
					String firstname = element.getElementsByTagName("firstname").item(0).getTextContent();
					String lastname = element.getElementsByTagName("lastname").item(0).getTextContent();
					String nickname = element.getElementsByTagName("nickname").item(0).getTextContent();

					NodeList salaryNodeList = element.getElementsByTagName("salary");
					String salary = salaryNodeList.item(0).getTextContent();

					// get salary's attribute
					String currency = salaryNodeList.item(0).getAttributes().getNamedItem("currency").getTextContent();

					System.out.println("Current Element :" + node.getNodeName());
					System.out.println("Staff Id : " + id);
					System.out.println("First Name : " + firstname);
					System.out.println("Last Name : " + lastname);
					System.out.println("Nick Name : " + nickname);
					System.out.printf("Salary [Currency] : %,.2f [%s]%n%n", Float.parseFloat(salary), currency);

				}
			}

	}

}
