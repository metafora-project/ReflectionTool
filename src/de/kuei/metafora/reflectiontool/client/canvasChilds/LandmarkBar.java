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

public class LandmarkBar extends HTML implements MouseOverHandler,
		MouseOutHandler, MouseUpHandler {

	private static Logger logger = Logger
			.getLogger("ReflectionTool.LandmarkBar");

	private static final String borderColor = "#0000FF";
	private static final String highlightBorderColor = "#FF0000";
	private static final String backgroundColor = "#AAAAAA";
	private static final int landmarkBarMinWidth = 15;
	private static final int landmarkBarHeight = 15;

	private Vector<Landmark> landmarks;

	private boolean finished = false;

	private int x;
	private int y;
	private int width;
	private int height;

	private String imageurl = null;
	private String name = null;
	private String category = null;

	public LandmarkBar(Landmark landmark) {
		super();

		logger.setLevel(Level.INFO);

		logger.log(
				Level.INFO,
				"Planning: " + landmark.isPlanningTool() + ", "
						+ landmark.getPlanningToolName() + ", "
						+ landmark.getPlanningToolPicture());

		if (landmark.isPlanningTool()) {
			imageurl = landmark.getPlanningToolPicture();
			name = landmark.getPlanningToolName();
			category = landmark.getPlanningToolCategory();
		}

		landmarks = new Vector<Landmark>();
		landmarks.add(landmark);

		setTitle(landmark.getDescription());

		x = 0;
		y = 0;
		width = landmarkBarMinWidth;
		height = landmarkBarHeight;

		setSize(width + "px", height + "px");
		getElement().getStyle().setBackgroundColor(backgroundColor);
		getElement().getStyle().setBorderColor(borderColor);
		getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
		getElement().getStyle().setBorderWidth(1, Unit.PX);

		addMouseOverHandler(this);
		addMouseOutHandler(this);
		addMouseUpHandler(this);

		if (landmark.isPlanningTool()) {
			setHTML("<img src=\"" + imageurl + "\" width=\"" + (height - 2)
					+ "\" height=\"" + (height - 2)
					+ "\" /> <span style=\"font-size: xx-small;\">" + name
					+ " (" + category + ")</span>");
		}

	}

	public void setBackgroundColor(String color) {
		getElement().getStyle().setBackgroundColor(color);
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

	public void linkToLandmark(Landmark landmark) {
		int pos = landmarks.size();

		for (int i = landmarks.size() - 1; i > 0; i--) {
			if (!landmarks.get(i).getTime().before(landmark.getTime())) {
				pos--;
			} else {
				break;
			}
		}

		landmarks.insertElementAt(landmark, pos);

		if (landmarks.lastElement().isFinished()) {
			update(landmarks.lastElement().getTime(),
					ReflectionToolHtml.reflectionToolInstance.getTimeline()
							.getLenghtOfAMinute());
		}
		finished = landmarks.lastElement().isFinished();
	}

	public void update(Date currentTime, double lenghtOfAMinute) {
		long time = 0;
		if (!finished) {
			time = currentTime.getTime()
					- landmarks.firstElement().getTime().getTime();
		} else {
			time = landmarks.lastElement().getTime().getTime()
					- landmarks.firstElement().getTime().getTime();
		}

		width = (int) ((lenghtOfAMinute * time) / 60000.0);

		if (width < landmarkBarMinWidth) {
			width = landmarkBarMinWidth;
		}
	}

	public Date getStartTime() {
		return landmarks.firstElement().getTime();
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		ReflectionToolHtml.reflectionToolInstance.selectLandmark(landmarks
				.lastElement());
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		getElement().getStyle().setBorderColor(borderColor);
		selectLandmarks(false);
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		getElement().getStyle().setBorderColor(highlightBorderColor);
		selectLandmarks(true);
	}

	private void selectLandmarks(boolean select) {
		Landmark selected = ReflectionToolHtml.reflectionToolInstance
				.getSelectedLandmark();

		for (Landmark lm : landmarks) {
			if (selected != null && selected.equals(lm))
				continue;
			lm.setFocus(select);
		}
	}

	public Landmark getLandmark() {
		return landmarks.firstElement();
	}

	public boolean isFinished() {
		return finished;
	}
}
