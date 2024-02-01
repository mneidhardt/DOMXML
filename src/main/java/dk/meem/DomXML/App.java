package dk.meem.DomXML;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/* Class to search and find stuff in an XML file, using namespaces.
 * In this case I read XSDs from Datafordeler.dk.
 */

public class App {
	private static String ns1 = "nons"; // Default namespace.
	private static String ns1uri = "http://www.w3.org/2001/XMLSchema";
	private static String ns2 = "mat"; // Target namespace for matriklen.
	private static String ns2uri = "http://data.gov.dk/schemas/matrikel/1/replikering";

	public static void main(String[] args)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		DomXMLReader xr = new DomXMLReader(ns1, ns1uri, ns2, ns2uri);
		xr.createDOM("20230516.v2.0.0.Matrikel.xsd");

		NodeList nl = (NodeList) xr.readDOM1("/" + ns1 + ":schema/" + ns1 + ":element[@name][@type]");

		for (int i = 0; i < nl.getLength(); i++) {
			String name = xr.getAttributeValue(nl.item(i), "name");		// Not actually needed for this.
			String type = xr.getAttributeValue(nl.item(i), "type");
			if (name == null) {
				throw new IOException("No attribute 'name' in node <" + nl.item(i).getNodeName() + ">.");
			}
			if (type == null) {
				throw new IOException("No attribute 'type' in node <" + nl.item(i).getNodeName() + ">.");
			}

			/*
			 * Now search for the complexType with the name found in type attribute,
			 * excluding the target namespace:
			 */
			if (type.startsWith(ns2 + ":")) {
				type = type.substring(ns2.length() + 1);
			}
			String xpathstr = "/" + ns1 + ":schema/" + ns1 + ":complexType[@name=\"" + type + "\"]/" + ns1
					+ ":sequence/" + ns1 + ":element[@name]";

			System.out.println("XP2=" + xpathstr);

			NodeList nl2 = (NodeList) xr.readDOM1(xpathstr);
			for (int j = 0; j < nl2.getLength(); j++) {
				// xr.dumpNode(nl2.item(j));
				String name2 = xr.getAttributeValue(nl2.item(j), "name");
				String type2 = xr.getAttributeValue(nl2.item(j), "type");
				System.out.println("    " + name2 + ", " + type2);
			}
		}

	}

}
