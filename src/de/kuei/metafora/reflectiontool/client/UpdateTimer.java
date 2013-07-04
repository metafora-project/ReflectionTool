package de.kuei.metafora.reflectiontool.client;

import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.reflectiontool.client.canvasChilds.Landmark;
import de.kuei.metafora.reflectiontool.client.serverlink.ServerRequest;
import de.kuei.metafora.reflectiontool.client.serverlink.ServerRequestAsync;
import de.kuei.metafora.reflectiontool.shared.LandmarkData;

public class UpdateTimer {

	private static final int SLEEP_TIME = 2500;

	private final ServerRequestAsync serverRequest = GWT
			.create(ServerRequest.class);

	private final Timer timer;
	private final String group;
	private final String challengeId;
	private int index = 0;

	public UpdateTimer(String groupName, String challenge) {
		this.group = groupName;
		this.challengeId = challenge;

		timer = new Timer() {

			@Override
			public void run() {
				serverRequest.getLandmarks(group, challengeId, index,
						new AsyncCallback<Vector<LandmarkData>>() {

							@Override
							public void onSuccess(Vector<LandmarkData> result) {
								for (LandmarkData lmd : result) {
									Landmark landmark = new Landmark(lmd);
									ReflectionToolHtml.reflectionToolInstance
											.addLandmark(landmark);

									if (index < lmd.getIndex()) {
										index = lmd.getIndex();
									}
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
		String browser = UpdateTimer.getBrowser();
		if (browser.contains("firefox")) {
			Window.alert("The Reflection Tool isn't supported in Firefox at the moment. Please use Chrome.");
		} else {
			timer.scheduleRepeating(SLEEP_TIME);
		}
	}

	public static native String getBrowser() /*-{
		return navigator.userAgent.toLowerCase();
	}-*/;
}
