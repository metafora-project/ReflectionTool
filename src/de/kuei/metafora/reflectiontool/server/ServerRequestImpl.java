package de.kuei.metafora.reflectiontool.server;

import java.util.Vector;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.reflectiontool.client.serverlink.ServerRequest;
import de.kuei.metafora.reflectiontool.server.xmpp.MessageEvaluator;
import de.kuei.metafora.reflectiontool.shared.LandmarkData;

public class ServerRequestImpl extends RemoteServiceServlet implements
		ServerRequest {

	private static final int maxLandmarkCount = 200;

	@Override
	public Vector<LandmarkData> getLandmarks(String group, String challengeId,
			int first) {

		Vector<LandmarkData> landmarks = MessageEvaluator.getInstance()
				.getLandmarks(group, challengeId);

		if (first < 0) {
			System.err
					.println("ReflectionTool: ServerRequestImpl: getLandmarks(): "
							+ first + " < 0 is not valid and was changed to 0.");
			first = landmarks.firstElement().getIndex();
		}

		if (!landmarks.isEmpty() && first > landmarks.lastElement().getIndex()) {
			System.err
					.println("ReflectionTool: ServerRequestImpl: getLandmarks(): "
							+ first
							+ " > "
							+ (landmarks.isEmpty() ? 0 : landmarks
									.lastElement().getIndex())
							+ " is larger than landmark count. Returning empty data set.");
			return new Vector<LandmarkData>();
		} else {
			Vector<LandmarkData> lms = new Vector<LandmarkData>();

			first = findPosition(landmarks, first);

			if (first >= landmarks.size()) {
				return lms;
			} else if ((landmarks.size() - first) > ServerRequestImpl.maxLandmarkCount) {
				int size = landmarks.size() - first;
				System.err
						.println("ReflectionTool: ServerRequestImpl: getLandmarks(): "
								+ size + " too large. First changed to " + size);
				first = landmarks.size() - ServerRequestImpl.maxLandmarkCount;
			}

			lms.addAll(landmarks.subList(first, landmarks.size()));

			return lms;
		}
	}

	private int findPosition(Vector<LandmarkData> landmarks, int index) {
		if (index <= 0 || landmarks == null || landmarks.size() == 0) {
			return 0;
		} else {
			int pos = landmarks.size() - 1;
			while (pos > 0 && landmarks.get(pos).getIndex() > index) {
				pos--;
			}
			return pos + 1;
		}
	}
}
