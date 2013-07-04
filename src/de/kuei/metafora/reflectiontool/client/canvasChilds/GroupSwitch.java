package de.kuei.metafora.reflectiontool.client.canvasChilds;

import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupSwitch extends Landmark {

	private static Logger logger = Logger.getLogger("ReflectionTool.Login");

	public GroupSwitch(Date time, Vector<String> users, String indicatorType,
			String activityType, String sendingTool, String challengeName,
			String groupId, String token, String classification,
			String description) {
		super(time, users, null, null, null, indicatorType, activityType,
				sendingTool, challengeName, groupId, token, classification,
				description, null, false, false, false, null, null, null, "#AAAAAA", "#FFFF00", null);

		logger.setLevel(Level.INFO);

		getElement().setClassName("groupSwitch");
	}
}
