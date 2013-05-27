package de.kuei.metafora.reflectiontool.server.xmpp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.GregorianCalendar;

import de.kuei.metafora.xmppbridge.xmpp.NameConnectionMapper;
import de.kuei.metafora.xmppbridge.xmpp.ServerConnection;
import de.kuei.metafora.xmppbridge.xmpp.XmppMUC;
import de.kuei.metafora.xmppbridge.xmpp.XmppMUCManager;

public class XMPPConnection {

	private static String channel = "analysis";
	private static String xmppserver = "metaforaserver.ku.de";
	private static String user = "ReflectionTool";
	private static String password = "didPfdRT";
	private static String commandChannel = "command";

	private static XMPPConnection instance = null;

	public static XMPPConnection getInstance() {
		if (instance == null) {
			instance = new XMPPConnection();
		}
		return instance;
	}

	public static void setPassword(String password) throws Exception {
		if (instance != null) {
			Exception ex = new Exception("Connection was already established!");
			ex.printStackTrace();
			throw ex;
		}
		System.err.println("Password changed from " + XMPPConnection.password + " to "
				+ password);
		XMPPConnection.password = password;
	}

	public static void setUser(String user) throws Exception {
		if (instance != null) {
			Exception ex = new Exception("Connection was already established!");
			ex.printStackTrace();
			throw ex;
		}
		System.err.println("User changed from " + XMPPConnection.user + " to " + user);
		XMPPConnection.user = user;
	}

	public static void setServer(String server) throws Exception {
		if (instance != null) {
			Exception ex = new Exception("Connection was already established!");
			ex.printStackTrace();
			throw ex;
		}
		System.err.println("Server changed from " + xmppserver + " to " + server);
		xmppserver = server;
	}

	public static void setChannel(String channel) throws Exception {
		if (instance != null) {
			Exception ex = new Exception("Connection was already established!");
			ex.printStackTrace();
			throw ex;
		}
		System.err.println("Channel changed from " + XMPPConnection.channel + " to "
				+ channel);
		XMPPConnection.channel = channel;
	}

	private String alias = null;
	private String device = null;

	private XmppMUC commandMuc = null;

	private XMPPConnection() {
		setup();
		connect();
	}

	private void setup() {
		String ip = "unknown";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		String host = "unknown";
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		String systemuser = "applet";

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		String time = calendar.get(GregorianCalendar.DAY_OF_MONTH) + "-"
				+ (calendar.get(GregorianCalendar.MONTH) + 1) + "-"
				+ calendar.get(GregorianCalendar.YEAR) + "_"
				+ calendar.get(GregorianCalendar.HOUR_OF_DAY) + "-"
				+ calendar.get(GregorianCalendar.MINUTE) + "-"
				+ calendar.get(GregorianCalendar.SECOND) + "-"
				+ calendar.get(GregorianCalendar.MILLISECOND);

		device = systemuser + "From" + host + "(" + ip + ")At" + time;
		alias = user + "@" + xmppserver + "/" + device;

		System.err.println("Device: " + device);
		System.err.println("Alias: " + alias);
	}

	public void connect() {
		String server = "analysis@conference." + xmppserver;

		System.err.println("Connecting to XMPP server: " + server);
		System.err.println("User: " + user);
		System.err.println("Password: " + password);
		System.err.println("Alias: " + alias);
		System.err.println("Device: " + device);

		ServerConnection connection = NameConnectionMapper.getInstance()
				.createConnection(channel, xmppserver, user, password, device);
		connection.addPacketListener(MessageEvaluator.getInstance());
		connection.login();

		XmppMUC analysisMuc = XmppMUCManager.getInstance().getMultiUserChat(
				channel, alias, connection);
		analysisMuc.join(0);

		System.err.println("connect to command");
		System.err.println("channel: " + commandChannel);
		System.err.println("alias: " + alias);

		commandMuc = XmppMUCManager.getInstance().getMultiUserChat(
				commandChannel, alias, connection);
		commandMuc.join(0);
	}

	public void sendToCommand(String message) {
		System.err.println("sendToCommand: " + commandMuc + ", " + message);
		if (commandMuc != null && message != null) {
			try {
				commandMuc.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
				// ignore
			}
		}
	}

	public String getAnalysisChannel() {
		return channel;
	}
}
