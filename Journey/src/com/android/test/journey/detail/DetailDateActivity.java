package com.android.test.journey.detail;

import java.util.ArrayList;

import junit.framework.Assert;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.test.journey.DatabaseHelper;
import com.android.test.journey.R;
import com.android.test.journey.location.OnPlaceDetectedListener;
import com.android.test.journey.location.PlaceManager;
import com.android.test.journey.model.CheckInData;
import com.android.test.journey.utils.Utility;

public class DetailDateActivity extends Activity implements OnPlaceDetectedListener {
	public final static String INTENT_PARAM_KEY_DATE = "date";
	
	private long mDate;
	private ArrayList<CheckInData> mCheckedInDataList = new ArrayList<CheckInData>();
	private DateAdapter mDateAdapter = new DateAdapter();
	
	private ListView mLVDates;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_date);
		
		Bundle extra = getIntent().getExtras();
		Assert.assertTrue(extra != null);
		
		mDate = extra.getLong(INTENT_PARAM_KEY_DATE);
		
		initUI();
		updateUI();
		
		// Attach place detected listener
		PlaceManager.getInstance(this).setOnPlaceDetectedListener(this);
	}
	
	private void initUI() {
		mLVDates = (ListView) findViewById(R.id.lvDate);
		mLVDates.setAdapter(mDateAdapter);
		
		mLVDates.setOnItemClickListener(mItemClickListener);
	}
	
	private void updateUI() {
		DatabaseHelper.getInstance(this).loadCheckinDataListOnDate(mCheckedInDataList, mDate);
		mDateAdapter.notifyDataSetChanged();
	}
	
	// ///////////////////// EVENTS ////////////////////////////
	private OnItemClickListener mItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int arg2,
				long arg3) {
			CheckInData data = (CheckInData) v.getTag();
			Intent intent = new Intent(DetailDateActivity.this, DetailMapActivity.class);
			startActivity(intent);
		}
	};

	@Override
	public void onPlaceDetectedListener(CheckInData checkinData) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateUI();
			}
		});
	}
	
	// ///////////////////// ADAPTER ////////////////////////////
	public class DateAdapter extends BaseAdapter {
		public DateAdapter() {
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			CheckInData data = (CheckInData) getItem(position);

			View view;
			if (convertView == null) {
				final LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.table_item_cell, null);

			} else {
				view = (View) convertView;
			}
			view.setTag(data);

			ProgressBar pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);
			
			TextView tvLocation = (TextView) view.findViewById(R.id.tvPlace);
			if (data.place != null) {
				tvLocation.setText(data.place);
				pbLoading.setVisibility(View.GONE);
			} else {
				tvLocation.setText(String.valueOf(data.lat) + ", " + String.valueOf(data.lng));
				pbLoading.setVisibility(View.VISIBLE);
			}

			TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
			String timeStr = Utility.getDateString(data.time, "hh:mm:ss a");
			tvTime.setText(timeStr);
			
			return view;
		}

		public final int getCount() {
			return mCheckedInDataList.size();
		}

		public final Object getItem(int position) {
			return mCheckedInDataList.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}
}
