package com.android.test.journey.location;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;

import com.android.test.journey.DatabaseHelper;
import com.android.test.journey.model.CheckInData;
import com.android.test.journey.utils.Constant;

public class PlaceManager extends ConcurrentLinkedQueue<CheckInData> implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6251963724174601118L;
	
	private static PlaceManager manager;
	
	private final static String PLACE_API_URL_FORMAT = "https://maps.googleapis.com/maps/api/place/search/json?location=%f,%f&radius=10&sensor=false&key=" + Constant.MAP_API_KEY;
	
	public static PlaceManager getInstance(Context context) {
		if (manager == null)
			manager = new PlaceManager(context);
		
		return manager;
	}
	
	private Context mContext;
	private Thread mThread;
	private OnPlaceDetectedListener mListener;
	
	private PlaceManager(Context context) {
    	mContext = context;
    	
    	mThread = new Thread(this);
    	mThread.start();
    }
	
	public void setOnPlaceDetectedListener(OnPlaceDetectedListener listener) {
		mListener = listener;
	}
	
	@Override
	public void run() {
		try {
			while(!mThread.isInterrupted()) {
				CheckInData checkInData = poll();
				if (checkInData == null) {
					Thread.sleep(1000);
					continue;
				}
				
				// Retrieve address
				String place = null;
				place = getAddress(checkInData.lat, checkInData.lng);
				checkInData.place = place;
				if (place == null) {
					// push failed location to last item for the retrying
					add(checkInData);
				} else {
					// Update place to database
					DatabaseHelper.getInstance(mContext).updateCheckInData(checkInData);
				}
				
				if (mListener != null && checkInData.place != null) {
					mListener.onPlaceDetectedListener(checkInData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getAddress(double lat, double lng) {
		// Convert the string to a URL so we can parse it
		String urlStr = String.format(PLACE_API_URL_FORMAT, lat, lng);
		String address = null;
		
		try {
			URL url = new URL(urlStr);
			
			HttpClient httpclient = new DefaultHttpClient();  
            HttpGet request = new HttpGet();
            request.setURI(url.toURI());
            ResponseHandler<String> handler = new BasicResponseHandler();  
            try {  
            	String json = httpclient.execute(request, handler); 
            	httpclient.getConnectionManager().shutdown();
            	
            	PlaceJSONParser parser = new PlaceJSONParser();
    			JSONObject object = new JSONObject(json);
    			List<HashMap<String, String>> result = parser.parse(object);
    			if (result != null && result.size() > 0) {
    				HashMap<String, String> place = result.get(0);
    				address = place.get("name");
    				if (address == null) {
    					address = place.get("vicinity");	
    				}
    			}
            } catch (ClientProtocolException e) {  
                e.printStackTrace();  
            } catch (Exception e) {  
                e.printStackTrace();  
            }
            
            
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return address;
	}
}
