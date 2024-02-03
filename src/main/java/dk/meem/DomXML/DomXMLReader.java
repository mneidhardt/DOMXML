package dk.meem.DomXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* Class for reading DOM and searching with namespaces.
 * Based on this:
 * https://stackoverflow.com/questions/6390339/how-to-query-xml-using-namespaces-in-java-with-xpath/6392700#6392700 
 */
public class DomXMLReader {
	private Document doc;
	private Map<String, String> nsmap;
	
	public DomXMLReader(String ns1, String ns1uri, String ns2, String ns2uri) {
		this.nsmap = new HashMap<String, String>();
		this.nsmap.put(ns1, ns1uri);
		this.nsmap.put(ns2, ns2uri);
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
	
	public NodeList xpathSearch(String xpathstr) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		SimpleNamespaceContext namespaces = new SimpleNamespaceContext(nsmap);
		xpath.setNamespaceContext(namespaces);
		XPathExpression expr = xpath.compile(xpathstr);
		NodeList nlist = (NodeList) expr.evaluate(this.doc, XPathConstants.NODESET);

		return nlist;
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
