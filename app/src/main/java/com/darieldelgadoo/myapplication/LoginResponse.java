package com.darieldelgadoo.myapplication;

public class LoginResponse {
    private String status;
    private String message;
    private User user;

    private int id;
    private String full_name;
    private String email;
    private String role;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public User getUser() { return user; }
    public int getId() { return user != null ? user.getId() : id; }
    public String getFullName() { return user != null ? user.getFullName() : full_name; }
    public String getEmail() { return user != null ? user.getEmail() : email; }
    public String getRole() { return user != null ? user.getRole() : role; }
}