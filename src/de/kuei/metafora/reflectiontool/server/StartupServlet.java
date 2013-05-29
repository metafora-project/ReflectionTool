package de.kuei.metafora.reflectiontool.server;

import java.util.Vector;

import javax.servlet.http.HttpServlet;

import de.kuei.metafora.reflectiontool.server.mysql.ChannelDescription;
import de.kuei.metafora.reflectiontool.server.mysql.MysqlInitConnector;
import de.kuei.metafora.reflectiontool.server.mysql.ServerDescription;
import de.kuei.metafora.reflectiontool.server.xmpp.MessageEvaluator;
import de.kuei.metafora.xmppbridge.xmpp.NameConnectionMapper;
import de.kuei.metafora.xmppbridge.xmpp.ServerConnection;
import de.kuei.metafora.xmppbridge.xmpp.XmppMUC;
import de.kuei.metafora.xmppbridge.xmpp.XmppMUCManager;

public class StartupServlet extends HttpServlet {

	public static XmppMUC command = null;
	public static String tomcatserver = "https://metaforaserver.ku.de";

	public void init() {

		MysqlInitConnector.getInstance().loadData("ReflectionTool");

		ServerDescription tomcatServer = MysqlInitConnector.getInstance()
				.getAServer("tomcat");

		StartupServlet.tomcatserver = tomcatServer.getServer();

		System.err.println("Tomcat server: " + tomcatserver);

		System.err.println("Config XMPP...");

		// configure xmpp
		Vector<ServerDescription> xmppServers = MysqlInitConnector
				.getInstance().getServer("xmpp");

		for (ServerDescription xmppServer : xmppServers) {
			System.err.println("XMPP server: " + xmppServer.getServer());
			System.err.println("XMPP user: " + xmppServer.getUser());
			System.err.println("XMPP password: " + xmppServer.getPassword());
			System.err.println("XMPP device: " + xmppServer.getDevice());
			System.err.println("Modul: " + xmppServer.getModul());

			System.err.println("Starting XMPP connection...");

			NameConnectionMapper.getInstance().createConnection(
					xmppServer.getConnectionName(), xmppServer.getServer(),
					xmppServer.getUser(), xmppServer.getPassword(),
					xmppServer.getDevice());

			NameConnectionMapper.getInstance()
					.getConnection(xmppServer.getConnectionName())
					.addPacketListener(MessageEvaluator.getInstance());

			NameConnectionMapper.getInstance()
					.getConnection(xmppServer.getConnectionName()).login();
		}

		Vector<ChannelDescription> channels = MysqlInitConnector.getInstance()
				.getXMPPChannels();

		for (ChannelDescription channeldesc : channels) {
			ServerConnection connection = NameConnectionMapper.getInstance()
					.getConnection(channeldesc.getConnectionName());

			if (connection == null) {
				System.err.println("StartupServlet: Unknown connection: "
						+ channeldesc.getUser());
				continue;
			}

			System.err.println("Joining channel " + channeldesc.getChannel()
					+ " as " + channeldesc.getAlias());

			XmppMUC muc = XmppMUCManager.getInstance().getMultiUserChat(
					channeldesc.getChannel(), channeldesc.getAlias(),
					connection);
			muc.join(0);

			if (channeldesc.getChannel().equals("command")) {
				System.err.println("StartupServlet: command configured.");
				command = muc;
			}
		}
	}

	@Override
	public void destroy() {
		Vector<ServerDescription> xmppServers = MysqlInitConnector
				.getInstance().getServer("xmpp");

		for (ServerDescription xmppServer : xmppServers) {
			NameConnectionMapper.getInstance()
					.getConnection(xmppServer.getConnectionName()).disconnect();
		}
	}
}
