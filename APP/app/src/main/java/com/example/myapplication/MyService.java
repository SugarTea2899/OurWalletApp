package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map data = remoteMessage.getData();
        String doer = (String) data.get("fcmToken");
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String fcmToken = sharedPreferences.getString("fcmToken", "");

        if (!doer.equals(fcmToken)){
            Intent intent = new Intent("LoadAgain");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }else{
            Log.d("xxx", "equalllll");
        }
    }
}
