package de.kuei.metafora.reflectiontool.server.util;

import java.util.Date;
import java.util.Vector;

import de.kuei.metafora.reflectiontool.server.xml.XMLMessage;
import de.kuei.metafora.reflectiontool.shared.LandmarkData;

public class LandmarkDataGenerator {

	public static LandmarkData generateLandmark(XMLMessage message,
			boolean started, boolean finished) {

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

		return new LandmarkData(time, users, resource, planningToolMap, tool,
				landmarkType, activityType, sendingTool, challengeName,
				groupId, token, classification, description, nodeId, started,
				finished);

	}

}
