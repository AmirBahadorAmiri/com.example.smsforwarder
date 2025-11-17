package com.example.smsforwarder.tools.dexter;

import android.content.Context;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

public class DexterTool {

    public static void requestPermissions(Context context, String[] permissions, MultiplePermissionsListener multiplePermissionsListener) {
        Dexter.withContext(context)
                .withPermissions(permissions)
                .withListener(multiplePermissionsListener).check();
    }

}
