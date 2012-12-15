/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.tablet;

import android.app.StatusBarManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Slog;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.AirplaneModeController;
import com.android.systemui.statusbar.policy.AutoRotateController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.BrightnessController;
import com.android.systemui.statusbar.policy.DoNotDisturbController;
import com.android.systemui.statusbar.policy.GpsController;
import com.android.systemui.statusbar.policy.ToggleSlider;
import com.android.systemui.statusbar.policy.VolumeController;
import com.android.systemui.statusbar.policy.WifiController;

public class SettingsView extends LinearLayout implements View.OnClickListener {
    static final String TAG = "SettingsView";

    BrightnessController mBrightness;
    AirplaneModeController mAirplane;
    WifiController mWifi;
    BluetoothController mBluetooth;
    GpsController mGps;
    AutoRotateController mRotate;
    DoNotDisturbController mDoNotDisturb;
    View mRotationLockContainer;
    View mRotationLockSeparator;

    public SettingsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final Context context = getContext();

        mBrightness = new BrightnessController(context,
                (ImageView)findViewById(R.id.brightness_icon),
                (ToggleSlider)findViewById(R.id.brightness));

        mAirplane = new AirplaneModeController(context,
                (CompoundButton)findViewById(R.id.airplane_checkbox));

        mWifi = new WifiController(context,
                (CompoundButton)findViewById(R.id.wifi_checkbox));
        findViewById(R.id.wifi).setOnClickListener(this);

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            findViewById(R.id.bluetooth).setVisibility(View.GONE);
            findViewById(R.id.bluetooth_separator).setVisibility(View.GONE);
        } else {
            mBluetooth = new BluetoothController(context,
                    (CompoundButton)findViewById(R.id.bluetooth_checkbox));
            findViewById(R.id.bluetooth).setOnClickListener(this);
        }

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            findViewById(R.id.gps).setVisibility(View.GONE);
            findViewById(R.id.gps_separator).setVisibility(View.GONE);
        } else {
            mGps = new GpsController(context,
                    (CompoundButton)findViewById(R.id.gps_checkbox));
            findViewById(R.id.gps).setOnClickListener(this);
        }

        mRotationLockContainer = findViewById(R.id.rotate);
        mRotationLockSeparator = findViewById(R.id.rotate_separator);
        mRotate = new AutoRotateController(context,
                (CompoundButton)findViewById(R.id.rotate_checkbox),
                new AutoRotateController.RotationLockCallbacks() {
                    @Override
                    public void setRotationLockControlVisibility(boolean show) {
                        mRotationLockContainer.setVisibility(show ? View.VISIBLE : View.GONE);
                        mRotationLockSeparator.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });

        mDoNotDisturb = new DoNotDisturbController(context,
                (CompoundButton)findViewById(R.id.do_not_disturb_checkbox));

        findViewById(R.id.settings).setOnClickListener(this);

        /*
        LayoutInflater inflater = LayoutInflater.from(context);
        View item;
        View separator;

        item = inflater.inflate(R.layout.system_bar_settings_view_switch, this, false);
        ((ImageView)item.findViewById(R.id.icon)).setImageResource(R.drawable.ic_sysbar_wifi_on);
        ((TextView)item.findViewById(R.id.label)).setText(R.string.status_bar_settings_wifi_button);
        addView(item);
        separator = inflater.inflate(R.layout.system_bar_settings_view_separator, this, false);
        addView(separator);

        if (BluetoothAdapter.getDefaultAdapter() != null) {
            item = inflater.inflate(R.layout.system_bar_settings_view_switch, this, false);
            ((ImageView)item.findViewById(R.id.icon)).setImageResource(R.drawable.ic_sysbar_bluetooth);
            ((TextView)item.findViewById(R.id.label)).setText(R.string.status_bar_settings_bluetooth);
            addView(item);
            separator = inflater.inflate(R.layout.system_bar_settings_view_separator, this, false);
            addView(separator);
        }

        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            item = inflater.inflate(R.layout.system_bar_settings_view_switch, this, false);
            ((ImageView)item.findViewById(R.id.icon)).setImageResource(R.drawable.ic_sysbar_gps);
            ((TextView)item.findViewById(R.id.label)).setText(R.string.status_bar_settings_gps);
            addView(item);
            separator = inflater.inflate(R.layout.system_bar_settings_view_separator, this, false);
            addView(separator);
        }
        */
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAirplane.release();
        mWifi.release();
        if (mBluetooth != null) {
            mBluetooth.release();
        }
        if (mGps != null) {
            mGps.release();
        }
        mRotate.release();
        mDoNotDisturb.release();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wifi:
                onClickWifi();
                break;
            case R.id.bluetooth:
                onClickBluetooth();
                break;
            case R.id.gps:
                onClickGps();
                break;
            case R.id.settings:
                onClickSettings();
                break;
        }
    }

    private StatusBarManager getStatusBarManager() {
        return (StatusBarManager)getContext().getSystemService(Context.STATUS_BAR_SERVICE);
    }

    // Wi-Fi
    // ----------------------------
    private void onClickWifi() {
        getContext().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        getStatusBarManager().collapsePanels();
    }

    // Bluetooth
    // ----------------------------
    private void onClickBluetooth() {
        getContext().startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        getStatusBarManager().collapsePanels();
    }

    // GPS
    // ----------------------------
    private void onClickGps() {
        getContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        getStatusBarManager().collapsePanels();
    }

    // Settings
    // ----------------------------
    private void onClickSettings() {
        getContext().startActivityAsUser(new Intent(Settings.ACTION_SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                new UserHandle(UserHandle.USER_CURRENT));
        getStatusBarManager().collapsePanels();
    }
}

