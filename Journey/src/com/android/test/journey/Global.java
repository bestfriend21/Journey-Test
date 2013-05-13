package com.android.test.journey;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Global {
	private static Global global;
		
	public static Global getInstance(Context context) {
		if (global == null) {
			global = new Global(context);
		}
		
		return global;
	}
	
	private Context mContext;
	
	// Preference
	public SharedPreferences prefs;
	public SharedPreferences.Editor prefsEditor;
	
	private DatabaseHelper helper;
	
	private Global(Context context) {
		mContext = context;
		
		// Init preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefsEditor = prefs.edit();
	}
	
	
}
