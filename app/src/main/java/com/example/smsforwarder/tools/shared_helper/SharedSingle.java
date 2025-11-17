package com.example.smsforwarder.tools.shared_helper;

import android.content.Context;

public class SharedSingle {

    private static SharedHelper sharedHelper;

    public static SharedHelper getSharedHelper(Context applicatioContext) {
        if (sharedHelper == null)
            sharedHelper = new SharedHelper(applicatioContext);
        return sharedHelper;
    }
}
