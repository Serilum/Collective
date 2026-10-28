package com.natamus.collective.functions;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class HttpFunctions {
	public static String getResponseBody(String url, int timeoutSeconds) {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection)URI.create(url).toURL().openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(timeoutSeconds * 1000);
			connection.setInstanceFollowRedirects(false);
			if (connection.getResponseCode() != 200) {
				return null;
			}

			try (InputStream inputStream = connection.getInputStream()) {
				return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
		catch (Exception ignored) {
			return null;
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
