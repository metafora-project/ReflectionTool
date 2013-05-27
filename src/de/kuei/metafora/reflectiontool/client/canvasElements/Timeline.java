package de.kuei.metafora.reflectiontool.client.canvasElements;

import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

import de.kuei.metafora.reflectiontool.client.ReflectionToolHtml;
import de.kuei.metafora.reflectiontool.client.canvasChilds.Landmark;
import de.kuei.metafora.reflectiontool.client.canvasChilds.LandmarkBar;
import de.kuei.metafora.reflectiontool.client.util.LandmarkBarManager;

public class Timeline {

	private static Logger logger = Logger.getLogger("ReflectionTool.Timeline");

	public static final int minWidth = 600;
	public static final int minHeight = 100;

	private static String[] units = new String[] { "s", "s", "s", "m", "m",
			"m", "h", "h", "h", "h", "d" };
	private static long[] unitMs = new long[] { 1000l, 10000l, 30000l, 60000l,
			600000l, 1800000l, 3600000l, 7200000l, 18000000l, 180000000l,
			86400000l };
	private static long[] unitFactor = new long[] { 1l, 10l, 30l, 1l, 10l, 30l,
			1l, 2l, 5l, 10l, 1l };

	private Canvas canvas;
	private ScrollPanel scroll;
	private LayoutPanel viewPanel;

	private final Timer timer;

	private Date startTime;
	private Date currentTime;

	private double lenghtOfAMinute;

	private CssColor color;

	private int requiredWidth;
	private int requiredHeight;

	private boolean autoZoom = true;
	private boolean keepFocus = true;

	private Vector<Landmark> landmarks;

	private Vector<LandmarkBar> landmarkbars;

	private int unit = 0;

	boolean landmarktest = false;

	private Landmark selectedLandmark = null;

	public Timeline(Canvas canvas, ScrollPanel scroll, LayoutPanel viewPanel) {
		logger.setLevel(Level.INFO);

		this.canvas = canvas;
		this.scroll = scroll;
		this.viewPanel = viewPanel;

		landmarks = new Vector<Landmark>();
		landmarkbars = new Vector<LandmarkBar>();

		startTime = new Date();
		currentTime = new Date();

		lenghtOfAMinute = 1000;

		color = CssColor.make("rgba(" + 0 + ", " + 0 + "," + 255 + ", " + 1
				+ ")");

		canvas.setWidth(minWidth + "px");
		canvas.setCoordinateSpaceWidth(minWidth);

		canvas.setHeight(minHeight + "px");
		canvas.setCoordinateSpaceHeight(minHeight);

		timer = new Timer() {
			@Override
			public void run() {
				updateTime();
				drawTimeline();

				if (landmarktest) {
					landmarktest = false;

					for (int i = 0; i < 5; i++) {
						Landmark lm = new Landmark(
								new Date(currentTime.getTime() - i),
								null,
								"http://metafora.ku.de/images/attitudes/critical.svg",
								null, null, null, null, null, null, null, null,
								null, "landmark at "
										+ (currentTime.getTime() - i),
								"myLandmark" + i, false, false, false, null,
								null, null);

						addLandmark(lm);
					}

					logger.log(Level.INFO, "Test landmark generated!");

				}
			}
		};
		timer.scheduleRepeating(5000);

		updateTime();
		drawTimeline();
	}

	private void updateTime() {
		currentTime.setTime(System.currentTimeMillis());
		updateLandmarkBars();
	}

	public void addLandmark(Landmark landmark) {
		if (viewPanel == null) {
			logger.log(Level.WARNING,
					"Timeline.addLandmark: viewPanel is null. Landmark couldn't be added!");
			return;
		}

		if (landmark == null) {
			logger.log(Level.WARNING,
					"Timeline.addLandmark: Tried to add null landmark!");
			return;
		}

		if (landmarks.contains(landmark)) {
			logger.log(Level.WARNING,
					"Timeline.addLandmark: Landmark was already added!");
			return;
		}

		if (landmark.getTime().getTime() < startTime.getTime()) {
			startTime.setTime(landmark.getTime().getTime());
		}
		insertLandmark(landmark);

		viewPanel.add(landmark);

		layoutLandmarks();

		if (landmark.getNodeId() != null) {
			LandmarkBarManager.getInstance().handleLandmark(landmark);
		}
	}

	private void insertLandmark(Landmark landmark) {
		int pos = landmarks.size();

		for (int i = landmarks.size() - 1; i > 0; i--) {
			if (!landmarks.get(i).getTime().before(landmark.getTime())) {
				pos--;
			} else {
				break;
			}
		}

		landmarks.insertElementAt(landmark, pos);
	}

	private void updateCanvasSize() {
		int height = (requiredHeight > minHeight) ? requiredHeight : minHeight;
		int width = (requiredWidth > minWidth) ? requiredWidth : minWidth;

		if (canvas.getCoordinateSpaceWidth() != width) {
			viewPanel.setWidth(width + "px");
			canvas.setWidth(width + "px");
			canvas.setCoordinateSpaceWidth(width);
		}

		if (canvas.getCoordinateSpaceHeight() != height) {
			viewPanel.setHeight(height + "px");
			canvas.setHeight(height + "px");
			canvas.setCoordinateSpaceHeight(height);
		}

		if (autoZoom && scroll.getElement().getOffsetWidth() > 0) {
			if (scroll.getOffsetWidth() < scroll.getElement().getScrollWidth()) {
				int ratio = scroll.getElement().getScrollWidth()
						/ scroll.getElement().getOffsetWidth();
				ratio += 1;
				double oldLen = lenghtOfAMinute;
				lenghtOfAMinute = lenghtOfAMinute / ratio;
				logger.log(Level.INFO, "Timeline.updateCanvasSize(): old len: "
						+ oldLen + ", new len: " + lenghtOfAMinute
						+ ", ratio: " + ratio);
			}
		}

		layoutLandmarks();

		updateLandmarkBars();

		layoutLandmarkBars();
	}

	public void drawTimeline() {
		long time = currentTime.getTime() - startTime.getTime();
		double length = (((double) time) / 60000.0) * lenghtOfAMinute;

		requiredWidth = (int) length + 50;
		requiredHeight = 50;
		updateCanvasSize();

		drawElement();
	}

	private void layoutLandmarks() {

		for (int i = 0; i < landmarks.size(); i++) {
			Landmark landmark = landmarks.get(i);

			long time = landmark.getTime().getTime() - startTime.getTime();
			double minutes = (double) time / 60000.0;
			int x = (int) (lenghtOfAMinute * minutes);
			x = x - (landmark.getOffsetWidth() / 2);

			int y = 2;

			if (i > 0) {

				Landmark lmbefore = landmarks.get(i - 1);

				int end = lmbefore.getX() + lmbefore.getWidth();

				if (end >= x) {
					y = lmbefore.getY() + 10;

					requiredHeight = y + landmark.getHeight() + 10;
				}
			}

			landmark.setX(x);
			landmark.setY(y);
			landmark.getElement().getStyle().setZIndex(20 + i);

			viewPanel.setWidgetLeftWidth(landmark, x, Unit.PX,
					landmark.getOffsetWidth(), Unit.PX);
			viewPanel.setWidgetTopHeight(landmark, y, Unit.PX,
					landmark.getOffsetHeight(), Unit.PX);
		}

		if (keepFocus && selectedLandmark != null) {
			int x = selectedLandmark.getX();
			x = x - (scroll.getOffsetWidth() / 2);
			scroll.setHorizontalScrollPosition(x);
		}
	}

	private void drawElement() {

		long time = currentTime.getTime() - startTime.getTime();
		double length = (((double) time) / 60000.0) * lenghtOfAMinute;

		Context2d context = canvas.getContext2d();

		context.clearRect(0, 0, scroll.getElement().getScrollWidth(), scroll
				.getElement().getScrollHeight());

		context.setFillStyle(color);
		context.setStrokeStyle(color);

		context.fillRect(0, 5, length, 5);
		context.fill();
		context.stroke();

		context.beginPath();
		context.moveTo(length, 0);
		context.lineTo(length + 7, 7);
		context.lineTo(length, 15);
		context.lineTo(length, 0);
		context.closePath();
		context.stroke();
		context.fill();

		int ticDist = (int) ((lenghtOfAMinute * (double) unitMs[unit]) / 60000.0);
		int count = (int) (time / unitMs[unit]);
		drawTics(count, ticDist, unitFactor[unit], units[unit]);

		ReflectionToolHtml.reflectionToolInstance.drawZoom();

		if (ticDist < 35 && unit < (units.length - 1)) {
			unit++;
			logger.log(Level.INFO,
					"ReflectionTool: Timeline.drawElement(): Unit switched to "
							+ unit);
		} else if (ticDist > 200 && unit > 0) {
			unit--;
			logger.log(Level.INFO,
					"ReflectionTool: Timeline.drawElement(): Unit switched to "
							+ unit);
		}
	}

	public void drawTics(int count, int len, long factor, String unit) {
		Context2d context = canvas.getContext2d();

		context.setFillStyle(color);
		context.setStrokeStyle(color);

		for (int i = 1; i <= count; i++) {
			context.beginPath();
			context.moveTo(len * i, 0);
			context.lineTo(len * i, 15);
			context.closePath();
			context.stroke();

			context.fillText((i * factor) + unit, len * i, 25, 20);
		}
	}

	public void setAutozoom(boolean autoZoom) {
		this.autoZoom = autoZoom;
		logger.log(Level.INFO,
				"ReflectionTool: Timeline.setAutozoom(): Value set to "
						+ autoZoom);
	}

	public boolean getAutozoom() {
		return autoZoom;
	}

	public double getLenghtOfAMinute() {
		return lenghtOfAMinute;
	}

	public void setLengthOfAMinute(double lenghtOfAMinute) {
		if (Double.isInfinite(lenghtOfAMinute)) {
			lenghtOfAMinute = Double.MAX_VALUE;
		} else if (lenghtOfAMinute == 0) {
			lenghtOfAMinute = Double.MIN_VALUE;
		}

		this.lenghtOfAMinute = lenghtOfAMinute;
		logger.log(Level.INFO, "Timeline.setLengthOfAMinute(): New len: "
				+ lenghtOfAMinute);
	}

	public void zoomIn() {
		lenghtOfAMinute = lenghtOfAMinute * 1.2;
		drawTimeline();
	}

	public void zoomOut() {
		lenghtOfAMinute = lenghtOfAMinute * 0.8;
		drawTimeline();
	}

	public void selectLandmark(Landmark landmark) {
		if (selectedLandmark != null) {
			selectedLandmark.setFocus(false);
		}

		selectedLandmark = landmark;

		if (selectedLandmark != null) {
			selectedLandmark.setFocus(true);
		}
	}

	public Landmark getSelectedLandmark() {
		return selectedLandmark;
	}

	public void setKeepFocus(boolean keepFocus) {
		this.keepFocus = keepFocus;
		logger.log(Level.INFO,
				"ReflectionTool: Timeline.setKeepFocus(): Value set to "
						+ keepFocus);
	}

	public boolean getKeepFocus() {
		return keepFocus;
	}

	public void addLandmarkBar(LandmarkBar landmarkbar) {
		if (viewPanel == null) {
			logger.log(Level.WARNING,
					"Timeline.addLandmarkBar: viewPanel is null. Landmarkbar couldn't be added!");
			return;
		}

		if (landmarkbar == null) {
			logger.log(Level.WARNING,
					"Timeline.addLandmarkBar: Tried to add null landmarkbar!");
			return;
		}

		if (landmarkbars.contains(landmarkbar)) {
			logger.log(Level.WARNING,
					"Timeline.addLandmarkBar: Landmarkbar was already added!");
			return;
		}

		if (landmarkbar.getStartTime().getTime() < startTime.getTime()) {
			startTime.setTime(landmarkbar.getStartTime().getTime());
		}
		insertLandmarkBar(landmarkbar);

		viewPanel.add(landmarkbar);

		layoutLandmarkBars();
	}

	private void insertLandmarkBar(LandmarkBar landmarkbar) {
		int pos = landmarkbars.size();

		for (int i = landmarkbars.size() - 1; i > 0; i--) {
			if (!landmarkbars.get(i).getStartTime()
					.before(landmarkbar.getStartTime())) {
				pos--;
			} else {
				break;
			}
		}

		landmarkbars.insertElementAt(landmarkbar, pos);
	}

	private void layoutLandmarkBars() {

		for (int i = 0; i < landmarkbars.size(); i++) {
			LandmarkBar landmarkbar = landmarkbars.get(i);

			long time = landmarkbar.getStartTime().getTime()
					- startTime.getTime();
			double minutes = (double) time / 60000.0;
			int x = (int) (lenghtOfAMinute * minutes);

			int y = requiredHeight;

			if (i > 0) {

				LandmarkBar lmbbefore = landmarkbars.get(i - 1);

				int end = lmbbefore.getX() + lmbbefore.getWidth();

				if (end >= x) {
					y = lmbbefore.getY() + 20;

					requiredHeight = y + landmarkbar.getHeight() + 20;
				}
			}

			landmarkbar.setX(x);
			landmarkbar.setY(y);
			landmarkbar.getElement().getStyle().setZIndex(20 + i);

			landmarkbar.setSize((landmarkbar.getWidth() - 2) + "px",
					(landmarkbar.getHeight() - 2) + "px");

			viewPanel.setWidgetLeftWidth(landmarkbar, x, Unit.PX,
					landmarkbar.getWidth(), Unit.PX);
			viewPanel.setWidgetTopHeight(landmarkbar, y, Unit.PX,
					landmarkbar.getHeight(), Unit.PX);
		}
	}

	private void updateLandmarkBars() {
		for (LandmarkBar lmb : landmarkbars) {
			lmb.update(currentTime, lenghtOfAMinute);
		}
	}

}
