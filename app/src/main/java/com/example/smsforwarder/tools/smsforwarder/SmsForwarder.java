package com.example.smsforwarder.tools.smsforwarder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.example.smsforwarder.tools.shared_helper.SharedSingle;

import java.util.ArrayList;
import java.util.List;

public class SmsForwarder {
    public static void sendSms(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
        } catch (Exception e) {
            Log.d("SmsForwarder", "Error sending SMS: " + e.getMessage());
        }
    }
}
