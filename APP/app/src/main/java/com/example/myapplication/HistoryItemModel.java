package com.example.myapplication;

public class HistoryItemModel {
    public String content;
    public String date;
    public String userName;
    public String value;
    public boolean isRevenues;

    public HistoryItemModel(String content, String date, String userName, String value, boolean isRevenues) {
        this.content = content;
        this.date = date;
        this.userName = userName;
        this.value = value;
        this.isRevenues = isRevenues;
    }

    public HistoryItemModel(){

    }
}
