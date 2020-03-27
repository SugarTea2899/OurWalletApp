package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class container extends AppCompatActivity {

    BottomNavigationView bnvNav;
    boolean doubleBackToExitPressedOnce = false;
    public static boolean isSuperAdmin;
    public static boolean isAdmin;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "Bạn đã bị chặn khỏi ví này.", Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
    };

    private BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isAdmin = intent.getBooleanExtra("isAdmin", false);
            container.isAdmin = isAdmin;
            if (isAdmin){
                Toast.makeText(getApplicationContext(), "Bạn đã được cho phép thêm thu chi.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "Bạn đã bị chặn thêm thu chi.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        getWidget();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("BeBanned"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver1, new IntentFilter("BanEdit"));
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ManagementFragment()).commit();
        setEvent();
    }

    private void setEvent(){
        bnvNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()){
                    case R.id.nav_home:
                        selectedFragment = new ManagementFragment();
                        break;
                    case R.id.nav_member:
                        selectedFragment =  new MemberListFragment();
                        break;
                    case R.id.nav_stat:
                        selectedFragment = new StatisticFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver1);
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Click một lần nữa để thoát.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver1);
        super.onDestroy();
    }

    private void getWidget(){
        bnvNav = (BottomNavigationView) findViewById(R.id.nav_bar);
    }

}
