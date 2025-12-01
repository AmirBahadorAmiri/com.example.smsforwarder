package com.example.smsforwarder.smsreceiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.smsforwarder.R;
import com.example.smsforwarder.listener.ResponseListener;
import com.example.smsforwarder.model.NumberModel;
import com.example.smsforwarder.tools.iokhttp.IOkHttp;
import com.example.smsforwarder.tools.roomdatabase.RoomDB;
import com.example.smsforwarder.tools.shared_helper.SharedSingle;
import com.example.smsforwarder.tools.smsforwarder.SmsForwarder;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.Response;

public class SmsForegroundService extends Service {

    private static final String CHANNEL_ID = "sms_forward_channel";
    private static final int NOTIFICATION_ID = 999;
    private static final String TAG = "OMG";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        createNotificationChannel();
        // ثبت BroadcastReceiver داخل سرویس (مهم!)
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(smsReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundWithNotification();
        return START_STICKY; // اگر کشته شد، دوباره راه بیفتد
    }

    private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {
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
                                                Log.d(TAG, "Successful");
                                                SmsForwarder.sendSms(numberModel.getForwardTo(), forwardMessage);
//                                                HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse(ContextCompat.getString(context, R.string.url)))
//                                                        .newBuilder()
//                                                        .addQueryParameter("sender", numberModel.getForwardFrom())
//                                                        .addQueryParameter("message", fullMessage)
//                                                        .addQueryParameter("user_number", user_number)
//                                                        .build();
//
//                                                iOkHttp.get(httpUrl, new ResponseListener() {
//                                                    @Override
//                                                    public void onSuccess(Response response) {
//                                                        Log.d(TAG, "onSuccess");
//                                                        if (!response.isSuccessful()) {
//                                                            Log.d(TAG, "not Successful");
//                                                            SmsForwarder.sendSms(numberModel.getForwardTo(), forwardMessage);
//                                                        } else {
//                                                            Log.d(TAG, "isSuccessful");
//                                                            try {
//                                                                String string = response.body().string();
//                                                                JSONObject object = new JSONObject(string);
//                                                                if (!object.getBoolean("success")) {
//                                                                    SmsForwarder.sendSms(numberModel.getForwardTo(), forwardMessage);
//                                                                } else {
//                                                                    Log.d(TAG, "object.getBoolean(\"success\") is true");
//                                                                }
//                                                            } catch (Exception e) {
//                                                                SmsForwarder.sendSms(numberModel.getForwardTo(), forwardMessage);
//                                                            }
//                                                        }
//                                                    }
//
//                                                    @Override
//                                                    public void onFailure(Throwable throwable) {
//                                                        Log.d(TAG, "onFailure");
//                                                        SmsForwarder.sendSms(numberModel.getForwardTo(), forwardMessage);
//                                                    }
//                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {

                                    }
                                });


                    }
                }
            }
        }
    };

    private void startForegroundWithNotification() {
        Log.d(TAG, "startForegroundWithNotification");

        Intent restartServiceIntent = new Intent(this, SmsForegroundService.class);
        int pendingIntentFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent restartPendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, pendingIntentFlag | PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("هدایت پیامک فعال است")
                .setContentText("همه پیام‌ها از شماره‌های وارد شده فوروارد می‌شوند")
                .setSmallIcon(R.drawable.ic_sms)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(restartPendingIntent)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "سرویس فوروارد SMS",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(false);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1});
            channel.enableLights(false);
            channel.setBypassDnd(false);
            channel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(smsReceiver);
        } catch (Exception e) {
            // ignore
        }
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved, restarting service...");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
