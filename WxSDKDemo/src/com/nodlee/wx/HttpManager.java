package com.nodlee.wx;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpManager {
	private static final String TAG = "HttpManager";
	private String charset = "UTF-8";

	public HttpManager() {
	}

	private byte[] execute(URL url, String query) throws IOException   {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(15000);
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=" + charset);

			if (query != null) {
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				BufferedOutputStream bos = new BufferedOutputStream(
						connection.getOutputStream());
				bos.write(query.getBytes(charset));
				bos.flush();
				bos.close();
			}

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.e(TAG, "数据请求失败, 响应码:" + connection.getResponseCode());
				return null;
			}
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();
			
			int byteRead = 0;
			byte[] buffer = new byte[1024];
			while ((byteRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, byteRead);
			}
			out.close();
			return out.toByteArray();
			
//			BufferedReader reader = null;
//			reader = new BufferedReader(new InputStreamReader(in, charset));
//
//			String line = null;
//			StringBuilder builder = new StringBuilder();
//			while ((line = reader.readLine()) != null) {
//				builder.append(line);
//			}
//			in.close();
//			return builder.toString();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public String executePost(URL url, String query) throws IOException {
		byte[] result = execute(url, query);
		if (result == null || result.length == 0) {
			return null;
		} else {
			return new String(result);
		}
	}

	public String executeGet(URL url) throws IOException {
		byte[] result = execute(url, null);
		if (result == null || result.length == 0) {
			return null;
		} else {
			return new String(result);
		}
	}
	
	public byte[] getUrlBytes(URL url) throws IOException {
		return execute(url, null);
	}
}
