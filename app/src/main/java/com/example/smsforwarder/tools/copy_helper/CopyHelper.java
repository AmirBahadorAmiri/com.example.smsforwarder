package com.example.smsforwarder.tools.copy_helper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.util.Objects;

public class CopyHelper {

    private static ClipboardManager clipboardManager;

    public static void initialize(Context applicatioContext) {
        if (clipboardManager == null) {
            clipboardManager = (ClipboardManager) applicatioContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }
    }

    public static void insert(String str) {
        ClipData clipData = ClipData.newPlainText("", str);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static String read() {
        return Objects.requireNonNull(clipboardManager.getPrimaryClip()).getItemAt(0).getText().toString();
    }

}