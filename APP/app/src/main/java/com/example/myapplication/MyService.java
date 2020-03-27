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
        String type = (String) data.get("type");
        switch (type){
            case "1":
                String doer = (String) data.get("fcmToken");
                SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                String fcmToken = sharedPreferences.getString("fcmToken", "");

                if (!doer.equals(fcmToken)){
                    Intent intent = new Intent("LoadAgain");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                break;
            case "2":
                Intent intent =  new Intent("BeBanned");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
            case "3":
                Intent intent1 =  new Intent("BeUnBanned");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
                break;
            case "4":
                Intent intent2 = new Intent("BanEdit");
                String temp = (String) data.get("isAdmin");
                boolean isAdmin;
                isAdmin = temp.equals("1");
                intent2.putExtra("isAdmin", isAdmin);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent2);
                break;
        }

    }
}
