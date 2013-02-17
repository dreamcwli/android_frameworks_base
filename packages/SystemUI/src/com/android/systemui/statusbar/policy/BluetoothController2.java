package com.android.systemui.statusbar.policy;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.widget.CompoundButton;

public class BluetoothController2 extends BroadcastReceiver
        implements CompoundButton.OnCheckedChangeListener {
    private Context mContext;
    private CompoundButton mButton;

    private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private boolean mUpdating = false;

    public BluetoothController2(Context context, CompoundButton button) {
        mContext = context;
        mButton = button;
        button.setOnCheckedChangeListener(this);
        updateButton(mAdapter.getState());

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(this, filter);
    }

    public void release() {
        mContext.unregisterReceiver(this);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mUpdating) {
            return;
        }

        if (isChecked
                && !WirelessSettings.isRadioAllowed(mContext, Settings.System.RADIO_BLUETOOTH)) {
            buttonView.setChecked(false);
            return;
        }

        if (isChecked ? mAdapter.enable() : mAdapter.disable()) {
            buttonView.setEnabled(false);
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            updateButton(intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF));
        }
    }

    private void updateButton(int state) {
        mUpdating = true;
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                mButton.setChecked(false);
                mButton.setEnabled(true);
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                mButton.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_ON:
                mButton.setChecked(true);
                mButton.setEnabled(true);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                mButton.setEnabled(false);
                break;
            default:
                mButton.setChecked(false);
                mButton.setEnabled(true);
                break;
        }
        mUpdating = false;
    }
}
