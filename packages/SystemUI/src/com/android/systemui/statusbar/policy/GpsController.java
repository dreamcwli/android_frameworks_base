package com.android.systemui.statusbar.policy;

import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.CompoundButton;

import java.util.Observable;
import java.util.Observer;

public class GpsController implements CompoundButton.OnCheckedChangeListener {
    private static final Uri LOCATION_PROVIDERS_URI =
            Settings.Secure.getUriFor(Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
    private Context mContext;
    private CompoundButton mButton;

    private ContentResolver mResolver;
    private ContentQueryMap mMap;
    private Observer mObserver = new Observer() {
        public void update(Observable observable, Object data) {
            updateButton();
        }
    };
    private boolean mUpdating = false;

    public GpsController(Context context, CompoundButton button) {
        mContext = context;
        mButton = button;
        button.setOnCheckedChangeListener(this);
        updateButton();
        mResolver = context.getContentResolver();
        Cursor cursor = mResolver.query(Settings.Secure.CONTENT_URI, null,
                "(" + Settings.System.NAME + "=?)",
                new String[]{Settings.Secure.LOCATION_PROVIDERS_ALLOWED}, null);
        mMap = new ContentQueryMap(cursor, Settings.System.NAME, true, null);
        mMap.addObserver(mObserver);
    }

    public void release() {
        mMap.deleteObserver(mObserver);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mUpdating) {
            return;
        }

        Settings.Secure.setLocationProviderEnabled(
                mResolver, LocationManager.GPS_PROVIDER, isChecked);
    }

    private void updateButton() {
        mUpdating = true;
        mButton.setChecked(Settings.Secure.isLocationProviderEnabled(
                    mResolver, LocationManager.GPS_PROVIDER));
        mUpdating = false;
    }
}
