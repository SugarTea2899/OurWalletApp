package com.example.myapplication;

public class StatisticModel {
    public String payMemberName;
    public long revenues;
    public long expenditures;

    public StatisticModel(String payMemberName, long revenues, long expenditures) {
        this.payMemberName = payMemberName;
        this.revenues = revenues;
        this.expenditures = expenditures;
    }
}
