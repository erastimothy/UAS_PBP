package com.erastimothy.laundry_app.api;

public class UserAPI extends ApiURL{

    //Tambahkan api buku disini
    public static final String URL_REGISTER    = ROOT_API+"register"; //POST
    public static final String URL_LOGIN    = ROOT_API+"login"; //POST
    public static final String URL_LOGOUT    = ROOT_API+"logout"; //POST
    public static final String URL_UPDATE    = ROOT_API+"user/"; //PUT
    public static final String URL_SELECT    = ROOT_API+"user"; //GET

//    public static final String URL_DELETE    = ROOT_API+"buku/"; //DELETE   ON WEB ADMIN
}
