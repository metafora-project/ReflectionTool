package de.kuei.metafora.reflectiontool.server;

import java.util.Vector;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.reflectiontool.client.serverlink.ServerRequest;
import de.kuei.metafora.reflectiontool.server.xmpp.MessageEvaluator;
import de.kuei.metafora.reflectiontool.shared.LandmarkData;

public class ServerRequestImpl extends RemoteServiceServlet implements
		ServerRequest {

	@Override
	public Vector<LandmarkData> getLandmarks(String group, String challengeId,
			int first) {
		Vector<LandmarkData> landmarks = MessageEvaluator.getInstance()
				.getLandmarks(group, challengeId);
		if (landmarks != null) {
			if (first <= 0) {
				return landmarks;
			} else {
				Vector<LandmarkData> lms = new Vector<LandmarkData>();
				lms.addAll(landmarks.subList(first, landmarks.size()));
				return lms;
			}
		} else {
			return new Vector<LandmarkData>();
		}
	}

}
