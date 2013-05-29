package de.kuei.metafora.reflectiontool.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.kuei.metafora.reflectiontool.server.StartupServlet;
import de.kuei.metafora.reflectiontool.server.xml.XMLMessage;
import de.kuei.metafora.reflectiontool.server.xmpp.MessageEvaluator;

public class HistoryRequest {

	private static void trustAll() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				// No need to implement.
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				// No need to implement.
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void request(String challengeId, String group)
			throws IOException {

		System.err.println("ReflectionTool: History request for: "
				+ challengeId + ", " + group);

		trustAll();

		int limit = 100;
		int limitstart = 0;
		int lastsize = 0;

		Vector<String> lines = new Vector<String>();

		do {
			lastsize = lines.size();

			String urltext = StartupServlet.tomcatserver
					+ "/metaforaservicemodul/metaforaservicemodul/logrequest?chat=analysis%25&group="
					+ group + "&challenge=" + challengeId + "&limit=" + limit
					+ "&limitstart=" + limitstart;

			URL url = new URL(urltext);

			URLConnection connection = url.openConnection();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}

			limitstart += limit;
		} while (lines.size() - lastsize == 100);

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.matches(".*[<][ ]*description[ ]*[>].*[<][ ]*[/]description[ ]*[>].*")) {
				XMLMessage msg = new XMLMessage(line);
				if (msg.isValidMessage()) {
					System.err.println("Handle landmark: "
							+ msg.getXMLMessage().replaceAll("\n", " "));
					MessageEvaluator.getInstance().handleMessage(msg);
				}
			}
		}
	}

}
