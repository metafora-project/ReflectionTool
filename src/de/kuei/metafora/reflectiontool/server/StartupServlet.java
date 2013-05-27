package de.kuei.metafora.reflectiontool.server;

import javax.servlet.http.HttpServlet;

import de.kuei.metafora.reflectiontool.server.xmpp.XMPPConnection;

public class StartupServlet extends HttpServlet {

	public void init() {
		try {
			XMPPConnection.setServer("metafora.ku-eichstaett.de");
		} catch (Exception e) {
			e.printStackTrace();
		}
		XMPPConnection.getInstance();
	}

	@Override
	public void destroy() {
	}
}
