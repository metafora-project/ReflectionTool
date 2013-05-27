package de.kuei.metafora.reflectiontool.client.serverlink;

import java.util.Vector;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import de.kuei.metafora.reflectiontool.shared.LandmarkData;

@RemoteServiceRelativePath("serverRequest")
public interface ServerRequest extends RemoteService {

	public Vector<LandmarkData> getLandmarks(String group, String challengeId,
			int first);
}
