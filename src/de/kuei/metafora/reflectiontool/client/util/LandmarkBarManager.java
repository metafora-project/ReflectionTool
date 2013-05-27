package de.kuei.metafora.reflectiontool.client.util;

import java.util.HashMap;

import de.kuei.metafora.reflectiontool.client.ReflectionToolHtml;
import de.kuei.metafora.reflectiontool.client.canvasChilds.Landmark;
import de.kuei.metafora.reflectiontool.client.canvasChilds.LandmarkBar;

public class LandmarkBarManager {

	private static LandmarkBarManager instance = null;

	public static LandmarkBarManager getInstance() {
		if (instance == null) {
			instance = new LandmarkBarManager();
		}
		return instance;
	}

	private HashMap<String, LandmarkBar> landmarkbars;

	private LandmarkBarManager() {
		landmarkbars = new HashMap<String, LandmarkBar>();
	}

	public void handleLandmark(Landmark landmark) {
		if (landmark.getNodeId() != null) {
			if (!landmarkbars.containsKey(landmark.getNodeId())) {
				if (landmark.isStarted()) {
					LandmarkBar landmarkbar = new LandmarkBar(landmark);
					landmarkbars.put(landmark.getNodeId(), landmarkbar);
					ReflectionToolHtml.reflectionToolInstance
							.addLandmarkBar(landmarkbar);
				}
			} else {
				landmarkbars.get(landmark.getNodeId()).linkToLandmark(landmark);
			}
		}
	}
}
