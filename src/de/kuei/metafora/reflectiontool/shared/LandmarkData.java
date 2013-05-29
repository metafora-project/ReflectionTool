package de.kuei.metafora.reflectiontool.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

public class LandmarkData implements Serializable {

	private Date time;
	private Vector<String> users;
	private String resource;
	private String planningToolMap;
	private String tool;
	private String landmarkType;
	private String activityType;
	private String sendingTool;
	private String challengeName;
	private String groupId;
	private String token;
	private String classification;
	private String description;
	private String nodeId;

	private boolean planningTool = false;
	private String planningToolPicture = null;
	private String planningToolCategory = null;
	private String planningToolName = null;

	private String planningToolCategoryColor = "#AAAAAA";
	
	private String landmarkColor = "#AAAAAA";

	private boolean started = false;
	private boolean finished = false;

	public LandmarkData() {

	}

	public LandmarkData(Date time, Vector<String> users, String resource,
			String planningToolMap, String tool, String landmarkType,
			String activityType, String sendingTool, String challengeName,
			String groupId, String token, String classification,
			String description, String nodeId, String landmarkColor, boolean started, boolean finished) {
		super();

		this.time = time;
		this.users = users;
		this.resource = resource;
		this.planningToolMap = planningToolMap;
		this.tool = tool;
		this.landmarkType = landmarkType;
		this.activityType = activityType;
		this.sendingTool = sendingTool;
		this.challengeName = challengeName;
		this.groupId = groupId;
		this.token = token;
		this.classification = classification;
		this.description = description;
		this.nodeId = nodeId;
		this.landmarkColor = landmarkColor;

		this.started = started;
		this.finished = finished;
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isFinished() {
		return finished;
	}

	public Date getTime() {
		return time;
	}

	public Vector<String> getUsers() {
		return users;
	}

	public String getResource() {
		return resource;
	}

	public String getPlanningToolMap() {
		return planningToolMap;
	}

	public String getTool() {
		return tool;
	}

	public String getLandmarkType() {
		return landmarkType;
	}

	public String getActivityType() {
		return activityType;
	}

	public String getSendingTool() {
		return sendingTool;
	}

	public String getChallengeName() {
		return challengeName;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getToken() {
		return token;
	}

	public String getClassification() {
		return classification;
	}

	public String getDescription() {
		return description;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setPlanningToolData(String picture, String name,
			String category, String color) {
		this.planningTool = true;
		this.planningToolPicture = picture;
		this.planningToolName = name;
		this.planningToolCategory = category;
		this.planningToolCategoryColor = color;
	}

	public String getPlanningToolPicture() {
		return planningToolPicture;
	}

	public String getPlanningToolCategory() {
		return planningToolCategory;
	}

	public String getPlanningToolCategoryColor() {
		return planningToolCategoryColor;
	}

	public String getPlanningToolName() {
		return planningToolName;
	}

	public boolean isPlanningTool() {
		return planningTool;
	}
	
	public String getLandmarkColor(){
		return landmarkColor;
	}
}
