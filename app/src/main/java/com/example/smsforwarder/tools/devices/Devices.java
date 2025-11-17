package com.example.smsforwarder.tools.devices;

import android.content.Context;
import android.provider.Settings;

public class Devices {
    public static String getUniqueId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}