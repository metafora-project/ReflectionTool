package de.kuei.metafora.reflectiontool.client.canvasChilds;

import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.HTML;

import de.kuei.metafora.reflectiontool.client.ReflectionToolHtml;
import de.kuei.metafora.reflectiontool.shared.LandmarkData;

public class Landmark extends HTML implements MouseOverHandler,
		MouseOutHandler, MouseUpHandler {

	private static Logger logger = Logger.getLogger("ReflectionTool.Landmark");

	private static final String borderColor = "#0000FF";
	private static final String highlightBorderColor = "#FF0000";

	private static final String background = "#FFFFFF";
	private static final String backgroundStarted = "#FFFF00";
	private static final String backgroundFinished = "#00FF00";

	private static final int landmarkWidth = 10;
	private static final int landmarkHeight = 20;

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
	private String planningToolCategoryColor = "#AAAAAA";
	private String planningToolName = null;

	private boolean started = false;
	private boolean finished = false;;

	private int x;
	private int y;
	private int width;
	private int height;

	private boolean hasFocus = false;

	public Landmark(LandmarkData data) {
		this(data.getTime(), data.getUsers(), data.getResource(), data
				.getPlanningToolMap(), data.getTool(), data.getLandmarkType(),
				data.getActivityType(), data.getSendingTool(), data
						.getChallengeName(), data.getGroupId(),
				data.getToken(), data.getClassification(), data
						.getDescription(), data.getNodeId(), data.isStarted(),
				data.isFinished(), data.isPlanningTool(), data
						.getPlanningToolPicture(), data.getPlanningToolName(),
				data.getPlanningToolCategory(), data
						.getPlanningToolCategoryColor(), data
						.getLandmarkColor(), data.getL2L2Url());
	}

	@SuppressWarnings("deprecation")
	public Landmark(Date time, Vector<String> users, String resource,
			String planningToolMap, String tool, String landmarkType,
			String activityType, String sendingTool, String challengeName,
			String groupId, String token, String classification,
			String description, String nodeId, boolean started,
			boolean finished, boolean planningTool, String picture,
			String name, String category, String categoryColor,
			String landmarkColor, String l2l2Url) {
		super();

		logger.setLevel(Level.INFO);

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

		logger.log(Level.INFO, "Landmark: New Landmark " + nodeId);

		this.started = started;
		this.finished = finished;

		if (planningTool) {
			this.planningTool = true;
			this.planningToolName = name;
			this.planningToolCategory = category;
			this.planningToolPicture = picture;
			this.planningToolCategoryColor = categoryColor;
		}

		Date now = new Date();
		if (time.getTime() > now.getTime()) {
			long offset = time.getTime() - now.getTime();
			ReflectionToolHtml.reflectionToolInstance.getTimeline()
					.setCurrentTimeOffest(offset);
		}

		setTitle(description + " (" + time.getHours() + ":" + time.getMinutes()
				+ ")");

		x = 0;
		y = 0;
		width = landmarkWidth;
		height = landmarkHeight;

		setSize(width + "px", height + "px");

		getElement().getStyle().setBackgroundColor(background);
		if (finished) {
			getElement().getStyle().setBackgroundColor(backgroundFinished);
		} else if (started) {
			getElement().getStyle().setBackgroundColor(backgroundStarted);
		} else if (landmarkColor != null) {
			getElement().getStyle().setBackgroundColor(landmarkColor);
		}

		getElement().getStyle().setBorderColor(borderColor);
		getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
		getElement().getStyle().setBorderWidth(1, Unit.PX);

		if (l2l2Url != null) {
			setHTML("<img src=\"" + l2l2Url + "\" width=\"" + ((width * 3) / 4)
					+ "\" style=\"position: absolute; top:0px; left:0px;\" />");
		}

		addMouseOverHandler(this);
		addMouseOutHandler(this);
		addMouseUpHandler(this);

	}

	public boolean isStarted() {
		return started;
	}

	public boolean isFinished() {
		return finished;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
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

	public String getNodeId() {
		return nodeId;
	}

	public Date getTime() {
		return time;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		if (ReflectionToolHtml.reflectionToolInstance != null) {
			ReflectionToolHtml.reflectionToolInstance.selectLandmark(this);
		}
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		if (!hasFocus)
			getElement().getStyle().setBorderColor(borderColor);
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		getElement().getStyle().setBorderColor(highlightBorderColor);
	}

	public void setFocus(boolean focus) {
		hasFocus = focus;
		if (focus)
			getElement().getStyle().setBorderColor(highlightBorderColor);
		else
			getElement().getStyle().setBorderColor(borderColor);
	}

	public boolean isPlanningTool() {
		return planningTool;
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
}
