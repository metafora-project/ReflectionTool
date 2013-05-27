package de.kuei.metafora.reflectiontool.client.serverlink;

import java.util.Vector;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.reflectiontool.shared.LandmarkData;

public interface ServerRequestAsync {

	void getLandmarks(String group, String challengeId, int first,
			AsyncCallback<Vector<LandmarkData>> callback);

}
