package com.android.test.journey;

import java.util.ArrayList;

import junit.framework.Assert;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.test.journey.detail.DetailDateActivity;
import com.android.test.journey.location.LocationMonitor;
import com.android.test.journey.location.PlaceManager;
import com.android.test.journey.location.LocationMonitor.OnLocationChangedListener;
import com.android.test.journey.model.CheckInData;
import com.android.test.journey.utils.Utility;

public class MainActivity extends Activity {
	private Location mCurrLocation = null;
	private final static int GPS_REFRESH_MIN_TIME = 5000;
	private final static int GPS_REFRESH_MIN_DISTANCE = 100;
	
	private ArrayList<Long> mDataList = new ArrayList<Long>();
	private DateAdapter mDateAdapter = new DateAdapter();
	
	private Button mBtnCheckIn;
	private ListView mLVDates;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initActivity();
		initUI();
		
		updateUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void initActivity() {
		// Start GPS updater
		LocationMonitor.getInstance(this).getLocation(this, mListener, GPS_REFRESH_MIN_TIME, GPS_REFRESH_MIN_DISTANCE, false);
		
		// Add pending items to the PlaceManager to detect the location
		ArrayList<CheckInData> checkInData = new ArrayList<CheckInData>();
		DatabaseHelper.getInstance(this).loadCheckinDataList(checkInData);
		for (CheckInData data : checkInData) {
			if (data.place == null || data.place.equals("")) {
				PlaceManager.getInstance(MainActivity.this).add(data);
			}
		}
	}
	
	private void initUI() {
		mBtnCheckIn = (Button) findViewById(R.id.btnCheckin);
		mBtnCheckIn.setEnabled(false);
		mBtnCheckIn.setOnClickListener(mCheckInClickListener);
		
		mLVDates = (ListView) findViewById(R.id.lvDate);
		mLVDates.setAdapter(mDateAdapter);
		
		mLVDates.setOnItemClickListener(mItemClickListener);
	}
	
	private void updateUI() {
		DatabaseHelper.getInstance(this).loadCheckedinDateList(mDataList);
		mDateAdapter.notifyDataSetChanged();
	}
	
	///////////////////// LOCATION ///////////////////////
	private OnLocationChangedListener mListener = new OnLocationChangedListener() {
		@Override
		public void onCancel() {
			
		}
		
		@Override
		public Location gotLocation(Location location) {
			mCurrLocation = location;
			if (mCurrLocation != null) {
				mBtnCheckIn.setEnabled(true);
			} else {
				mBtnCheckIn.setEnabled(false);
			}
			
			return mCurrLocation;
		}
	};
	
	/////////////////////// EVENTS ////////////////////////////
	private OnItemClickListener mItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int arg2,
				long arg3) {
			long date = (Long) v.getTag();
			Intent intent = new Intent(MainActivity.this, DetailDateActivity.class);
			intent.putExtra(DetailDateActivity.INTENT_PARAM_KEY_DATE, date);
			startActivity(intent);
		}
	};
	
	private OnClickListener mCheckInClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Assert.assertTrue(mCurrLocation != null);
			// Add new check-in data to database
			CheckInData newData = new CheckInData();
			newData.lat = mCurrLocation.getLatitude();
			newData.lng = mCurrLocation.getLongitude();
			newData.time = System.currentTimeMillis();
			DatabaseHelper.getInstance(MainActivity.this).addCheckInData(newData);
			
			// Add this item to pending queue to detect the meaningful place
			PlaceManager.getInstance(MainActivity.this).add(newData);
			
			Toast.makeText(MainActivity.this, getString(R.string.checkedIn), Toast.LENGTH_LONG).show();
			
			updateUI();
		}
	};
	
	/////////////////////// ADAPTER ////////////////////////////
	public class DateAdapter extends BaseAdapter {
		public DateAdapter() {
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			long date = (Long)getItem(position);
			String dateStr = Utility.getDateString(date, "MMMM dd yyyy");
			
			View view;
			if (convertView == null) {
				final LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.table_date_cell, null);
                
			} else {
				view = (View) convertView;
			}
			view.setTag(date);
			
			TextView tvDate = (TextView) view.findViewById(R.id.tvLocation);
			tvDate.setText(dateStr);
			
			return view;
		}

		public final int getCount() {
			return mDataList.size();
		}

		public final Object getItem(int position) {
			return mDataList.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}
}
