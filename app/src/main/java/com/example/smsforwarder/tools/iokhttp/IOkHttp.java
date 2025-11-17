package com.example.smsforwarder.tools.iokhttp;

import com.example.smsforwarder.listener.ResponseListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IOkHttp {

    private OkHttpClient client;
    public static final String TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single";

    public IOkHttp post(String url, RequestBody body, ResponseListener responseListener) {
        if (client == null)
            client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                responseListener.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    responseListener.onSuccess(response);
                } else {
                    responseListener.onFailure(new Throwable("error code " + response.code()));
                }
            }
        });
        return this;
    }

    public IOkHttp get(HttpUrl url, ResponseListener responseListener) {
        if (client == null)
            client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                responseListener.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    responseListener.onSuccess(response);
                } else {
                    responseListener.onFailure(new Throwable("error code " + response.code()));
                }
            }
        });
        return this;
    }

}
