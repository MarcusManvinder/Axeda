package com.axeda.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class AxedaScripto {
	private String sessionid;
	private String url;

	public void setURL(String url) {
		this.url = url;
	}

	public boolean login(String username, String password) {
		boolean ok = false;
		HttpClient httpclient = new DefaultHttpClient();

		HttpGet httpget = new HttpGet(this.url
				+ "/services/v1/rest/Auth/login?principal.username="
				+ username.trim() + "&password=" + password.trim());

		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			InputStream instream = entity.getContent();
			if (entity != null) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(instream));
				String resp = new String();
				resp = reader.readLine();
				if (!resp.contains("error")) {
					int resp1 = resp.indexOf("<ns1:sessionId>", 1);
					int resp2 = resp.indexOf("</ns1:sessionId>");
					this.sessionid = resp.substring(resp1 + 15, resp2);
					ok = true;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException ex) {
			httpget.abort();
		}

		httpclient.getConnectionManager().shutdown();
		Log.i("scripto", "login");
		Log.i("scripto", "success:" + ok);
		return ok;
	}

	public String callScripto(String script, ArrayList<NameValuePair> params) {
		String response = "";
		PostMethod postScripto = new PostMethod(this.url
				+ "/services/v1/rest/Scripto/execute/" + script + "?sessionid="
				+ this.sessionid);
		if (params != null) {
			for (NameValuePair nvp : params)
				postScripto.addParameter(nvp);
		} else {
			postScripto.addParameter("foo", "bar");
		}

		try {
			org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
			int status = client.executeMethod(postScripto);
			response = postScripto.getResponseBodyAsString();
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}

		Log.i("scripto", script);
		if (params != null) {
			for (NameValuePair nvp : params)
				Log.i("scripto", nvp.toString());
		}
		Log.i("scripto", response);

		return response;
	}

}
