package com.erastimothy.laundry_app.model;

import java.io.Serializable;

public class Laundry implements Serializable {
    private int id,service_id,user_id;
    private float quantity;
    private double shippingcost,total;
    private String status,address,date;
    public Laundry(){}
    public Laundry(int id, int service_id, int user_id, float quantity, double shippingcost, double total, String status,String address,String date) {
        this.id = id;
        this.service_id = service_id;
        this.user_id = user_id;
        this.quantity = quantity;
        this.shippingcost = shippingcost;
        this.total = total;
        this.status = status;
        this.address = address;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getService_id() {
        return service_id;
    }

    public void setService_id(int service_id) {
        this.service_id = service_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public double getShippingcost() {
        return shippingcost;
    }

    public void setShippingcost(double shippingcost) {
        this.shippingcost = shippingcost;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}

