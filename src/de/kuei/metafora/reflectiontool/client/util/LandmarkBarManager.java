package de.kuei.metafora.reflectiontool.client.util;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.kuei.metafora.reflectiontool.client.ReflectionToolHtml;
import de.kuei.metafora.reflectiontool.client.canvasChilds.Landmark;
import de.kuei.metafora.reflectiontool.client.canvasChilds.LandmarkBar;

public class LandmarkBarManager {

	private static Logger logger = Logger
			.getLogger("ReflectionTool.LandmarkBarManager");

	private static LandmarkBarManager instance = null;

	public static LandmarkBarManager getInstance() {
		if (instance == null) {
			instance = new LandmarkBarManager();
		}
		return instance;
	}

	private HashMap<String, LandmarkBar> landmarkbars;

	private LandmarkBarManager() {
		logger.setLevel(Level.INFO);

		landmarkbars = new HashMap<String, LandmarkBar>();
	}

	public void handleLandmark(Landmark landmark) {
		logger.log(Level.INFO, "handleLandmark: " + landmark.getNodeId());

		if (landmark.getNodeId() != null) {

			if (!landmarkbars.containsKey(landmark.getNodeId())) {
				if (landmark.isStarted()) {
					LandmarkBar landmarkbar = new LandmarkBar(landmark);

					String category = landmark.getPlanningToolCategory();

					logger.log(Level.INFO, "New Landmarkbar: Category: "
							+ category + ", NodeId: " + landmark.getNodeId());

					landmarkbar.setBackgroundColor(landmark
							.getPlanningToolCategoryColor());

					landmarkbars.put(landmark.getNodeId(), landmarkbar);
					ReflectionToolHtml.reflectionToolInstance
							.addLandmarkBar(landmarkbar);
				} else {
					logger.log(Level.INFO, "handleLandmark: not started "
							+ landmark.getNodeId());
				}
			} else {
				landmarkbars.get(landmark.getNodeId()).linkToLandmark(landmark);
				logger.log(Level.INFO, "handleLandmark: known Landmarkbar "
						+ landmark.getNodeId());
			}
		}
	}
}
