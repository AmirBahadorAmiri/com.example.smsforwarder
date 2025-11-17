package com.example.smsforwarder.tools.texttools;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class TextTools {

    public static String base64HighEncoding(String str) {
        return base64Encode(reverse(base64Encode(str)));
    }

    public static String base64HighDecoding(String str) {
        return base64Decode(reverse(base64Decode(str)));
    }

    public static String base64Decode(String str) {
        byte[] data = Base64.decode(str, Base64.DEFAULT);
        return new String(data, StandardCharsets.UTF_8);
    }

    public static String base64Encode(String str) {
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }
}