package com.dkgp.vehicles;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.util.EntityUtils;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DecodeVINTask {
	
	public Vehicle decodeVIN(String barCode)
	{
		
	    String request ="{\"vehicles\":[{\"vehicle\":{\"vin\":\""+barCode+"\"}}]}";
	    
	    HttpResponse response =makeRequest("https://api.dev-2.cobalt.com/inventory/rest/v1.0/vehicles/detail?inventoryOwner=gmps-kindred&locale=en_us", request);
	    
	    
	    
        try {
        	
        	HttpEntity entity = response.getEntity();
        	JsonParser jsonParser = new JsonParser();
        	JsonObject json = jsonParser.parse(EntityUtils.toString(entity))
            	    .getAsJsonObject().getAsJsonArray("vehicles").get(0)
            	    .getAsJsonObject().getAsJsonObject("vehicle");
        	String make = json.getAsJsonObject("make").get("label").getAsString();
        	String model = json.getAsJsonObject("model").get("label").getAsString();
        	String year = json.get("year").getAsString();
        	
        	Vehicle vehicle = new Vehicle();
        	vehicle.setMake(make);
        	vehicle.setModel(model);
        	vehicle.setYear(year);
        	return vehicle;

        	
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	private HttpResponse makeRequest(String uri, String json) {
	    try {
	        HttpPost httpPost = new HttpPost(uri);
	        httpPost.setEntity(new StringEntity(json));
	        httpPost.setHeader("Accept", "application/json");
	        httpPost.setHeader("Content-type", "application/json");
	        return new DefaultHttpClient().execute(httpPost);
	        
	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

}
