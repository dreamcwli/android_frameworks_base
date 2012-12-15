package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.CompoundButton;

public class WifiController extends BroadcastReceiver
        implements CompoundButton.OnCheckedChangeListener {
    private Context mContext;
    private CompoundButton mButton;

    private WifiManager mManager;
    private boolean mUpdating = false;

    public WifiController(Context context, CompoundButton button) {
        mContext = context;
        mButton = button;
        button.setOnCheckedChangeListener(this);
        mManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        updateButton(mManager.getWifiState());

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        context.registerReceiver(this, filter);
    }

    public void release() {
        mContext.unregisterReceiver(this);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mUpdating) {
            return;
        }

        if (isChecked && !WirelessSettings.isRadioAllowed(mContext, Settings.System.RADIO_WIFI)) {
            buttonView.setChecked(false);
            return;
        }

        int apState = mManager.getWifiApState();
        if (isChecked && (apState == WifiManager.WIFI_AP_STATE_ENABLING
                    || apState == WifiManager.WIFI_AP_STATE_ENABLED)) {
            mManager.setWifiApEnabled(null, false);
        }

        if (mManager.setWifiEnabled(isChecked)) {
            buttonView.setEnabled(false);
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            updateButton(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
        }
    }

    private void updateButton(int state) {
        mUpdating = true;
        switch (state) {
            case WifiManager.WIFI_STATE_DISABLING:
                mButton.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                mButton.setChecked(false);
                mButton.setEnabled(true);
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                mButton.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                mButton.setChecked(true);
                mButton.setEnabled(true);
                break;
            default:
                mButton.setChecked(false);
                mButton.setEnabled(true);
                break;
        }
        mUpdating = false;
    }
}
