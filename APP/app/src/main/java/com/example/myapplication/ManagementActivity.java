package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class ManagementActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList <HistoryItemModel> itemModels;
    HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        getWidget();
        HistoryItemModel itemModel = new HistoryItemModel("Nhận hoa hồng", "22/02/2020", "Quang Thiện", "5000000", true);
        itemModels.add(itemModel);
        adapter = new HistoryAdapter(itemModels, this);
        recyclerView.setAdapter(adapter);
        setEvent();
    }

    private void setEvent(){

    }

    private void getWidget(){
        itemModels = new ArrayList<HistoryItemModel>();
        recyclerView = (RecyclerView) findViewById(R.id.rv_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
