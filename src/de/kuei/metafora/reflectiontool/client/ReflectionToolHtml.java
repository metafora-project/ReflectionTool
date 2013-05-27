package de.kuei.metafora.reflectiontool.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

import de.kuei.metafora.reflectiontool.client.canvasChilds.Landmark;
import de.kuei.metafora.reflectiontool.client.canvasChilds.LandmarkBar;
import de.kuei.metafora.reflectiontool.client.canvasElements.Timeline;
import de.kuei.metafora.reflectiontool.client.util.UrlDecoder;

public class ReflectionToolHtml implements EntryPoint, MouseDownHandler,
		MouseUpHandler, MouseMoveHandler, MouseOverHandler, MouseOutHandler {

	public static ReflectionToolHtml reflectionToolInstance = null;

	private static Logger classLogger = Logger
			.getLogger("ReflectionTool.ReflectionToolHtml");
	private static Logger reflectionLogger = Logger.getLogger("ReflectionTool");
	private static Logger rootLogger = Logger.getLogger("");

	private Canvas canvas;
	private Context2d context;

	private ScrollPanel scroll;
	private LayoutPanel viewPanel;

	private boolean mouseDown = false;
	private int startx = -1;
	private int starty = -1;
	private int currentx = -1;
	private int currenty = -1;

	private Timeline timeline;
	private CheckBox autoZoom;
	private CheckBox keepFocus;
	private HTML selectedLandmark;

	private String group = null;
	private String challengeId = null;
	private String user = null;
	private String token = null;

	public void onModuleLoad() {
		reflectionToolInstance = this;

		// configure logger level
		rootLogger.setLevel(Level.SEVERE);
		reflectionLogger.setLevel(Level.SEVERE);
		// debug this class
		classLogger.setLevel(Level.INFO);

		group = UrlDecoder.getParameter("groupid");
		challengeId = UrlDecoder.getParameter("challengeid");
		token = UrlDecoder.getParameter("token");
		user = UrlDecoder.getParameter("user");

		canvas = Canvas.createIfSupported();

		if (canvas == null) {
			classLogger
					.log(Level.SEVERE,
							"Your browser doesn't support HTML5 canvas. This app can't work without that canvas.");
			return;
		}

		LayoutPanel layout = new LayoutPanel();

		LayoutPanel header = new LayoutPanel();
		layout.add(header);
		layout.setWidgetLeftRight(header, 0, Unit.PX, 0, Unit.PX);
		layout.setWidgetTopHeight(header, 0, Unit.PX, 30, Unit.PX);

		viewPanel = new LayoutPanel();
		viewPanel.add(canvas);
		viewPanel.setWidgetLeftWidth(canvas, 0, Unit.PX, 100, Unit.PCT);
		viewPanel.setWidgetTopHeight(canvas, 0, Unit.PX, 100, Unit.PCT);

		viewPanel.setWidth(Timeline.minWidth + "px");
		canvas.setWidth(Timeline.minWidth + "px");
		canvas.setCoordinateSpaceWidth(Timeline.minWidth);

		viewPanel.setHeight(Timeline.minHeight + "px");
		canvas.setHeight(Timeline.minHeight + "px");
		canvas.setCoordinateSpaceHeight(Timeline.minHeight);

		scroll = new ScrollPanel(viewPanel);
		layout.add(scroll);
		layout.setWidgetTopBottom(scroll, 30, Unit.PX, 0, Unit.PX);
		layout.setWidgetLeftRight(scroll, 0, Unit.PX, 0, Unit.PX);

		RootLayoutPanel rootLayout = RootLayoutPanel.get();
		rootLayout.add(layout);
		rootLayout.setWidgetTopBottom(layout, 0, Unit.PX, 0, Unit.PX);
		rootLayout.setWidgetLeftRight(layout, 0, Unit.PX, 0, Unit.PX);

		context = canvas.getContext2d();

		canvas.addMouseDownHandler(this);
		canvas.addMouseUpHandler(this);
		canvas.addMouseMoveHandler(this);
		canvas.addMouseOutHandler(this);
		canvas.addMouseOverHandler(this);

		timeline = new Timeline(canvas, scroll, viewPanel);

		int left = 5;

		Image zoomIn = new Image("zoomin.png");
		zoomIn.setTitle("zoom in");
		header.add(zoomIn);
		header.setWidgetLeftWidth(zoomIn, left, Unit.PX, 25, Unit.PX);
		header.setWidgetTopHeight(zoomIn, 2, Unit.PX, 25, Unit.PX);
		zoomIn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				disableAutozoom();
				zoomIn();
			}
		});

		left += 25 + 5;

		Image zoomFit = new Image("zoomfit.png");
		zoomFit.setTitle("fit to screen");
		header.add(zoomFit);
		header.setWidgetLeftWidth(zoomFit, left, Unit.PX, 25, Unit.PX);
		header.setWidgetTopHeight(zoomFit, 2, Unit.PX, 25, Unit.PX);
		zoomFit.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// TODO: implement
			}
		});

		left += 25 + 5;

		Image zoomOut = new Image("zoomout.png");
		zoomOut.setTitle("zoom out");
		header.add(zoomOut);
		header.setWidgetLeftWidth(zoomOut, left, Unit.PX, 25, Unit.PX);
		header.setWidgetTopHeight(zoomOut, 2, Unit.PX, 25, Unit.PX);
		zoomOut.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				disableAutozoom();
				zoomOut();
			}
		});

		left += 25 + 5;

		autoZoom = new CheckBox("<img src=\"zoomauto.png\" />", true);
		autoZoom.setValue(timeline.getAutozoom());
		autoZoom.setTitle("auto size");
		header.add(autoZoom);
		header.setWidgetLeftWidth(autoZoom, left, Unit.PX, 100, Unit.PX);
		header.setWidgetTopHeight(autoZoom, 5, Unit.PX, 20, Unit.PX);
		autoZoom.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				updateAutozoom();
			}
		});

		left += 50 + 5;

		keepFocus = new CheckBox("<img src=\"keepFocus.png\" />", true);
		keepFocus.setValue(timeline.getKeepFocus());
		keepFocus.setTitle("keep focus");
		header.add(keepFocus);
		header.setWidgetLeftWidth(keepFocus, left, Unit.PX, 100, Unit.PX);
		header.setWidgetTopHeight(keepFocus, 5, Unit.PX, 20, Unit.PX);
		keepFocus.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				updateKeepFocus();
			}
		});

		left += 50 + 5;

		Image before = new Image("before.png");
		before.setTitle("select landmark before");
		header.add(before);
		header.setWidgetLeftWidth(before, left, Unit.PX, 25, Unit.PX);
		header.setWidgetTopHeight(before, 2, Unit.PX, 25, Unit.PX);
		before.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// TODO: implement
			}
		});

		left += 25 + 5;

		Image next = new Image("after.png");
		next.setTitle("select next landmark");
		header.add(next);
		header.setWidgetLeftWidth(next, left, Unit.PX, 25, Unit.PX);
		header.setWidgetTopHeight(next, 2, Unit.PX, 25, Unit.PX);
		next.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// TODO: implement
			}
		});

		left += 25 + 5;

		selectedLandmark = new HTML("Click on landmark to focus!");
		header.add(selectedLandmark);
		header.setWidgetLeftRight(selectedLandmark, left, Unit.PX, 0, Unit.PX);
		header.setWidgetTopHeight(selectedLandmark, 5, Unit.PX, 20, Unit.PX);

		UpdateTimer updateTimer = new UpdateTimer(group, challengeId);
		updateTimer.start();
	}

	public void selectLandmark(Landmark landmark) {
		if (landmark != null) {
			selectedLandmark.setHTML(landmark.getDescription());
		} else {
			selectedLandmark.setHTML("Click on landmark to focus!");
		}
		timeline.selectLandmark(landmark);
	}

	public Landmark getSelectedLandmark() {
		return timeline.getSelectedLandmark();
	}

	public Timeline getTimeline() {
		return timeline;
	}

	public void addLandmark(Landmark landmark) {
		timeline.addLandmark(landmark);
	}

	public void addLandmarkBar(LandmarkBar landmarkbar) {
		timeline.addLandmarkBar(landmarkbar);
	}

	public void updateAutozoom() {
		timeline.setAutozoom(autoZoom.getValue());
	}

	public void disableAutozoom() {
		timeline.setAutozoom(false);
		autoZoom.setValue(false);
	}

	public void updateKeepFocus() {
		timeline.setKeepFocus(keepFocus.getValue());
	}

	public void zoomIn() {
		timeline.zoomIn();
	}

	public void zoomOut() {
		timeline.zoomOut();
	}

	public void drawZoom() {
		if (mouseDown) {
			CssColor fillcolor = CssColor.make("rgba(" + 0 + ", " + 0 + "," + 0
					+ ", " + 0.0 + ")");
			CssColor drawcolor = CssColor.make("rgba(" + 255 + ", " + 0 + ","
					+ 0 + ", " + 1 + ")");

			context.setFillStyle(fillcolor);
			context.setStrokeStyle(drawcolor);

			if (currentx == -1) {
				context.strokeRect(startx, starty, 2, 2);
			} else {
				int width = currentx - startx;
				int height = currenty - starty;

				context.strokeRect(startx, starty, width, height);

				classLogger.log(Level.INFO, "Zoom rect: x: " + startx + ", y: "
						+ starty + ", width: " + width + ", height: " + height);
			}
		}
	}

	public void clearZoom() {
		if (mouseDown) {
			CssColor color = CssColor.make("rgba(" + 255 + ", " + 255 + ","
					+ 255 + ", " + 1 + ")");

			context.setFillStyle(color);
			context.setStrokeStyle(color);

			if (currentx == -1) {
				context.strokeRect(startx, starty, 2, 2);
			} else {
				int width = currentx - startx;
				int height = currenty - starty;

				context.strokeRect(startx, starty, width, height);

				classLogger.log(Level.INFO, "Zoom rect: x: " + startx + ", y: "
						+ starty + ", width: " + width + ", height: " + height);
			}
		}
	}

	public void startZoom(int x, int y) {
		startx = x;
		starty = y;

		drawZoom();

		timeline.setAutozoom(false);
	}

	public void updateZoom(int x, int y) {
		clearZoom();

		if (x < startx) {
			currentx = startx;
			startx = x;
		} else {
			currentx = x;
		}

		if (y < starty) {
			currenty = starty;
			starty = y;
		} else {
			currenty = y;
		}

		drawZoom();
	}

	public void doZoom(int x, int y) {
		int len = currentx - startx;
		double ratio = ((double) scroll.getOffsetWidth()) / len;
		double lenOfMin = timeline.getLenghtOfAMinute();
		double newLenOfMin = lenOfMin * ratio;

		classLogger.log(Level.INFO,
				"doZoom: startx: " + startx + ", len: " + len
						+ ", newLenOfAMinute: " + newLenOfMin + ", ratio: "
						+ ratio + ", lenOfMin: " + lenOfMin + ", scroll: "
						+ scroll.getOffsetWidth());

		timeline.setLengthOfAMinute(newLenOfMin);
		timeline.drawTimeline();

		scroll.setHorizontalScrollPosition((int) (startx * ratio));

		startx = currentx = starty = currenty = -1;
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if (mouseDown) {
			updateZoom(event.getX(), event.getY());
		}
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		if (mouseDown) {
			mouseDown = false;
			doZoom(event.getX(), event.getY());
		}
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		if (mouseDown) {
			mouseDown = false;
			startx = currentx = starty = currenty = -1;
		} else {
			mouseDown = true;
			startZoom(event.getX(), event.getY());
		}

	}

	@Override
	public void onMouseOut(MouseOutEvent event) {

	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		if (mouseDown) {
			updateZoom(event.getX(), event.getY());
		}
	}
}
