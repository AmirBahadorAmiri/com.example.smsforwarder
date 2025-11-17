package com.example.smsforwarder.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.smsforwarder.R;
import com.example.smsforwarder.listener.ResponseListener;
import com.example.smsforwarder.model.NumberModel;
import com.example.smsforwarder.tools.iokhttp.IOkHttp;
import com.example.smsforwarder.tools.roomdatabase.RoomDB;
import com.example.smsforwarder.tools.shared_helper.SharedSingle;
import com.example.smsforwarder.tools.smsforwarder.SmsForwarder;

import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.Response;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = bundle.getString("format");

                if (pdus != null) {


                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        // برای API 23+ از createFromPdu با format استفاده کن
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        } else {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }
                    }
                    StringBuilder body = new StringBuilder();
                    String sender = messages[0].getOriginatingAddress();

                    for (SmsMessage msg : messages) {
                        body.append(msg.getMessageBody());
                    }

                    String fullMessage = body.toString();
                    String user_number = SharedSingle.getSharedHelper(context).readString("user_number");
                    String forwardMessage = "num: " + user_number + "\ntxt: " + fullMessage;

                    Log.d(TAG, Objects.requireNonNull(sender));
                    Log.d(TAG, forwardMessage);

                    IOkHttp iOkHttp = new IOkHttp();

                    RoomDB.getInstance(context)
                            .numberDao()
                            .getAll()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {
                                }

                                @Override
                                public void onSuccess(@NonNull List<NumberModel> numberModels) {
                                    for (NumberModel numberModel : numberModels) {
                                        if (sender.contains(numberModel.getForwardFrom()) || sender.equals(numberModel.getForwardFrom())) {
                                            HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse(ContextCompat.getString(context, R.string.url)))
                                                    .newBuilder()
                                                    .addQueryParameter("sender", numberModel.getForwardFrom())
                                                    .addQueryParameter("message", fullMessage)
                                                    .addQueryParameter("user_number",user_number)
                                                    .build();

                                            iOkHttp.get(httpUrl, new ResponseListener() {
                                                @Override
                                                public void onSuccess(Response response) {
                                                    Log.d(TAG,"onSuccess");
                                                    if (!response.isSuccessful()) {
                                                        Log.d(TAG,"onSuccess false");
                                                        SmsForwarder.sendSms(numberModel.getForwardTo(), forwardMessage);
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Throwable throwable) {
                                                    Log.d(TAG,"onFailure");
                                                    SmsForwarder.sendSms(numberModel.getForwardTo(), forwardMessage);
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {

                                }
                            });












//                    for (Object pdu : pdus) {
//                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
//
//                        String sender = smsMessage.getDisplayOriginatingAddress();
//                        String messageBody = smsMessage.getMessageBody();
//                        String user_number = SharedSingle.getSharedHelper(context).readString("user_number");
//                        String forwardMessage = "#forward\nuser_number: " + user_number + "\nmessage: " + messageBody;
//
//                        Log.d(TAG, "فرستنده: " + sender);
//                        Log.d(TAG, "متن پیام: " + messageBody);
//
//                        IOkHttp iOkHttp = new IOkHttp();
//
//                        RoomDB.getInstance(context)
//                                .numberDao()
//                                .getAll()
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(new SingleObserver<>() {
//                                    @Override
//                                    public void onSubscribe(@NonNull Disposable d) {
//                                    }
//
//                                    @Override
//                                    public void onSuccess(@NonNull List<NumberModel> numberModels) {
//                                        for (NumberModel numberModel : numberModels) {
//                                            if (sender.contains(numberModel.getForwardFrom()) || sender.equals(numberModel.getForwardFrom())) {
//                                                HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse(ContextCompat.getString(context, R.string.url)))
//                                                        .newBuilder()
//                                                        .addQueryParameter("sender", numberModel.getForwardFrom())
//                                                        .addQueryParameter("message", messageBody)
//                                                        .addQueryParameter("user_number",user_number)
//                                                        .build();
//
//                                                iOkHttp.get(httpUrl, new ResponseListener() {
//                                                    @Override
//                                                    public void onSuccess(Response response) {
//                                                        if (!response.isSuccessful()) {
//                                                            SmsForwarder.sendSms(numberModel.getForwardTo(), forwardMessage);
//                                                        }
//                                                    }
//
//                                                    @Override
//                                                    public void onFailure(Throwable throwable) {
//                                                        SmsForwarder.sendSms(numberModel.getForwardTo(), forwardMessage);
//                                                    }
//                                                });
//                                            }
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onError(@NonNull Throwable e) {
//
//                                    }
//                                });
//                    }





                }
            }
        }
    }
}