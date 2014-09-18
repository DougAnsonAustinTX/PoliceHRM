/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package no.nordicsemi.android.nrftoolbox.hrs;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity;

import org.achartengine.GraphicalView;

import com.arm.sensinode.gateway.SensinodeService;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * HRSActivity is the main Heart rate activity. It implements HRSManagerCallbacks to receive callbacks from HRSManager class. The activity supports portrait and landscape orientations. The activity
 * uses external library AChartEngine to show real time graph of HR values.
 */
public class HRSActivity extends BleProfileActivity implements HRSManagerCallbacks {
	@SuppressWarnings("unused")
	private final String TAG = "HRSActivity";

	private static final String GRAPH_STATUS = "graph_status";
	private static final String GRAPH_COUNTER = "graph_counter";
	private static final String HR_VALUE = "hr_value";

	// Tunables - should match main.cpp in PoliceHRM (mbed)
	private final int HRM_OFF			= 0;		// offline
	private final int HRM_MIN_VALUE 	= 10;		// min hrm
	private final int HRM_MAX_VALUE 	= 250;		// max hrm
	private final int HRM_ITERATION_MS 	= 3000;		// in ms
	
	private Handler mHandler = new Handler();

	private boolean isGraphInProgress = false;

	private GraphicalView mGraphView;
	private LineGraphView mLineGraph;
	private TextView mHRSValue, mHRSPosition, mBattery;

	private int mInterval = HRM_ITERATION_MS;
	private int mHrmValue = HRM_OFF;
	private int mCounter = 0;

	@Override
	protected void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_hrs);
		setGUI();
	}

	private void setGUI() {
		mLineGraph = LineGraphView.getLineGraphView();
		mHRSValue = (TextView) findViewById(R.id.text_hrs_value);
		mHRSPosition = (TextView) findViewById(R.id.text_hrs_position);
		mBattery = (TextView)findViewById(R.id.battery);
		showGraph();
	}

	private void showGraph() {
		mGraphView = mLineGraph.getView(this);
		ViewGroup layout = (ViewGroup) findViewById(R.id.graph_hrs);
		layout.addView(mGraphView);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState != null) {
			isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
			mCounter = savedInstanceState.getInt(GRAPH_COUNTER);
			mHrmValue = savedInstanceState.getInt(HR_VALUE);

			if (isGraphInProgress)
				startShowGraph();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(GRAPH_STATUS, isGraphInProgress);
		outState.putInt(GRAPH_COUNTER, mCounter);
		outState.putInt(HR_VALUE, mHrmValue);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopShowGraph();
	}

	@Override
	protected int getAboutTextId() {
		return R.string.hrs_about_text;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.hrs_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		return HRSManager.HR_SERVICE_UUID;
	}

	private void updateGraph(final int hrmValue) {
		mCounter++;
		mLineGraph.addValue(new Point(mCounter, hrmValue));
		mGraphView.repaint();
		mBattery.setText("100%");	// fake battery for now...
	}

	private Runnable mRepeatTask = new Runnable() {
		@Override
		public void run() {
			updateGraph(mHrmValue);
			if (isGraphInProgress)
				mHandler.postDelayed(mRepeatTask, mInterval);
		}
	};

	void startShowGraph() {
		isGraphInProgress = true;
		mRepeatTask.run();
	}

	void stopShowGraph() {
		isGraphInProgress = false;
		mHandler.removeCallbacks(mRepeatTask);
	}

	@Override
	protected BleManager<HRSManagerCallbacks> initializeManager() {
		HRSManager manager = HRSManager.getInstance(this);
		manager.setGattCallbacks(this);
		return manager;
	}

	private void setHRSValueOnView(final int value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (value != HRM_OFF) {
					mHRSValue.setText(Integer.toString(value));
				} else {
					mHRSValue.setText(R.string.not_available_value);
				}
			}
		});
	}

	private void setHRSPositionOnView(final String position) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (position != null) {
					mHRSPosition.setText(position);
				} else {
					mHRSPosition.setText(R.string.not_available);
				}
			}
		});
	}

	@Override
	public void onServicesDiscovered(boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	@Override
	public void onHRSensorPositionFound(String position) {
		setHRSPositionOnView(position);
	}

	@Override
	public void onHRNotificationEnabled() {
		startShowGraph();
	}

	// HOOK: when we update our UI, lets also update MDS...
	@Override
	public void onHRValueReceived(int value) {
		mHrmValue = value;
		
		// boundary checks...
		if (mHrmValue < 0) mHrmValue = HRM_OFF;
		else if (mHrmValue <= HRM_MIN_VALUE) mHrmValue = HRM_MIN_VALUE;
		else if (mHrmValue >= HRM_MAX_VALUE) mHrmValue = HRM_MAX_VALUE;
		
		// set the HRM value
		setHRSValueOnView(mHrmValue);
		
		// Also update MDS with the latest HRM value
		if (SensinodeService.getInstance() != null) SensinodeService.getInstance().getMDSHRMResource().updateHRMValue(mHrmValue);
	}

	@Override
	public void onDeviceDisconnected() {
		super.onDeviceDisconnected();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mHRSValue.setText(R.string.not_available_value);
				mHRSPosition.setText(R.string.not_available);
				stopShowGraph();
			}
		});
	}

	@Override
	protected void setDefaultUI() {
		mHRSValue.setText(R.string.not_available_value);
		mHRSPosition.setText(R.string.not_available);
		clearGraph();
	}

	private void clearGraph() {
		mLineGraph.clearGraph();
		mGraphView.repaint();
		mCounter = 0;
		mHrmValue = 0;
	}
}
