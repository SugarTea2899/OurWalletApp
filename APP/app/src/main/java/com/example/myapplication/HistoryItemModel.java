package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class HistoryItemModel {
    @SerializedName("describe")
    public String content;
    @SerializedName("createOn")
    public String date;
    public String name;
    public long value;
    public boolean isRevenue;
    public String payMemberName;

    public HistoryItemModel(String content, String date, String userName, long value, boolean isRevenues) {
        this.content = content;
        this.date = date;
        this.name = userName;
        this.value = value;
        this.isRevenue = isRevenues;
    }

    public HistoryItemModel(){

    }
}
