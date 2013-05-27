package de.kuei.metafora.reflectiontool.server.xmpp;

import java.util.HashMap;
import java.util.Vector;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.w3c.dom.Document;

import de.kuei.metafora.reflectiontool.server.util.LandmarkDataGenerator;
import de.kuei.metafora.reflectiontool.server.xml.XMLException;
import de.kuei.metafora.reflectiontool.server.xml.XMLUtils;
import de.kuei.metafora.reflectiontool.shared.LandmarkData;
import de.kuei.metafora.xmltools.XMLMessage;

public class MessageEvaluator implements PacketListener {

	private static MessageEvaluator instance = null;

	public static MessageEvaluator getInstance() {
		if (instance == null) {
			instance = new MessageEvaluator();
		}
		return instance;
	}

	private HashMap<String, HashMap<String, Vector<LandmarkData>>> landmarks;

	private MessageEvaluator() {
		landmarks = new HashMap<String, HashMap<String, Vector<LandmarkData>>>();
	}

	public Vector<LandmarkData> getLandmarks(String group, String challengeId) {
		if (landmarks.containsKey(group))
			return landmarks.get(group).get(challengeId);
		else
			return null;
	}

	public void handleMessage(XMLMessage message) {
		System.err.println("Handle message");

		String group = message.getProperty("group_id");

		if (group != null) {
			if (!landmarks.containsKey(group)) {
				landmarks.put(group,
						new HashMap<String, Vector<LandmarkData>>());
			}

			String type = message.getActionType();

			if (type.toLowerCase().equals("landmark")) {
				handleLandmark(message);
			} else if (type.toLowerCase().equals("indicator")) {
				handleIndicator(message);
			} else {
				System.err.println("Unknown document type.");
			}
		} else {
			System.err.println("Group is null. Message ignored.");
		}
	}

	private void handleIndicator(XMLMessage message) {
		String type = message.getObjectType();
		type = type.toLowerCase();

		// String groupId = message.getProperty("group_id");

		if (type.equals("login")) {
			// handleLogin(message);
		} else if (type.equals("logout")) {
			// handleLogout(message);
		} else if (type.equals("group_switch")) {
			// handleGroupSwitch(message);
		} else {
			System.err.println("Unknown object type " + type);
		}
	}

	//
	// private void handleLogin(XMLMessage message) {
	// Login login = new Login(message);
	// ReflectionPanel.getInstance().addReflectionElement(login);
	// }
	//
	// private void handleLogout(XMLMessage message) {
	// Logout logout = new Logout(message);
	// ReflectionPanel.getInstance().addReflectionElement(logout);
	// }
	//
	// private void handleGroupSwitch(XMLMessage message) {
	// GroupSwitch groupSwitch = new GroupSwitch(message);
	// ReflectionPanel.getInstance().addReflectionElement(groupSwitch);
	// }

	private void handleLandmark(XMLMessage message) {
		String activityType = message.getProperty("activity_type");
		boolean started = false;
		boolean finished = false;

		if (activityType != null) {
			if (activityType.toLowerCase().equals("modify_state_started")) {
				started = true;
			} else if (activityType.toLowerCase().equals(
					"modify_state_finished")) {
				finished = true;
			}
		}

		String challengeId = message.getProperty("challenge_id");

		if (challengeId != null) {
			String group = message.getProperty("group_id");
			HashMap<String, Vector<LandmarkData>> groupMap = landmarks
					.get(group);
			if (!groupMap.containsKey(challengeId)) {
				groupMap.put(challengeId, new Vector<LandmarkData>());
			}

			LandmarkData landmark = LandmarkDataGenerator.generateLandmark(
					message, started, finished);

			System.err.println("new landmark: " + challengeId + ", " + group);

			String sendingTool = message.getProperty("sending_tool");

			if (sendingTool != null
					&& sendingTool.toLowerCase().contains("planning")) {
				String id = message.getObjectId();
				String type = message.getObjectType();
				type = type.replaceAll("\n", "");
				System.err.println("id: " + id + ", type: " + type);
				if (id != null) {
					if (type.matches(".*<.*>.*")) {
						handlePlanningToolLandmark(message, landmark);
					} else {
						System.err
								.println("Type " + type + " is no inner xml.");
					}
				}
			}

			groupMap.get(challengeId).add(landmark);

		} else {
			System.err.println("Message challenge id is null!");
		}

	}

	private void handlePlanningToolLandmark(XMLMessage message,
			LandmarkData landmark) {
		try {
			Document doc = XMLUtils.parseXMLString(message.getObjectType(),
					false);
			String picture = doc.getElementsByTagName("pictureurl").item(0)
					.getAttributes().getNamedItem("value").getNodeValue();
			String category = doc.getElementsByTagName("categorie").item(0)
					.getAttributes().getNamedItem("value").getNodeValue();
			String name = doc.getElementsByTagName("name").item(0)
					.getAttributes().getNamedItem("value").getNodeValue();

			System.err.println("PlanningToolLandmark: " + picture + ", "
					+ category + ",  " + name);

			landmark.setPlanningToolData(picture, name, category);

		} catch (XMLException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	public void processPacket(Packet packet) {
		if (packet.getFrom().contains(
				XMPPConnection.getInstance().getAnalysisChannel())) {

			if (packet != null && packet instanceof Message) {
				String message = ((Message) packet).getBody();
				message = message.replaceAll("\n", "");
				if (message
						.matches(".*[<][ ]*description[ ]*[>].*[<][ ]*[/]description[ ]*[>].*")) {
					XMLMessage msg = new XMLMessage(message);
					handleMessage(msg);
				} else {
					System.err.println("Message is no xml message: " + message);
				}
			}
		} else {
			System.err.println("XMPP message from " + packet.getFrom()
					+ " dropped.");
		}
	}

}
