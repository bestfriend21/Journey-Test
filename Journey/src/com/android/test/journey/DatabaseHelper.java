package com.android.test.journey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import junit.framework.Assert;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.test.journey.model.CheckInData;
import com.android.test.journey.utils.Constant;
import com.android.test.journey.utils.Utility;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static DatabaseHelper databaseHelper;
	
	private Context mContext;

	// Defines database name and table names
	public final static String DB_NAME = "journey.sqlite";// the extension may
															// be .sqlite or .db

	private final static String DATE_FORMAT = "yyyy MM dd";
	
	// Defines db error
	public enum DBError {
		DB_ERROR_NONE, DB_ERROR_FAILED, DB_ERROR_ALREADY_EXIST, DB_ERROR_NOT_EXIST,
	};

	private String DB_PATH;

	public SQLiteDatabase mDatabase;
	
	public static DatabaseHelper getInstance(Context context) {
		if (databaseHelper == null) {
			try {
				databaseHelper = new DatabaseHelper(context);
			} catch (Exception e) {}
		}
		
		return databaseHelper;
	}
	
	private DatabaseHelper(Context context) throws IOException {
		super(context, DB_NAME, null, 1);
		mContext = context;

		DB_PATH = "/data/data/"
				+ mContext.getApplicationContext().getPackageName()
				+ "/databases/";

		boolean dbexist = checkDatabase();
		if (dbexist) {
			openDatabase();
		} else {
			createDatabase();
			openDatabase();
		}
	}

	public void createDatabase() throws IOException {
		boolean dbexist = checkDatabase();

		if (dbexist) {
			System.out.println(" Database exists.");
		} else {
			this.getWritableDatabase();
			try {
				copyDatabase();
			} catch (IOException e) {
				// throw new Error("Error copying mDatabase");
			}
		}
	}

	public void closeDatabase() {
		mDatabase.close();
	}

	private boolean checkDatabase() {
		boolean checkdb = false;
		try {
			String myPath = DB_PATH + DB_NAME;
			File dbfile = new File(myPath);
			checkdb = dbfile.exists();
		} catch (SQLiteException e) {
			System.out.println("Database doesn't exist");
		}

		return checkdb;
	}

	public void copyDatabase(String orgPath) throws IOException {
		// Open your local db as the input stream
		FileInputStream input = new FileInputStream(orgPath);

		// Open the empty db as the output stream
		String myDBPath = DB_PATH + DB_NAME;
		(new File(myDBPath)).delete();

		OutputStream output = new FileOutputStream(myDBPath);

		// transfer byte to inputfile to outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = input.read(buffer)) > 0) {
			output.write(buffer, 0, length);
		}

		// Close the streams
		output.flush();
		output.close();
		input.close();
	}

	private void copyDatabase() throws IOException {
		// Open your local db as the input stream
		InputStream input = mContext.getAssets().open(DB_NAME);

		// Open the empty db as the output stream
		String myDBPath = DB_PATH + DB_NAME;

		OutputStream output = new FileOutputStream(myDBPath);

		// transfer byte to inputfile to outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = input.read(buffer)) > 0) {
			output.write(buffer, 0, length);
		}

		// Close the streams
		output.flush();
		output.close();
		input.close();
	}

	public void openDatabase() throws SQLException {
		// Open the mDatabase
		String mypath = DB_PATH + DB_NAME;
		mDatabase = SQLiteDatabase.openDatabase(mypath, null,
				SQLiteDatabase.OPEN_READWRITE);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	/************************************* CHECKIN *************************************/
	public synchronized DBError loadCheckedinDateList(ArrayList<Long> dateList) {
		// clear all favorite list
		Assert.assertTrue(dateList != null);
		dateList.clear();

		try {
			String rawQuery = "select distinct date from tbl_checkin";
			Cursor listCursor = mDatabase.rawQuery(rawQuery, null);
			
			if (listCursor == null)
				return DBError.DB_ERROR_FAILED;

			if (listCursor.getCount() > 0) {
				while (listCursor.moveToNext() == true) {
					String dateStr = listCursor.getString(listCursor.getColumnIndex("date"));
					long date = Utility.getDate(dateStr, DATE_FORMAT).getTime();
					dateList.add(date);
				}
			}

			// Close query
			listCursor.close();
		} catch (SQLiteException e) {
			return DBError.DB_ERROR_FAILED;
		}

		return DBError.DB_ERROR_NONE;
	}
	
	// Load all check-in data on specified date
		public synchronized DBError loadCheckinDataList(ArrayList<CheckInData> dataList) {
			// clear all favorite list
			Assert.assertTrue(dataList != null);
			dataList.clear();

			try {
				String rawQuery = "select * from tbl_checkin order by time";
				Cursor listCursor = mDatabase.rawQuery(rawQuery, null);
				
				if (listCursor == null)
					return DBError.DB_ERROR_FAILED;

				if (listCursor.getCount() > 0) {
					while (listCursor.moveToNext() == true) {
						CheckInData newData = new CheckInData();
						newData.lat = listCursor.getDouble(listCursor.getColumnIndex("lat"));
						newData.lng = listCursor.getDouble(listCursor.getColumnIndex("lng"));
						newData.time = listCursor.getLong(listCursor.getColumnIndex("time"));
						newData.place = listCursor.getString(listCursor.getColumnIndex("place"));
						
						dataList.add(newData);
					}
				}

				// Close query
				listCursor.close();
			} catch (SQLiteException e) {
				return DBError.DB_ERROR_FAILED;
			}

			return DBError.DB_ERROR_NONE;
		}
		
	// Load all check-in data on specified date
	public synchronized DBError loadCheckinDataListOnDate(ArrayList<CheckInData> dataList, long date) {
		// clear all favorite list
		Assert.assertTrue(dataList != null);
		dataList.clear();

		long startTime;
		long endTime;
		
		startTime = Utility.getDateTimeMilis(date);
		endTime = startTime + Constant.DATE_MILISECONDS;
		try {
			String rawQuery = "select * from tbl_checkin where time >= " + startTime + " and time < " + 
					endTime + " order by time";
			Cursor listCursor = mDatabase.rawQuery(rawQuery, null);
			
			if (listCursor == null)
				return DBError.DB_ERROR_FAILED;

			if (listCursor.getCount() > 0) {
				while (listCursor.moveToNext() == true) {
					CheckInData newData = new CheckInData();
					newData.lat = listCursor.getDouble(listCursor.getColumnIndex("lat"));
					newData.lng = listCursor.getDouble(listCursor.getColumnIndex("lng"));
					newData.time = listCursor.getLong(listCursor.getColumnIndex("time"));
					newData.place = listCursor.getString(listCursor.getColumnIndex("place"));
					
					dataList.add(newData);
				}
			}

			// Close query
			listCursor.close();
		} catch (SQLiteException e) {
			return DBError.DB_ERROR_FAILED;
		}

		return DBError.DB_ERROR_NONE;
	}

	/**
	 * Add check-in data
	 */
	public synchronized DBError addCheckInData(CheckInData data) {
		ContentValues values = new ContentValues();
		long result;
		
		Assert.assertTrue(data != null);
		
		values.put("lat",  data.lat);
		values.put("lng",  data.lng);
		values.put("time",  data.time);
		
		String dateStr = Utility.getDateString(data.time, DATE_FORMAT);
		values.put("date",  dateStr);
		values.put("place",  data.place);
		
		result = mDatabase.insert("tbl_checkin", null, values);
		if (result == -1)
			return DBError.DB_ERROR_FAILED;
		
		return DBError.DB_ERROR_NONE;
	}
	
	// Update check-in data
	public synchronized DBError updateCheckInData(CheckInData data) {
		Assert.assertTrue(data != null);
		
		DBError result = DBError.DB_ERROR_NONE;
		try {
			// Append new shortcut item to mDatabase
			ContentValues values = new ContentValues();
			values.put("lat",  data.lat);
			values.put("lng",  data.lng);
			values.put("time",  data.time);
			values.put("date",  Utility.getDateString(data.time, DATE_FORMAT));
			values.put("place",  data.place);

			long ret = mDatabase.update("tbl_checkin", values, "time=?" ,
					new String[] { String.valueOf(data.time) });
			if (ret == -1)
				result = DBError.DB_ERROR_FAILED;
			else if (ret == 0)
				result = DBError.DB_ERROR_NOT_EXIST;

		} catch (SQLiteException e) {
			result = DBError.DB_ERROR_FAILED;
		}
		
		return result;
	}
	
	/**
	 * Remove check-in data
	 */
	public synchronized DBError removeCheckInData(CheckInData data) {
		int result;

		Assert.assertTrue(data != null);
		
		result = mDatabase.delete("tbl_checkin", "time=?", 
				new String[] {String.valueOf(data.time)});

		if (result == -1)
			return DBError.DB_ERROR_FAILED;
		else if (result == 0)
			return DBError.DB_ERROR_NOT_EXIST;
		else
			return DBError.DB_ERROR_NONE;
	}
}