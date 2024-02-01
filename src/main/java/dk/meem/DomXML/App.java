package dk.meem.DomXML;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import javax.xml.stream.XMLStreamException;

public class App {
	private static String ns1 = "nons"; // Default namespace.
	private static String ns2 = "mat"; // Target namespace for matriklen.

	public static void main(String[] args)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		// https://docs.oracle.com/javase/8/docs/api/java/sql/package-summary.html#package.description
		// auto java.sql.Driver discovery -- no longer need to load a java.sql.Driver
		// class via Class.forName

		DomXMLReader xr = new DomXMLReader(ns1, ns2);
		xr.createDOM("20230516.v2.0.0.Matrikel.xsd");

		NodeList nl = (NodeList) xr.readDOM1("/" + ns1 + ":schema/" + ns1 + ":element[@name][@type]");

		for (int i = 0; i < nl.getLength(); i++) {
			// xr.dumpNode(nl.item(i));
			String name = xr.getAttributeValue(nl.item(i), "name");
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
