package com.erastimothy.laundry_app.model;


import java.io.Serializable;

public class User implements Serializable {
    private int id,role_id;
    private String name,email,access_token,phoneNumber,avatar,role_name;

    public User(int id, int role_id, String name, String email, String access_token, String phoneNumber, String avatar, String role_name) {
        this.id = id;
        this.role_id = role_id;
        this.name = name;
        this.email = email;
        this.access_token = access_token;
        this.phoneNumber = phoneNumber;
        this.avatar = avatar;
        this.role_name = role_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRole_id() {
        return role_id;
    }

    public void setRole_id(int role_id) {
        this.role_id = role_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole_name() {
        return role_name;
    }

    public void setRole_name(String role_name) {
        this.role_name = role_name;
    }
}
