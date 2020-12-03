package com.erastimothy.laundry_app.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.erastimothy.laundry_app.model.User;

public class UserPreferences {
    SharedPreferences userSP;
    SharedPreferences.Editor editor;
    Context context;
    User user;

    private static final String IS_LOGIN = "isLoggedIn";

    public static final String KEY_EMAIL = "email";
    public static final String KEY_NAME = "name";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_ID = "id";
    public static final String KEY_PHONENUMBER = "phoneNumber";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_ROLE_ID = "role_id";
    public static final String KEY_ROLE_NAME = "role_name";

    public UserPreferences(Context context) {
        this.context = context;
        userSP = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        editor = userSP.edit();
    }

    public void createLoginUser(int id, String email, String access_token, String name, String phoneNumber, int role_id, String role_name,String avatar) {
        //assign user login
        editor.putBoolean(IS_LOGIN, true);

        editor.putInt(KEY_ID, id);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ACCESS_TOKEN, access_token);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_ROLE_NAME, role_name);
        editor.putString(KEY_PHONENUMBER, phoneNumber);
        editor.putString(KEY_AVATAR, avatar);
        editor.putInt(KEY_ROLE_ID, role_id);

        editor.commit();
    }

    public String getAccesToken(){
        return userSP.getString(KEY_ACCESS_TOKEN,null);
    }

    public User getUserLoginFromSharedPrefernces() {
        int id,role_id;
        String name,email,access_token,phoneNumber,avatar,role_name;

        id = userSP.getInt(KEY_ID,0);
        role_id = userSP.getInt(KEY_ROLE_ID, 0);
        name = userSP.getString(KEY_NAME, null);
        email = userSP.getString(KEY_EMAIL, null);
        access_token = userSP.getString(KEY_ACCESS_TOKEN, null);
        phoneNumber = userSP.getString(KEY_PHONENUMBER, null);
        avatar = userSP.getString(KEY_AVATAR, null);
        role_name = userSP.getString(KEY_ROLE_NAME, null);

        user = new User(id,role_id,name,email,access_token,phoneNumber,avatar,role_name);
        return user;
    }

    public boolean checkLogin() {
        if (userSP.getBoolean(IS_LOGIN, true)) {
            return true;
        } else
            return false;
    }

    public void logout(){
        editor.clear();
        editor.commit();
    }
}
