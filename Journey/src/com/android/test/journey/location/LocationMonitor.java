package com.android.test.journey.location;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationMonitor {
	public final static int REFRESH_UPDATE_MIN_TIME = 1000;
	public final static float REFRESH_UPDATE_MIN_DISTANCE = 10.0f;
	
	private static LocationMonitor handler;
	
	public static LocationMonitor getInstance(Context context) {
		if (handler == null)
			handler = new LocationMonitor(context);
		
		return handler;
	}
	
	private Context mContext;
	private LocationManager locationManager;
	private OnLocationChangedListener mListener;
	private boolean gps_enabled = false;
	private boolean network_enabled = false;
    
    Location mCurrLoc = null;

    private int mMinTime;
    private int mMinDistance;
    public static final long MAX_MTIMEOUT = 1000 * 60 * 60 * 24 * 10; 
    // private long mTimeout = MAX_MTIMEOUT;
    
    private boolean mDisabledByUser = true;
    
    private LocationMonitor(Context context) {
    	mContext = context;
    }
    
    //////////////////////////////// PUBLIC API //////////////////////////////////////
    @Deprecated
    public boolean getLocation(Context context, OnLocationChangedListener listener, int timeout) {
    	return getLocation(context, listener, REFRESH_UPDATE_MIN_TIME, REFRESH_UPDATE_MIN_DISTANCE, false);
    }
    
    public boolean getLocation(Context context, final OnLocationChangedListener listener, int minTime, float minDistance, boolean slientMode) {
    	mContext = context;
    	network_enabled = false;
        gps_enabled 	= false;
        
        //I use OnLocationChangedListener callback class to pass location value from LocationMonitor to user code.
        mListener = listener;
        if(locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try{gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
        try{network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

        //don't start listeners if no provider is enabled
        if (!slientMode) {
	        if(!gps_enabled && !network_enabled) {
	        	((Activity)mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
						alertDialog.setTitle("GPS");
						alertDialog.setMessage("May I use your location?");
						alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
						   public void onClick(DialogInterface dialog, int which) {
							   	Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							   	mContext.startActivity(intent);
								dialog.dismiss();
						   }
						});
						alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									mDisabledByUser = false;
									mListener.onCancel();
								}
						});
						
						alertDialog.show();
					}
	        		
	        	});
	        }
    	}
        
        if (minTime == 0) {
        	mCurrLoc = getLastLocation();
        	if (mCurrLoc != null)
        		return true;
        }
        
        Log.d("gpshandler", "*** gps_enabled = " + gps_enabled);
        Log.d("gpshandler", "*** network_enabled = " + network_enabled);
		if (gps_enabled) {
			if (!slientMode && (context instanceof Activity)) {
				((Activity)context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								mMinTime, mMinDistance, locationListenerGps);
					}
				});
			} else {
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER,
						minTime,
						minDistance, locationListenerGps);
			}
		}

		if (network_enabled) {
			if (!slientMode && (context instanceof Activity)) {
				((Activity)context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						locationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER,
								mMinTime, mMinDistance,
								locationListenerNetwork);
					}
				});
			} else {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						minTime, minDistance,
						locationListenerNetwork);
			}
		}
			
        return true;
    }

    public void stop() {
    	locationManager.removeUpdates(locationListenerNetwork);
    	locationManager.removeUpdates(locationListenerGps);
    }
    
    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
        	if (location != null)
        		Log.d("gpshandler", "Location changed = " + location);
            if (mListener.gotLocation(location) != null);
            	stop();
        }
        
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
        	if (mListener.gotLocation(location) != null);
            	stop();
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
    
    public Location getLastLocation()
    {
        Location net_loc = null, gps_loc = null;
        
        if(gps_enabled)
            gps_loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(network_enabled)
            net_loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        //if there are both values use the latest one
        if(gps_loc != null && net_loc != null){
            if(gps_loc.getTime() > net_loc.getTime()) {
            	mCurrLoc = gps_loc;
            	if (mListener != null)
            		mCurrLoc = mListener.gotLocation(gps_loc);
            } else {
            	mCurrLoc = net_loc;
            	if (mListener != null)
            		mCurrLoc = mListener.gotLocation(net_loc);
            }
            return mCurrLoc;
        }

        if(gps_loc != null){
        	mCurrLoc = gps_loc;
        	if (mListener != null)
        		mCurrLoc = mListener.gotLocation(gps_loc);
            return mCurrLoc;
        }
        if(net_loc != null){
        	mCurrLoc = net_loc;
        	if (mListener != null)
        		mCurrLoc = mListener.gotLocation(net_loc);
            return mCurrLoc;
        }
        if (mListener != null)
        	mCurrLoc = mListener.gotLocation(null);
        return mCurrLoc;
    }
    
    public static abstract class OnLocationChangedListener {
        public abstract Location gotLocation(Location location);
        public abstract void onCancel();
    }
}
