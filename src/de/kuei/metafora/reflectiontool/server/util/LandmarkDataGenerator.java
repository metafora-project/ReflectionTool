package de.kuei.metafora.reflectiontool.server.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import de.kuei.metafora.reflectiontool.server.StartupServlet;
import de.kuei.metafora.reflectiontool.server.xml.XMLMessage;
import de.kuei.metafora.reflectiontool.shared.LandmarkData;

public class LandmarkDataGenerator {

	public static LandmarkData generateLandmark(XMLMessage message,
			boolean started, boolean finished, String landmarkColor, int index) {

		Date time = message.getActionDate();
		Vector<String> users = message.getOriginators();
		String resource = message.getProperty("resource_url");
		String planningToolMap = message.getProperty("planning_tool_map");
		if (planningToolMap == null || planningToolMap.length() == 0) {
			planningToolMap = message.getProperty("map_name");
		}
		String tool = message.getProperty("tool");
		String landmarkType = message.getProperty("landmark_type");
		String activityType = message.getProperty("activity_type");
		String sendingTool = message.getProperty("sending_tool");
		String challengeName = message.getProperty("challenge_name");
		String groupId = message.getProperty("group_id");
		String token = message.getProperty("token");
		String classification = message.getClassification();
		String description = message.getDescription();
		String nodeId = message.getObjectId();
		String l2l2 = message.getProperty("L2L2_TAG");

		String l2l2Url = StartupServlet.apacheserver
				+ "/images/l2l2/generic.jpg";

		try {
			if (l2l2 != null) {
				String urlText = StartupServlet.apacheserver + "/images/l2l2/"
						+ l2l2.toLowerCase() + ".jpg";
				URL url = new URL(urlText);
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();
				con.setRequestMethod("HEAD");
				if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
					l2l2Url = urlText;
				} else {
					System.err.println("Image " + urlText
							+ " not found. Status code: "
							+ con.getResponseCode());
				}
			} else {
				l2l2 = "GENERIC";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new LandmarkData(time, users, resource, planningToolMap, tool,
				landmarkType, activityType, sendingTool, challengeName,
				groupId, token, classification, description, nodeId,
				landmarkColor, started, finished, l2l2, l2l2Url, index);

	}

}
