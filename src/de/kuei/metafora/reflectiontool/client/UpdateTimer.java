package de.kuei.metafora.reflectiontool.client;

import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.reflectiontool.client.canvasChilds.Landmark;
import de.kuei.metafora.reflectiontool.client.serverlink.ServerRequest;
import de.kuei.metafora.reflectiontool.client.serverlink.ServerRequestAsync;
import de.kuei.metafora.reflectiontool.shared.LandmarkData;

public class UpdateTimer {

	private final ServerRequestAsync serverRequest = GWT
			.create(ServerRequest.class);

	private final Timer timer;
	private final String group;
	private final String challengeId;
	private int messageCount = 0;

	public UpdateTimer(String groupName, String challenge) {
		this.group = groupName;
		this.challengeId = challenge;

		timer = new Timer() {

			@Override
			public void run() {
				serverRequest.getLandmarks(group, challengeId, messageCount,
						new AsyncCallback<Vector<LandmarkData>>() {

							@Override
							public void onSuccess(Vector<LandmarkData> result) {
								for (LandmarkData lmd : result) {
									Landmark landmark = new Landmark(lmd);
									ReflectionToolHtml.reflectionToolInstance
											.addLandmark(landmark);
									messageCount++;
								}
							}

							@Override
							public void onFailure(Throwable caught) {
							}
						});
			}
		};
	}

	public void start() {
		timer.scheduleRepeating(2500);
	}

}
