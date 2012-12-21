package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.widget.CompoundButton;

public class GpsController implements CompoundButton.OnCheckedChangeListener {
    private class GpsObserver extends ContentObserver {
        public GpsObserver(Handler handler) {
            super(handler);
        }

        @Override public void onChange(boolean selfChange) {
            updateButton();
        }

        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.LOCATION_PROVIDERS_ALLOWED),
                    false, this, mUser);
        }

        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }

    private Context mContext;
    private CompoundButton mButton;
    private int mUser;

    private Handler mHandler;
    private GpsObserver mObserver;

    private boolean mUpdating = false;

    public GpsController(Context context, CompoundButton button) {
        mContext = context;
        mButton = button;
        mUser = ActivityManager.getCurrentUser();
        updateButton();
        button.setOnCheckedChangeListener(this);

        mHandler = new Handler();
        mObserver = new GpsObserver(mHandler);
        mObserver.startObserving();
    }

    public void release() {
        mObserver.stopObserving();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mUpdating) {
            return;
        }

        Settings.Secure.setLocationProviderEnabledForUser(mContext.getContentResolver(),
                LocationManager.GPS_PROVIDER, isChecked, mUser);
    }

    private void updateButton() {
        mUpdating = true;
        mButton.setChecked(Settings.Secure.isLocationProviderEnabledForUser(
                    mContext.getContentResolver(), LocationManager.GPS_PROVIDER, mUser));
        mUpdating = false;
    }
}
