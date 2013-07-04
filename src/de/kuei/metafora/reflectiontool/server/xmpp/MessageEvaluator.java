package de.kuei.metafora.reflectiontool.server.xmpp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.w3c.dom.Document;

import de.kuei.metafora.reflectiontool.server.util.HistoryRequest;
import de.kuei.metafora.reflectiontool.server.util.LandmarkDataGenerator;
import de.kuei.metafora.reflectiontool.server.xml.XMLException;
import de.kuei.metafora.reflectiontool.server.xml.XMLMessage;
import de.kuei.metafora.reflectiontool.server.xml.XMLUtils;
import de.kuei.metafora.reflectiontool.shared.LandmarkData;

public class MessageEvaluator implements PacketListener {

	private static final String[] colors = new String[] { "#63AF27", "#5188C7",
			"#868686", "#DF2B27", "#AEDDDE", "#DEB000", "#8B4A97", "#D8DADA" };

	private static final String[] lmColors = new String[] { "#9B760B",
			"#63788B", "#951315", "#9AB17B", "#6DB04D", "#BB7133", "#3AA2BB",
			"#939393" };
	private static final String[] toolnames = new String[] { "planning",
			"lasad", "piki", "sus", "expresser", "juggler", "math" };

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

	public void initLandmarks(String group, String challengeId) {
		if (!landmarks.containsKey(group)) {
			landmarks.put(group, new HashMap<String, Vector<LandmarkData>>());
		}

		HashMap<String, Vector<LandmarkData>> groupLandmarks = landmarks
				.get(group);
		if (!groupLandmarks.containsKey(challengeId)) {
			groupLandmarks.put(challengeId, new Vector<LandmarkData>());
		}

		try {
			HistoryRequest.request(challengeId, group);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Vector<LandmarkData> getLandmarks(String group, String challengeId) {
		if (!landmarks.containsKey(group)
				|| !landmarks.get(group).containsKey(challengeId)) {
			initLandmarks(group, challengeId);
		}

		return landmarks.get(group).get(challengeId);
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

		if (type.equals("login")) {
			System.err.println("Login: " + message.getDescription());
			// handleLogin(message);
		} else if (type.equals("logout")) {
			System.err.println("Logout: " + message.getDescription());
			// handleLogout(message);
		} else if (type.equals("group_switch")) {
			System.err.println("Group Switch: " + message.getDescription());
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

			if (group != null) {
				if (!landmarks.containsKey(group)
						|| !landmarks.get(group).containsKey(challengeId)) {
					initLandmarks(group, challengeId);
				}

				HashMap<String, Vector<LandmarkData>> groupMap = landmarks
						.get(group);

				if (!groupMap.containsKey(challengeId)) {
					groupMap.put(challengeId, new Vector<LandmarkData>());
				}

				String sendingTool = message.getProperty("sending_tool");

				String color = lmColors[lmColors.length - 1];

				if (sendingTool != null && sendingTool.length() > 0) {
					for (int i = 0; i < toolnames.length; i++) {
						if (sendingTool.toLowerCase().contains(toolnames[i])) {
							color = lmColors[i];
							break;
						}
					}
				}

				if (activityType != null
						&& activityType.toLowerCase().equals("help_request")) {
					color = "#FF0000";
				}

				LandmarkData landmark = LandmarkDataGenerator.generateLandmark(
						message, started, finished, color,
						groupMap.get(challengeId).size());

				if (sendingTool != null
						&& sendingTool.toLowerCase().contains("planning")) {

					String id = message.getObjectId();
					String type = message.getObjectType();
					type = type.replaceAll("\n", "");

					if (id != null) {
						if (type.matches(".*<.*>.*")) {
							handlePlanningToolLandmark(message, landmark);
						} else {
							System.err.println("Type " + type
									+ " is no inner xml.");
						}
					}
				}

				System.err.println("New landmark for " + group + ", "
						+ challengeId + " with description "
						+ landmark.getDescription());

				groupMap.get(challengeId).add(landmark);
			} else {
				String msg = message.getXMLMessage();
				msg = msg.replaceAll("\n", " ");
				System.err
						.println("ReflectionTool.MessageEvaluator: Group id is null! "
								+ msg);
			}
		} else {
			String msg = message.getXMLMessage();
			msg = msg.replaceAll("\n", " ");
			System.err
					.println("ReflectionTool.MessageEvaluator: Challenge id is null! "
							+ msg);
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
	}
}
