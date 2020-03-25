package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class container extends AppCompatActivity {

    BottomNavigationView bnvNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        getWidget();
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
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });
    }


    private void getWidget(){
        bnvNav = (BottomNavigationView) findViewById(R.id.nav_bar);
    }

}
