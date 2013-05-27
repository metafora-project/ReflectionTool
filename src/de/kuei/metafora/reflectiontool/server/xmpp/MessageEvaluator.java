package de.kuei.metafora.reflectiontool.server.xmpp;

import java.util.HashMap;
import java.util.Vector;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.w3c.dom.Document;

import de.kuei.metafora.reflectiontool.server.util.LandmarkDataGenerator;
import de.kuei.metafora.reflectiontool.server.xml.XMLException;
import de.kuei.metafora.reflectiontool.server.xml.XMLMessage;
import de.kuei.metafora.reflectiontool.server.xml.XMLUtils;
import de.kuei.metafora.reflectiontool.shared.LandmarkData;

public class MessageEvaluator implements PacketListener {

	private static final String[] colors = new String[] { "#63AF27", "#5188C7",
			"#868686", "#DF2B27", "#AEDDDE", "#DEB000", "#8B4A97", "#D8DADA" };

	private static MessageEvaluator instance = null;

	public static MessageEvaluator getInstance() {
		if (instance == null) {
			instance = new MessageEvaluator();
		}
		return instance;
	}

	private HashMap<String, HashMap<String, Vector<LandmarkData>>> landmarks;

	private HashMap<String, String> categoryColors;

	private MessageEvaluator() {
		landmarks = new HashMap<String, HashMap<String, Vector<LandmarkData>>>();
		categoryColors = new HashMap<String, String>();
	}

	public Vector<LandmarkData> getLandmarks(String group, String challengeId) {
		if (landmarks.containsKey(group))
			return landmarks.get(group).get(challengeId);
		else
			return null;
	}

	public void handleMessage(XMLMessage message) {
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

			String sendingTool = message.getProperty("sending_tool");

			if (sendingTool != null
					&& sendingTool.toLowerCase().contains("planning")) {
				String id = message.getObjectId();
				String type = message.getObjectType();
				type = type.replaceAll("\n", "");
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

			String color = "#AAAAAA";

			if (category != null) {
				if (categoryColors.containsKey(category)) {
					color = categoryColors.get(category);
				} else {
					int colorIndex = (int) (Math.random() * 8);
					color = colors[colorIndex];
					categoryColors.put(category, color);
					System.err.println("ReflectionTool: MessageEvaluator: "
							+ color + " assigned to category " + category);
				}
			}

			landmark.setPlanningToolData(picture, name, category, color);

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
