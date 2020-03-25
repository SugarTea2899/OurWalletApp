package com.example.myapplication;

public class MemberModel {
    public String name;
    public boolean isBanned;
    public boolean isAdmin;
    public boolean isSuperAdmin;

    public MemberModel(String name, boolean isBanned, boolean isAdmin, boolean isSuperAdmin) {
        this.name = name;
        this.isBanned = isBanned;
        this.isAdmin = isAdmin;
        this.isSuperAdmin = isSuperAdmin;
    }


}
