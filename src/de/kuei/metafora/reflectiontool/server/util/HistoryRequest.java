package de.kuei.metafora.reflectiontool.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.kuei.metafora.reflectiontool.server.StartupServlet;
import de.kuei.metafora.reflectiontool.server.xml.XMLMessage;
import de.kuei.metafora.reflectiontool.server.xmpp.MessageEvaluator;

public class HistoryRequest {

	private static SSLSocketFactory trustAll() {
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
			return sc.getSocketFactory();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static HostnameVerifier getAnalphabeticVerifier() {
		HostnameVerifier verifier = new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		return verifier;
	}

	public static void request(String challengeId, String group)
			throws IOException {

		System.err.println("ReflectionTool: History request for: "
				+ challengeId + ", " + group);

		SSLSocketFactory factory = trustAll();

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
			System.err.println("History Request: " + url.toString());

			BufferedReader reader = null;

			try {
				// URLConnection connection = url.openConnection();
				HttpsURLConnection connection = (HttpsURLConnection) url
						.openConnection();
				connection.setSSLSocketFactory(factory);
				connection.setHostnameVerifier(getAnalphabeticVerifier());

				// connection.setHostnameVerifier(v)

				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));

			} catch (Exception ex) {
				System.err.println("History request failed ("
						+ ex.getClass().getCanonicalName() + ") with: "
						+ ex.getMessage() + ". Trying port 8443.");
				try {
					urltext = StartupServlet.tomcatserver
							+ ":8443"
							+ "/metaforaservicemodul/metaforaservicemodul/logrequest?chat=analysis%25&group="
							+ group + "&challenge=" + challengeId + "&limit="
							+ limit + "&limitstart=" + limitstart;

					url = new URL(urltext);
					System.err.println("Url: " + url.toString());

					HttpsURLConnection connection = (HttpsURLConnection) url
							.openConnection();
					connection.setSSLSocketFactory(factory);
					connection.setHostnameVerifier(getAnalphabeticVerifier());

					reader = new BufferedReader(new InputStreamReader(
							connection.getInputStream()));

				} catch (Exception innerex) {
					System.err.println("History request failed! "
							+ innerex.getMessage());
					innerex.printStackTrace();
				}
			}

			if (reader == null) {
				return;
			}

			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}

			limitstart += limit;

			System.err
					.println("ReflectionTool: HistoryRequest: request(): DB request: Challenge: "
							+ challengeId
							+ ", Group: "
							+ group
							+ ", Records: "
							+ limitstart);
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

		System.err
				.println("ReflectionTool: HistoryRequest: request(): DB request for challenge "
						+ challengeId + "and group " + group + " finished.");
	}

}
