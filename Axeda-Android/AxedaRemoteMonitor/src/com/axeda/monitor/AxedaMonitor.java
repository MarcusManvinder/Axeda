package com.axeda.monitor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;

import android.app.Application;
import android.util.Log;

public class AxedaMonitor extends Application {
	private AxedaScripto axedaScripto = new AxedaScripto();
	
	   @Override
	    public void onCreate() {
	     

	    }
	   
	   public void setURL(String url)
	   {
		   this.axedaScripto.setURL(url);
	   }
	   public boolean login(String username, String password)
	   {
		   return this.axedaScripto.login(username, password);
	   }
	   public String callScripto(String script,ArrayList<NameValuePair> params)
	   {
		   return this.axedaScripto.callScripto(script, params);
	   }
}
