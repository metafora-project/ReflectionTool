package de.kuei.metafora.reflectiontool.server.xml;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLMessage {

	private String message = null;

	private Document doc = null;
	private HashMap<String, String> propertyMap;

	private Date actiondate = null;
	private String actionType = null;

	private String objectId;
	private String objectType;

	private String description = "";

	private Vector<String> originators;

	private String classification = null;

	private boolean validMessage = true;

	public XMLMessage(String message) {
		this.message = message;

		originators = new Vector<String>();

		propertyMap = new HashMap<String, String>();

		try {
			doc = XMLUtils.parseXMLString(message, false);

			parseAction();
			parseOriginators();
			parseProperties();
		} catch (XMLException e) {
			validMessage = false;
			e.printStackTrace();
		}
	}

	public boolean isValidMessage() {
		return validMessage;
	}

	public String getXMLMessage() {
		return message;
	}

	public Date getActionDate() {
		return actiondate;
	}

	public String getActionTime() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(actiondate);

		String time = calendar.get(GregorianCalendar.DAY_OF_MONTH) + "."
				+ calendar.get(GregorianCalendar.MONTH) + "."
				+ calendar.get(GregorianCalendar.YEAR) + " "
				+ calendar.get(GregorianCalendar.HOUR_OF_DAY) + ":"
				+ calendar.get(GregorianCalendar.MINUTE) + ":"
				+ calendar.get(GregorianCalendar.SECOND);

		return time;
	}

	public String getActionType() {
		return actionType;
	}

	public Vector<String> getOriginators() {
		return originators;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getObjectType() {
		return objectType;
	}

	public String getDescription() {
		return description;
	}

	public String getClassification() {
		return classification;
	}

	private void parseOriginators() {
		NodeList nl = doc.getElementsByTagName("user");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getAttributes().getNamedItem("role") != null) {
				if (!node.getAttributes().getNamedItem("role").getNodeValue()
						.toLowerCase().equals("originator")) {
					continue;
				}
			}

			if (node.getAttributes().getNamedItem("id") != null) {
				originators.add(node.getAttributes().getNamedItem("id")
						.getNodeValue());
			}
		}
	}

	private void parseAction() {
		NodeList actionList = doc.getElementsByTagName("action");
		Node action = actionList.item(0);
		String actiontime = action.getAttributes().getNamedItem("time")
				.getNodeValue();

		actiondate = null;
		try {
			long millis = Long.parseLong(actiontime);
			actiondate = new Date(millis);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}

		actionList = doc.getElementsByTagName("actiontype");
		Node actiontype = actionList.item(0);
		if (actiontype != null) {
			actionType = actiontype.getAttributes().getNamedItem("type")
					.getNodeValue();
			classification = actiontype.getAttributes()
					.getNamedItem("classification").getNodeValue();
		}

		actionList = doc.getElementsByTagName("object");
		Node object = actionList.item(0);
		if (object != null) {
			try {
				objectId = object.getAttributes().getNamedItem("id")
						.getNodeValue();
				objectType = object.getAttributes().getNamedItem("type")
						.getNodeValue();
			} catch (NullPointerException ex) {
				// ignore
			}
		}

		NodeList nl = doc.getElementsByTagName("description");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			NodeList descChilds = e.getChildNodes();
			for (int j = 0; j < descChilds.getLength(); j++) {
				Node node = descChilds.item(j);
				description += node.getNodeValue() + " ";
			}
		}
	}

	private void parseProperties() {
		NodeList properties = doc.getElementsByTagName("property");

		for (int k = 0; k < properties.getLength(); k++) {
			Node propertyNode = properties.item(k);
			try {
				String name = propertyNode.getAttributes().getNamedItem("name")
						.getNodeValue();
				String value = propertyNode.getAttributes()
						.getNamedItem("value").getNodeValue();

				if (!propertyMap.containsKey(name.toLowerCase()))
					propertyMap.put(name.toLowerCase(), value);
				else
					System.err.println("Property " + name
							+ " already exists! Values: "
							+ propertyMap.get(name.toLowerCase()) + ", "
							+ value);
			} catch (NullPointerException ex) {
				// ignore
			}
		}
	}

	public String getProperty(String name) {
		return propertyMap.get(name.toLowerCase());
	}

}
