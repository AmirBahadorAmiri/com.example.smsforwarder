package com.example.smsforwarder.listener;

import okhttp3.Response;

public interface ResponseListener {
    void onSuccess(Response response);
    void onFailure(Throwable throwable);
}
