package com.example.smsforwarder.tools.shared_helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smsforwarder.R;


public class SharedHelper {

    private final SharedPreferences shp;
    private final SharedPreferences.Editor shpe;

    public SharedHelper(Context context) {
        shp = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        shpe = shp.edit();
    }

    public String readString(String key) {
        return shp.getString(key, "");
    }

    public int readInt(String key) {
        return shp.getInt(key, 0);
    }

    public Boolean readBoolean(String key) {
        return shp.getBoolean(key, false);
    }

    public Long readLong(String key) {
        return shp.getLong(key, 0);
    }

    public void insert(String key, String value) {
        shpe.putString(key, value);
        shpe.apply();
    }

    public void insert(String key, Boolean value) {
        shpe.putBoolean(key, value);
        shpe.apply();
    }

    public void insert(String key, Long value) {
        shpe.putLong(key, value);
        shpe.apply();
    }

    public void insert(String key, int value) {
        shpe.putInt(key, value);
        shpe.apply();
    }
}
