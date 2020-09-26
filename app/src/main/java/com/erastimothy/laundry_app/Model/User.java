package com.erastimothy.laundry_app.Model;

public class User {
    private String name,email,password,phoneNumber,uid;

    public User(String uid,String name, String email, String password, String phoneNumber) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }
    public String getUid() {return uid;}

    public void setUid(String uid){this.uid = uid;}

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
