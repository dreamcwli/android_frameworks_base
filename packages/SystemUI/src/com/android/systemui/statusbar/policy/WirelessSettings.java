package com.android.systemui.statusbar.policy;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

public class WirelessSettings {
    public static boolean isRadioAllowed(Context context, String radio) {
        ContentResolver resolver = context.getContentResolver();
        String radios = Settings.System.getString(resolver, Settings.System.AIRPLANE_MODE_RADIOS);
        if (radios == null || !radios.contains(radio)) {
            return true;
        }
        if (Settings.System.getInt(resolver, Settings.System.AIRPLANE_MODE_ON, 0) == 0) {
            return true;
        }
        radios = Settings.System.getString(
                resolver, Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        return radios != null && radios.contains(radio);
    }
}
