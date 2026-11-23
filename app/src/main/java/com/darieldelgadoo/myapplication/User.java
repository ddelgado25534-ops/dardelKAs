package com.darieldelgadoo.myapplication;

import com.google.gson.annotations.SerializedName;

public class User {
    private int id;
    @SerializedName("full_name")
    private String fullName;
    private String email;
    private String role;

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}