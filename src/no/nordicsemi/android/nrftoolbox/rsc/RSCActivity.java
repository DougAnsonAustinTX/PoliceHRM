package no.nordicsemi.android.nrftoolbox.rsc;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService.LocalBinder;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

public class RSCActivity extends BleProfileServiceReadyActivity<RSCService.LocalBinder> {
	private TextView mSpeedView;
	private TextView mCadenceView;
	private TextView mDistanceView;
	private TextView mDistanceUnitView;
	private TextView mTotalDistanceView;
	private TextView mStridesCountView;
	private TextView mActivityView;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_rsc);
		setGui();
	}

	@Override
	protected void onInitialize() {
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, makeIntentFilter());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
	}

	private void setGui() {
		mSpeedView = (TextView) findViewById(R.id.speed);
		mCadenceView = (TextView) findViewById(R.id.cadence);
		mDistanceView = (TextView) findViewById(R.id.distance);
		mDistanceUnitView = (TextView) findViewById(R.id.distance_unit);
		mTotalDistanceView = (TextView) findViewById(R.id.total_distance);
		mStridesCountView = (TextView) findViewById(R.id.strides);
		mActivityView = (TextView) findViewById(R.id.activity);
	}

	@Override
	protected void setDefaultUI() {
		mSpeedView.setText(R.string.not_available_value);
		mCadenceView.setText(R.string.not_available_value);
		mDistanceView.setText(R.string.not_available_value);
		mDistanceUnitView.setText(R.string.rsc_distance_unit_m);
		mTotalDistanceView.setText(R.string.not_available_value);
		mStridesCountView.setText(R.string.not_available_value);
		mActivityView.setText(R.string.not_available);
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.rsc_feature_title;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.rsc_default_name;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.rsc_about_text;
	}

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return RSCService.class;
	}

	@Override
	protected UUID getFilterUUID() {
		return RSCManager.RUNNING_SPEED_AND_CADENCE_SERVICE_UUID;
	}

	@Override
	protected void onServiceBinded(final LocalBinder binder) {
		// not used
	}

	@Override
	protected void onServiceUnbinded() {
		// not used
	}

	@Override
	public void onServicesDiscovered(final boolean optionalServicesFound) {
		// not used
	}

	private void onMeasurementReceived(final float speed, final int cadence, final float totalDistance, final int activity) {
		mSpeedView.setText(String.format("%.1f", speed));
		mCadenceView.setText(String.format("%d", cadence));
		if (totalDistance == RSCManagerCallbacks.NOT_AVAILABLE) {
			mTotalDistanceView.setText(R.string.not_available);
		} else {
			mTotalDistanceView.setText(String.format("%.2f", totalDistance / 10000.0f)); // 1km in dm
		}

		mActivityView.setText(activity == RSCManagerCallbacks.ACTIVITY_RUNNING ? R.string.rsc_running : R.string.rsc_walking);
	}

	private void onStripsesUpdate(final float distance, final int strides) {
		if (distance == RSCManagerCallbacks.NOT_AVAILABLE) {
			mDistanceView.setText(R.string.not_available);
			mDistanceUnitView.setText(R.string.rsc_distance_unit_m);
		} else if (distance < 100000) { // 1 km in cm
			mDistanceView.setText(String.format("%.0f", distance / 100.0f));
			mDistanceUnitView.setText(R.string.rsc_distance_unit_m);
		} else {
			mDistanceView.setText(String.format("%.2f", distance / 100000.0f));
			mDistanceUnitView.setText(R.string.rsc_distance_unit_km);
		}

		mStridesCountView.setText(String.valueOf(strides));
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();

			if (RSCService.BROADCAST_RSC_MEASUREMENT.equals(action)) {
				final float speed = intent.getFloatExtra(RSCService.EXTRA_SPEED, 0.0f);
				final int cadence = intent.getIntExtra(RSCService.EXTRA_CADENCE, 0);
				final float totalDistance = intent.getFloatExtra(RSCService.EXTRA_TOTAL_DISTANCE, RSCManagerCallbacks.NOT_AVAILABLE);
				final int activity = intent.getIntExtra(RSCService.EXTRA_ACTIVITY, RSCManagerCallbacks.ACTIVITY_WALKING);
				// Update GUI
				onMeasurementReceived(speed, cadence, totalDistance, activity);
			} else if (RSCService.BROADCAST_STRIDES_UPDATE.equals(action)) {
				final int strides = intent.getIntExtra(RSCService.EXTRA_STRIDES, 0);
				final float distance = intent.getFloatExtra(RSCService.EXTRA_DISTANCE, 0);
				// Update GUI
				onStripsesUpdate(distance, strides);
			}
		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RSCService.BROADCAST_RSC_MEASUREMENT);
		intentFilter.addAction(RSCService.BROADCAST_STRIDES_UPDATE);
		return intentFilter;
	}
}
